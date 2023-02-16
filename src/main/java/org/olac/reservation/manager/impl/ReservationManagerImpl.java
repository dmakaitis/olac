package org.olac.reservation.manager.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.olac.reservation.config.OlacProperties;
import org.olac.reservation.engine.TemplateEngine;
import org.olac.reservation.exception.OlacException;
import org.olac.reservation.manager.ReservationManager;
import org.olac.reservation.resource.NotificationAccess;
import org.olac.reservation.resource.PaymentProcessorAccess;
import org.olac.reservation.resource.ReservationDatastoreAccess;
import org.olac.reservation.resource.TicketDatastoreAccess;
import org.olac.reservation.resource.model.*;
import org.olac.reservation.resource.paypal.model.CreateOrderResponse;
import org.olac.reservation.resource.paypal.model.PurchaseUnit;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationManagerImpl implements ReservationManager {

    private final TicketDatastoreAccess ticketDatastoreAccess;
    private final ReservationDatastoreAccess reservationDatastoreAccess;
    private final TemplateEngine templateEngine;
    private final NotificationAccess notificationAccess;
    private final PaymentProcessorAccess paymentProcessorAccess;
    private final OlacProperties properties;

    @Override
    public TicketType saveTicketType(TicketType ticketType) {
        return ticketDatastoreAccess.saveTicketType(ticketType);
    }

    @Override
    public List<TicketType> getTicketTypes() {
        return ticketDatastoreAccess.getTicketTypes();
    }

    @Override
    public long createReservation(Reservation reservation) {
        if (!areTicketsAvailable(getTicketCount(reservation))) {
            throw new OlacException("Temporary exception");
        }

        return reservationDatastoreAccess.createReservation(reservation);
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
                .build();

        reservationDatastoreAccess.addPaymentToReservation(reservationId, payment);
        reservationDatastoreAccess.getReservation(reservationId).ifPresent(r -> {
            String message = templateEngine.createPaymentReceivedConfirmation(r);
            notificationAccess.sentNotification(r.getEmail(), "Reservation Confirmation", message);
        });

        return true;
    }

    @Override
    public void sendPaymentReminder(String reservationId) {
        if (isBlank(reservationId)) {
            return;
        }

        Optional<Reservation> reservation = reservationDatastoreAccess.getReservation(reservationId);
        reservation.ifPresent(r -> {
            String message = templateEngine.createPaymentInstructions(r);
            notificationAccess.sentNotification(r.getEmail(), "Reservation Confirmation", message);
        });
    }

    @Override
    public void deleteTicketType(String ticketTypeCode) {
        ticketDatastoreAccess.deleteTicketType(ticketTypeCode);
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
        reservationDatastoreAccess.addPaymentToReservation(reservationId, payment);
    }

    private long getTicketCount(Reservation reservation) {
        return reservation.getTicketCounts().stream()
                .mapToLong(TicketCounts::getCount)
                .sum();
    }

}
