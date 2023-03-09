package org.olac.reservation.resource.paypal

import org.olac.reservation.resource.paypal.model.CreateOrderResponse
import spock.lang.Specification

class PaypalPaymentProcessorAccessTest extends Specification {

    def payPayClient = Mock(PayPalClient)

    def service = new PaypalPaymentProcessorAccess(payPayClient)

    def "If the transaction ID could not be found, an empty optional should be returned"() {
        given:
          _ * payPayClient.getOrderDetails(_) >> null

        expect:
          service.getOrder("my-transaction-id") == Optional.empty()
    }

    def "If the PayPal client throws an exception, an empty optional should be returned"() {
        given:
          _ * payPayClient.getOrderDetails(_) >> { throw new RuntimeException("Something bad happened") }

        expect:
          service.getOrder("my-transaction-id") == Optional.empty()
    }

    def "If the PayPal client returns a result, then it should be passed on to the caller"() {
        given:
          def transactionId = "my-transaction-id"
          def expected = Optional.of(new CreateOrderResponse(id: transactionId))

          _ * payPayClient.getOrderDetails(transactionId) >> expected.get()

        expect:
          service.getOrder(transactionId) == expected
    }
}
