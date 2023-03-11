package org.olac.reservation.engine.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.olac.reservation.engine.PaymentEngine;
import org.olac.reservation.engine.TemplateEngine;
import org.olac.reservation.resource.NotificationAccess;
import org.olac.reservation.resource.PaymentProcessorAccess;
import org.olac.reservation.resource.ReservationDatastoreAccess;
import org.olac.reservation.resource.TicketDatastoreAccess;
import org.olac.reservation.resource.model.*;
import org.olac.reservation.resource.paypal.model.CreateOrderResponse;
import org.olac.reservation.resource.paypal.model.PurchaseUnit;
import org.olac.reservation.utility.DateTimeUtility;
import org.olac.reservation.utility.SecurityUtility;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentEngineImpl implements PaymentEngine {

    private final TemplateEngine templateEngine;
    private final ReservationDatastoreAccess reservationAccess;
    private final TicketDatastoreAccess ticketAccess;
    private final NotificationAccess notificationAccess;
    private final PaymentProcessorAccess paymentProcessorAccess;
    private final SecurityUtility securityUtility;
    private final DateTimeUtility dateTimeUtility;

    @Override
    public void normalizePaymentsAndStatus(Reservation reservation) {
        // Make sure we document who entered any new payments and when:
        String currentUser = securityUtility.getCurrentUserName();
        reservation.getPayments().stream()
                .filter(p -> isBlank(p.getEnteredBy()))
                .forEach(p -> {
                    p.setEnteredBy(currentUser);
                    p.setCreatedTimestamp(dateTimeUtility.getCurrentTime());
                });

        // Update our amount due...
        Map<String, Double> ticketTypes = ticketAccess.getTicketTypes().stream()
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
    }

    @Override
    public Consumer<Reservation> getPaymentNotificationFunction(Reservation reservation) {
        Consumer<Reservation> sendReservationFunction;

        // Send notifications if the reservation status has changed and notifications were requested
        if (reservationStatusHasChanged(reservation)) {
            switch (reservation.getStatus()) {
                case PENDING_PAYMENT -> sendReservationFunction = r ->
                        notificationAccess.sendNotification(
                                r.getEmail(),
                                "Reservation Payment Reminder",
                                templateEngine.createPaymentInstructions(r));
                case RESERVED -> sendReservationFunction = r -> notificationAccess.sendNotification(
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

        return sendReservationFunction;
    }

    @Override
    public boolean validateAndAddOnlinePayment(Reservation reservation, String paymentProcessorTransactionId) {
        if (reservation == null || isBlank(paymentProcessorTransactionId)) {
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
        Payment payment = buildOnlinePayment(reservation.getReservationId(), paymentProcessorTransactionId, response);
        if (payment == null) {
            return false;
        }

        List<Payment> newPayments = new ArrayList<>(reservation.getPayments());
        newPayments.add(payment);
        reservation.setPayments(newPayments);

        return true;
    }

    @Nullable
    private Payment buildOnlinePayment(String reservationId, String paymentProcessorTransactionId, CreateOrderResponse response) {
        // Search through the transaction for our reservation in order to find the amount paid...
        double amount = getPaymentAmount(reservationId, response);

        if (amount <= 0.0) {
            // Could not find purchase unit with payment for reservation
            return null;
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
        return payment;
    }

    private boolean reservationStatusHasChanged(Reservation reservation) {
        return reservationAccess.getReservation(reservation.getReservationId())
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
