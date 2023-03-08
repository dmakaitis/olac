package org.olac.reservation.manager.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.olac.reservation.config.OlacProperties;
import org.olac.reservation.engine.TemplateEngine;
import org.olac.reservation.manager.AdministrationManager;
import org.olac.reservation.manager.ReservationManager;
import org.olac.reservation.resource.NotificationAccess;
import org.olac.reservation.resource.PaymentProcessorAccess;
import org.olac.reservation.resource.ReservationDatastoreAccess;
import org.olac.reservation.resource.TicketDatastoreAccess;
import org.olac.reservation.resource.model.*;
import org.olac.reservation.resource.paypal.model.CreateOrderResponse;
import org.olac.reservation.resource.paypal.model.PurchaseUnit;
import org.olac.reservation.utility.SecurityUtility;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Consumer;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationManagerImpl implements ReservationManager, AdministrationManager {

    private final TicketDatastoreAccess ticketDatastoreAccess;
    private final ReservationDatastoreAccess reservationDatastoreAccess;
    private final TemplateEngine templateEngine;
    private final NotificationAccess notificationAccess;
    private final PaymentProcessorAccess paymentProcessorAccess;
    private final OlacProperties properties;
    private final SecurityUtility securityUtility;

    @Override
    public TicketType saveTicketType(TicketType ticketType) {
        return ticketDatastoreAccess.saveTicketType(ticketType);
    }

    @Override
    public List<TicketType> getTicketTypes() {
        return ticketDatastoreAccess.getTicketTypes();
    }

    @Override
    public Page<Reservation> getReservations(String filter, PageRequest pageRequest) {
        return reservationDatastoreAccess.getReservations(filter, pageRequest);
    }

    @Override
    public boolean areTicketsAvailable(long requestedTicketCount) {
        long availableTickets = properties.getMaxTickets() - reservationDatastoreAccess.getTotalTicketsReserved();
        return requestedTicketCount <= availableTickets;
    }

    @Override
    public boolean validateAndAddPayment(String reservationId, String paymentProcessorTransactionId) {
        if (isBlank(reservationId) || isBlank(paymentProcessorTransactionId)) {
            return false;
        }

        // Load the actual transaction from PayPal since the one we have could have been altered...
        Optional<CreateOrderResponse> responseOptional = paymentProcessorAccess.getOrder(paymentProcessorTransactionId);
        if (responseOptional.isEmpty()) {
            // Not a valid transaction
            return false;
        }
        CreateOrderResponse response = responseOptional.get();

        // Search through the transaction for our reservation in order to find the amount paid...
        double amount = getPaymentAmount(reservationId, response);

        if (amount <= 0.0) {
            // Could not find purchase unit with payment for reservation
            return false;
        }

        // Record the payment:
        Payment payment = Payment.builder()
                .amount(amount)
                .status(PaymentStatus.SUCCESSFUL)
                .method(PaymentMethod.ONLINE)
                .notes("PayPal Transaction ID: " + paymentProcessorTransactionId)
                .enteredBy(securityUtility.getCurrentUserName())
                .build();

        if (response.getCreateTime() != null) {
            payment.setCreatedTimestamp(response.getCreateTime());
        }

        reservationDatastoreAccess.addPaymentToReservation(reservationId, payment);
        reservationDatastoreAccess.getReservation(reservationId).ifPresent(r -> {
            // Update payment status if needed
            if (r.getStatus() == ReservationStatus.PENDING_PAYMENT) {
                double totalPaid = r.getPayments().stream()
                        .mapToDouble(Payment::getAmount)
                        .sum();

                if (totalPaid >= r.getAmountDue()) {
                    reservationDatastoreAccess.updateReservationStatus(reservationId, ReservationStatus.RESERVED);
                }
            }

            // Send confirmation email
            String message = templateEngine.createPaymentReceivedConfirmation(r);
            notificationAccess.sentNotification(r.getEmail(), "Reservation Confirmation", message);
        });

        return true;
    }

    @Override
    public void deleteTicketType(String ticketTypeCode) {
        ticketDatastoreAccess.deleteTicketType(ticketTypeCode);
    }

    @Override
    public void deleteReservation(String reservationId) {
        reservationDatastoreAccess.deleteReservation(reservationId);
    }

    @Override
    public Reservation saveReservation(Reservation reservation) {
        if (securityUtility.isCurrentUserAdmin()) {
            return saveReservation(reservation, true);
        }

        // If the user is not an admin, they can only save the full reservation if it doesn't already exist
        Optional<Reservation> oldReservationOptional = reservationDatastoreAccess.getReservation(reservation.getReservationId());
        if (oldReservationOptional.isEmpty()) {
            return saveReservation(reservation, true);
        }

        // Non administrators can only add new payments...
        Reservation oldReservation = oldReservationOptional.get();
        List<Payment> payments = new ArrayList<>(oldReservation.getPayments());
        int startPaymentsSize = payments.size();
        reservation.getPayments().stream()
                .filter(p -> p.getId() == null)
                .forEach(payments::add);

        if (startPaymentsSize != payments.size()) {
            oldReservation.setPayments(payments);
            return saveReservation(oldReservation, true);
        }

        return oldReservation;
    }

    @Override
    public Reservation saveReservation(Reservation reservation, boolean sendNotification) {
        // Make sure we document who entered any new payments and when:
        String currentUser = securityUtility.getCurrentUserName();
        reservation.getPayments().stream()
                .filter(p -> isBlank(p.getEnteredBy()))
                .forEach(p -> {
                    p.setEnteredBy(currentUser);
                    p.setCreatedTimestamp(new Date());
                });

        // Update our amount due...
        Map<String, Double> ticketTypes = getTicketTypes().stream()
                .collect(toMap(TicketType::getCode, TicketType::getCostPerTicket));

        reservation.setAmountDue(reservation.getTicketCounts().stream()
                .mapToDouble(c -> c.getCount() * ticketTypes.getOrDefault(c.getTicketTypeCode(), 0.0))
                .sum());

        // Update status, if needed
        if (reservation.getStatus() == ReservationStatus.PENDING_PAYMENT || reservation.getStatus() == ReservationStatus.RESERVED) {
            double amountPaid = reservation.getPayments().stream()
                    .filter(p -> p.getStatus() == PaymentStatus.SUCCESSFUL)
                    .mapToDouble(Payment::getAmount)
                    .sum();

            if (amountPaid >= reservation.getAmountDue()) {
                reservation.setStatus(ReservationStatus.RESERVED);
            } else {
                reservation.setStatus(ReservationStatus.PENDING_PAYMENT);
            }
        }

        // If we don't have a reservation timestamp, set it now...
        if (reservation.getReservationTimestamp() == null) {
            reservation.setReservationTimestamp(new Date());
        }

        Consumer<Reservation> sendReservationFunction;

        // Send notifications if the reservation status has changed and notifications were requested
        if (sendNotification && reservationStatusHasChanged(reservation)) {
            switch (reservation.getStatus()) {
                case PENDING_PAYMENT -> sendReservationFunction = r ->
                        notificationAccess.sentNotification(
                                r.getEmail(),
                                "Reservation Payment Reminder",
                                templateEngine.createPaymentInstructions(r));
                case RESERVED -> sendReservationFunction = r -> notificationAccess.sentNotification(
                        r.getEmail(),
                        "Reservation Confirmation",
                        templateEngine.createPaymentReceivedConfirmation(r));
                default ->
                        sendReservationFunction = r -> log.debug("No notification sent for current reservation status: {}", r.getStatus());
            }
        } else {
            sendReservationFunction = r -> {
            };
        }

        // Save the reservations, generating the reservation number if it doesn't already exist
        reservation = reservationDatastoreAccess.saveReservation(reservation);

        // Send whatever notification we determined was needed now that we have a reservation number
        sendReservationFunction.accept(reservation);

        return reservation;
    }

    private boolean reservationStatusHasChanged(Reservation reservation) {
        return reservationDatastoreAccess.getReservation(reservation.getReservationId())
                .map(Reservation::getStatus)
                .map(status -> status != reservation.getStatus())
                .orElse(true);
    }

    private static double getPaymentAmount(String reservationId, CreateOrderResponse response) {
        List<PurchaseUnit> purchaseUnits = response.getPurchaseUnits();
        if (purchaseUnits == null) {
            purchaseUnits = emptyList();
        }

        return purchaseUnits.stream()
                .filter(p -> reservationId.equals(p.getCustomId()))
                .filter(p -> p.getAmount() != null)
                .filter(p -> p.getAmount().getValue() != null)
                .mapToDouble(p -> p.getAmount().getValue())
                .sum();
    }

}
