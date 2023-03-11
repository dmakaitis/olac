package org.olac.reservation.engine.impl

import org.olac.reservation.engine.TemplateEngine
import org.olac.reservation.resource.NotificationAccess
import org.olac.reservation.resource.PaymentProcessorAccess
import org.olac.reservation.resource.ReservationDatastoreAccess
import org.olac.reservation.resource.TicketDatastoreAccess
import org.olac.reservation.resource.model.*
import org.olac.reservation.resource.paypal.model.AmountWithBreakdown
import org.olac.reservation.resource.paypal.model.CreateOrderResponse
import org.olac.reservation.resource.paypal.model.PurchaseUnit
import org.olac.reservation.utility.DateTimeUtility
import org.olac.reservation.utility.SecurityUtility
import spock.lang.Specification
import spock.lang.Unroll

class PaymentEngineImplTest extends Specification {

    def static final ADULT_TYPE = new TicketType("type-adult", "Adult", 50.0)
    def static final CHILD_TYPE = new TicketType("type-child", "Child", 20.0)

    def static final CURRENT_USER = "current-user-name"

    def static final EMAIL = "nobody@nowhere.com"

    def now = new Date()

    def templateEngine = Mock(TemplateEngine)
    def reservationAccess = Mock(ReservationDatastoreAccess)
    def ticketAccess = Mock(TicketDatastoreAccess) {
        _ * getTicketTypes() >> [ADULT_TYPE, CHILD_TYPE]
    }
    def notificationAccess = Mock(NotificationAccess)
    def paymentProcessorAccess = Mock(PaymentProcessorAccess)
    def securityUtility = Mock(SecurityUtility) {
        _ * getCurrentUserName() >> CURRENT_USER
    }
    def dateTimeUtility = Mock(DateTimeUtility) {
        _ * getCurrentTime() >> now
    }

    def engine = new PaymentEngineImpl(templateEngine, reservationAccess, ticketAccess, notificationAccess, paymentProcessorAccess, securityUtility, dateTimeUtility)

    @Unroll
    def "Normalizing payments should ensure all payments have all auto-set fields populated and the reservation status and amount due has been updated"() {
        given:
          def oldDate = new Date(124097245L)

          def reservation = new Reservation(
                  status: oldStatus,
                  amountDue: 0.0,
                  ticketCounts: [
                          new TicketCounts(ADULT_TYPE.code, 3),
                          new TicketCounts(CHILD_TYPE.code, 2)
                  ],
                  payments: [
                          new Payment(amount: amountPaid, status: PaymentStatus.SUCCESSFUL, method: PaymentMethod.ONLINE),
                          new Payment(amount: 10.0, status: PaymentStatus.SUCCESSFUL, method: PaymentMethod.ONLINE, enteredBy: "someone-else", createdTimestamp: oldDate),
                          new Payment(amount: 1000.0, status: PaymentStatus.FAILED, method: PaymentMethod.ONLINE, enteredBy: "someone-else", createdTimestamp: oldDate),
                          new Payment(amount: 1000.0, status: PaymentStatus.PENDING, method: PaymentMethod.ONLINE, enteredBy: "someone-else", createdTimestamp: oldDate),
                  ],
          )

        when:
          engine.normalizePaymentsAndStatus(reservation)

        then:
          reservation.amountDue == 190.0 as double
          reservation.status == expectedStatus

          reservation.payments[0].createdTimestamp == now
          reservation.payments[0].enteredBy == CURRENT_USER

          reservation.payments[1].createdTimestamp == oldDate
          reservation.payments[1].enteredBy == "someone-else"

          reservation.payments[2].createdTimestamp == oldDate
          reservation.payments[2].enteredBy == "someone-else"

          reservation.payments[3].createdTimestamp == oldDate
          reservation.payments[3].enteredBy == "someone-else"

        where:
          oldStatus                         | amountPaid || expectedStatus
          ReservationStatus.PENDING_PAYMENT | 190.0      || ReservationStatus.RESERVED
          ReservationStatus.RESERVED        | 100.0      || ReservationStatus.PENDING_PAYMENT
          ReservationStatus.CHECKED_IN      | 100.0      || ReservationStatus.CHECKED_IN
          ReservationStatus.CANCELLED       | 100.0      || ReservationStatus.CANCELLED
    }

    def "If the reservation status has not changed, no notification should be sent"() {
        given:
          def reservation = new Reservation(reservationId: "my-reservation-id", email: EMAIL, status: ReservationStatus.RESERVED)

        when:
          engine.getPaymentNotificationFunction(reservation)
                  .accept(reservation)

        then:
          1 * reservationAccess.getReservation(reservation.reservationId) >> Optional.of(reservation)

          0 * templateEngine._
          0 * notificationAccess._
    }

    def "If the reservation status has changed to RESERVED, then send a payment confirmation"() {
        given:
          def reservation = new Reservation(reservationId: "my-reservation-id", email: EMAIL, status: ReservationStatus.RESERVED)
          def oldReservation = new Reservation(reservationId: "my-reservation-id", email: EMAIL, status: ReservationStatus.PENDING_PAYMENT)

          def message = "my-expected-message"

        when:
          engine.getPaymentNotificationFunction(reservation)
                  .accept(reservation)

        then:
          1 * reservationAccess.getReservation(reservation.reservationId) >> Optional.of(oldReservation)

          1 * templateEngine.createPaymentReceivedConfirmation(reservation) >> message
          1 * notificationAccess.sendNotification(EMAIL, 'Reservation Confirmation', message)
    }

    def "If the reservation status has changed to PENDING_PAYMENT, then send payment instructions"() {
        given:
          def reservation = new Reservation(reservationId: "my-reservation-id", email: EMAIL, status: ReservationStatus.PENDING_PAYMENT)
          def oldReservation = new Reservation(reservationId: "my-reservation-id", email: EMAIL, status: ReservationStatus.RESERVED)

          def message = "my-expected-message"

        when:
          engine.getPaymentNotificationFunction(reservation)
                  .accept(reservation)

        then:
          1 * reservationAccess.getReservation(reservation.reservationId) >> Optional.of(oldReservation)

          1 * templateEngine.createPaymentInstructions(reservation) >> message
          1 * notificationAccess.sendNotification(EMAIL, 'Reservation Payment Reminder', message)
    }

    def "If the reservation status has changed to CHECKED_IN, then send nothing"() {
        given:
          def reservation = new Reservation(reservationId: "my-reservation-id", email: EMAIL, status: ReservationStatus.CHECKED_IN)
          def oldReservation = new Reservation(reservationId: "my-reservation-id", email: EMAIL, status: ReservationStatus.RESERVED)

          def message = "my-expected-message"

        when:
          engine.getPaymentNotificationFunction(reservation)
                  .accept(reservation)

        then:
          1 * reservationAccess.getReservation(reservation.reservationId) >> Optional.of(oldReservation)

          0 * templateEngine._
          0 * notificationAccess._
    }

    def "If the reservation status has changed to CANCELLED, then send nothing"() {
        given:
          def reservation = new Reservation(reservationId: "my-reservation-id", email: EMAIL, status: ReservationStatus.CANCELLED)
          def oldReservation = new Reservation(reservationId: "my-reservation-id", email: EMAIL, status: ReservationStatus.RESERVED)

          def message = "my-expected-message"

        when:
          engine.getPaymentNotificationFunction(reservation)
                  .accept(reservation)

        then:
          1 * reservationAccess.getReservation(reservation.reservationId) >> Optional.of(oldReservation)

          0 * templateEngine._
          0 * notificationAccess._
    }

    @Unroll
    def "For a brand new reservation, send the appropriate notification"() {
        given:
          def reservation = new Reservation(reservationId: "my-reservation-id", email: EMAIL, status: status)

          def message = "my-expected-message"

        when:
          engine.getPaymentNotificationFunction(reservation)
                  .accept(reservation)

        then:
          1 * reservationAccess.getReservation(reservation.reservationId) >> Optional.empty()

          instructions * templateEngine.createPaymentInstructions(reservation) >> message
          confirmation * templateEngine.createPaymentReceivedConfirmation(reservation) >> message
          notification * notificationAccess.sendNotification(EMAIL, subject, message)

        where:
          status                            || instructions | confirmation | subject                        | notification
          ReservationStatus.PENDING_PAYMENT || 1            | 0            | 'Reservation Payment Reminder' | 1
          ReservationStatus.RESERVED        || 0            | 1            | 'Reservation Confirmation'     | 1
          ReservationStatus.CHECKED_IN      || 0            | 0            | ''                             | 0
          ReservationStatus.CANCELLED       || 0            | 0            | ''                             | 0
    }

    def "Can not add a PayPal payment without a reservation"() {
        expect:
          !engine.validateAndAddOnlinePayment(null, 'payment-id')
    }

    def "Can not add a PayPal payment without a payment ID"() {
        expect:
          !engine.validateAndAddOnlinePayment(new Reservation(), '')
    }

    def "Can not add a PayPal payment if we can not validate the payment Id"() {
        given:
          _ * paymentProcessorAccess.getOrder(_) >> Optional.empty()

        expect:
          !engine.validateAndAddOnlinePayment(new Reservation(), 'payment-id')
    }

    def "Can not add a PayPal payment if there are no purchase units on the transaction"() {
        given:
          def reservation = new Reservation(
                  reservationId: "test-reservation-id"
          )
          def paymentId = "test-payment-id"

          def response = new CreateOrderResponse(
                  id: paymentId,
                  createTime: new Date()
          )
          _ * paymentProcessorAccess.getOrder(paymentId) >> Optional.of(response)

        when:
          def successful = engine.validateAndAddOnlinePayment(reservation, paymentId)

        then:
          !successful
          reservation.payments.empty
    }

    def "Can not add a PayPal payment if the payment can not be found on the transaction"() {
        given:
          def reservation = new Reservation(
                  reservationId: "test-reservation-id"
          )
          def paymentId = "test-payment-id"

          def expectedAmount = 270.0

          def response = new CreateOrderResponse(
                  id: paymentId,
                  createTime: new Date(),
                  purchaseUnits: [new PurchaseUnit(
                          customId: "some-random-value",
                          amount: new AmountWithBreakdown(value: expectedAmount)
                  )]
          )
          _ * paymentProcessorAccess.getOrder(paymentId) >> Optional.of(response)

        when:
          def successful = engine.validateAndAddOnlinePayment(reservation, paymentId)

        then:
          !successful
          reservation.payments.empty
    }

    def "Adding a PayPal payment to a reservation"() {
        given:
          def reservation = new Reservation(
                  reservationId: "test-reservation-id"
          )
          def paymentId = "test-payment-id"

          def expectedAmount = 270.0

          def response = new CreateOrderResponse(
                  id: paymentId,
                  createTime: new Date(),
                  purchaseUnits: [new PurchaseUnit(
                          customId: reservation.reservationId,
                          amount: new AmountWithBreakdown(value: expectedAmount)
                  )]
          )
          _ * paymentProcessorAccess.getOrder(paymentId) >> Optional.of(response)

        when:
          def successful = engine.validateAndAddOnlinePayment(reservation, paymentId)

        then:
          successful

          reservation.payments.size() == 1
          reservation.payments[0] == new Payment(
                  amount: expectedAmount,
                  status: PaymentStatus.SUCCESSFUL,
                  method: PaymentMethod.ONLINE,
                  notes: "PayPal Transaction ID: ${paymentId}",
                  enteredBy: CURRENT_USER,
                  createdTimestamp: response.createTime
          )
    }

}
