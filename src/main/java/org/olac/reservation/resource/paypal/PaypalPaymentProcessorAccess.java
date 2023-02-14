package org.olac.reservation.resource.paypal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.olac.reservation.resource.PaymentProcessorAccess;
import org.olac.reservation.resource.paypal.model.CreateOrderResponse;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaypalPaymentProcessorAccess implements PaymentProcessorAccess {

    private final PayPalClient payPalClient;

    @Override
    public Optional<CreateOrderResponse> getOrder(String transactionId) {
        try {
            return Optional.ofNullable(payPalClient.getOrderDetails(transactionId));
        } catch (Exception e) {
            log.error("Failed to load transaction from PayPal: {}", transactionId, e);
            return Optional.empty();
        }
    }

}
