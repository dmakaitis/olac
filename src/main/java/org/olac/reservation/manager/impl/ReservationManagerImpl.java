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

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
    public List<Reservation> getReservations() {
        return reservationDatastoreAccess.getReservations();
    }

    @Override
    public Optional<Reservation> getReservation(String reservationId) {
        return reservationDatastoreAccess.getReservation(reservationId);
    }

    @Override
    public boolean areTicketsAvailable(long requestedTicketCount) {
        long availableTickets = properties.getMaxTickets() - reservationDatastoreAccess.getReservations().stream()
                .map(Reservation::getTicketCounts)
                .flatMap(List::stream)
                .mapToLong(TicketCounts::getCount)
                .sum();

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
    public Reservation saveReservation(Reservation reservation) {
        return saveReservation(reservation, true);
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

        // Send notifications if the reservation status has changed and notifications were requested
        if (sendNotification && reservationStatusHasChanged(reservation)) {
            switch (reservation.getStatus()) {
                case PENDING_PAYMENT:
                    notificationAccess.sentNotification(
                            reservation.getEmail(),
                            "Reservation Payment Reminder",
                            templateEngine.createPaymentInstructions(reservation));
                    break;
                case RESERVED:
                    notificationAccess.sentNotification(
                            reservation.getEmail(),
                            "Reservation Confirmation",
                            templateEngine.createPaymentReceivedConfirmation(reservation));
                    break;
                default:
                    log.debug("No notification sent for current reservation status: {}", reservation.getStatus());
            }
        }

        return reservationDatastoreAccess.saveReservation(reservation);
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

    @Override
    public void addPayment(String reservationId, Payment payment) {
        payment.setEnteredBy(securityUtility.getCurrentUserName());
        reservationDatastoreAccess.addPaymentToReservation(reservationId, payment);
    }

}
