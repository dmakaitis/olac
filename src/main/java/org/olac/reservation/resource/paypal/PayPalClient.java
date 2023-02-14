package org.olac.reservation.resource.paypal;


import feign.Param;
import feign.RequestLine;
import org.olac.reservation.resource.paypal.model.CreateOrderResponse;

public interface PayPalClient {

    @RequestLine("GET /v2/checkout/orders/{id}")
    CreateOrderResponse getOrderDetails(@Param("id") String id);

}
