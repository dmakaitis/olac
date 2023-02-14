package org.olac.reservation.resource;

import org.olac.reservation.resource.paypal.model.CreateOrderResponse;

import java.util.Optional;

public interface PaymentProcessorAccess {

    Optional<CreateOrderResponse> getOrder(String transactionId);

}
