package org.olac.reservation.engine;

import org.olac.reservation.resource.model.Reservation;

import java.util.function.Consumer;

public interface PaymentEngine {

    void normalizePaymentsAndStatus(Reservation reservation);

    Consumer<Reservation> getPaymentNotificationFunction(Reservation reservation);

    boolean validateAndAddOnlinePayment(Reservation reservation, String paymentProcessorTransactionId);

}
