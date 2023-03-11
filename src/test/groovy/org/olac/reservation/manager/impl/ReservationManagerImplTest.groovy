package org.olac.reservation.manager.impl

import org.olac.reservation.config.OlacProperties
import org.olac.reservation.engine.PaymentEngine
import org.olac.reservation.resource.ReservationDatastoreAccess
import org.olac.reservation.resource.TicketDatastoreAccess
import org.olac.reservation.resource.model.*
import org.olac.reservation.utility.SecurityUtility
import spock.lang.Specification
import spock.lang.Unroll

import java.util.function.Consumer

class ReservationManagerImplTest extends Specification {

    def paymentEngine = Mock(PaymentEngine)
    def ticketAccess = Mock(TicketDatastoreAccess)
    def reservationAccess = Mock(ReservationDatastoreAccess)
    def securityUtility = Mock(SecurityUtility)
    def properties = new OlacProperties()

    def service = new ReservationManagerImpl(paymentEngine, ticketAccess, reservationAccess, securityUtility, properties)

    def "Saving a ticket type"() {
        given:
          def ticketType = new TicketType(code: "type-code", description: "Type Description", costPerTicket: 100.0)
          def expected = new TicketType(code: "type-code", description: "Type Description", costPerTicket: 100.0)

        when:
          def result = service.saveTicketType(ticketType)

        then:
          result == ticketType

          1 * ticketAccess.saveTicketType(ticketType) >> expected
    }

    def "Retrieving ticket type"() {
        given:
          def expected = [
                  new TicketType(code: "type-code-1", description: "Type Description A", costPerTicket: 100.0),
                  new TicketType(code: "type-code-2", description: "Type Description B", costPerTicket: 50.0),
                  new TicketType(code: "type-code-3", description: "Type Description C", costPerTicket: 20.0)
          ]

          _ * ticketAccess.getTicketTypes() >> expected

        expect:
          service.getTicketTypes() == expected
    }

    def "Retrieving a page of reservations"() {
        given:
          def filter = "my-filter-string"
          def pageRequest = new PageRequest(3, 5, "id", false)

          def expected = new Page<Reservation>(3, 3, 5, 18, [
                  new Reservation(reservationId: "reservation-16"),
                  new Reservation(reservationId: "reservation-17"),
                  new Reservation(reservationId: "reservation-18")
          ], "id", false)

          _ * reservationAccess.getReservations(filter, pageRequest) >> expected

        expect:
          service.getReservations(filter, pageRequest) == expected
    }

    @Unroll
    def "Check ticket availability"() {
        given:
          properties.setMaxTickets(maxTickets)
          _ * reservationAccess.getTotalTicketsReserved() >> totalReserved

        expect:
          service.areTicketsAvailable(requested) == expected

        where:
          maxTickets | totalReserved | requested || expected
          250        | 20            | 10        || true
          250        | 247           | 4         || false
          25         | 20            | 10        || false
          300        | 247           | 5         || true
          250        | 247           | 3         || true
    }

    def "Validate and add payment"() {
        given:
          def reservationId = "reservation-id"
          def paymentId = "payment-id"

          def reservation = new Reservation(
                  reservationId: reservationId,
                  status: ReservationStatus.PENDING_PAYMENT
          )
          _ * reservationAccess.getReservation(reservationId) >> Optional.of(reservation)

          def notificationFunction = Mock(Consumer)
          _ * paymentEngine.getPaymentNotificationFunction(reservation) >> notificationFunction

        when:
          def result = service.validateAndAddPayment(reservationId, paymentId)

        then:
          result

          1 * paymentEngine.validateAndAddOnlinePayment(reservation, paymentId) >> true

        then:
          1 * paymentEngine.normalizePaymentsAndStatus(reservation)

        then:
          1 * reservationAccess.saveReservation(reservation) >> reservation

        then:
          1 * notificationFunction.accept(reservation)
    }

    def "Delete ticket type"() {
        given:
          def ticketTypeCode = "type-to-delete"

        when:
          service.deleteTicketType(ticketTypeCode)

        then:
          1 * ticketAccess.deleteTicketType(ticketTypeCode)
    }

    def "Delete reservation"() {
        given:
          def reservationId = "reservation-to-delete"

        when:
          service.deleteReservation(reservationId)

        then:
          1 * reservationAccess.deleteReservation(reservationId)
    }

    @Unroll
    def "Saving a reservation"() {
        given:
          def reservation = new Reservation(
                  reservationId: "my-reservation"
          )
          def expected = new Reservation(
                  id: 1234,
                  reservationId: "my-reservation",
          )

          def notificationFunction = Mock(Consumer)
          _ * paymentEngine.getPaymentNotificationFunction(reservation) >> notificationFunction

        when:
          def result = service.saveReservation(reservation, sendNotification)

        then:
          result == expected

          1 * paymentEngine.normalizePaymentsAndStatus(reservation)

        then:
          1 * reservationAccess.saveReservation(reservation) >> expected

        then:
          sendNotificationCount * notificationFunction.accept(expected)

        where:
          sendNotification || sendNotificationCount
          true             || 1
          false            || 0
    }

    def "Saving a reservation as an administrator should work normally"() {
        given:
          def reservation = new Reservation(
                  reservationId: "my-reservation"
          )
          def expected = new Reservation(
                  id: 1234,
                  reservationId: "my-reservation",
          )

          def notificationFunction = Mock(Consumer)
          _ * paymentEngine.getPaymentNotificationFunction(reservation) >> notificationFunction

          _ * securityUtility.isCurrentUserAdmin() >> true

        when:
          def result = service.saveReservation(reservation)

        then:
          result == expected

          0 * reservationAccess.getReservation(_)

        then:
          1 * paymentEngine.normalizePaymentsAndStatus(reservation)

        then:
          1 * reservationAccess.saveReservation(reservation) >> expected

        then:
          1 * notificationFunction.accept(expected)
    }

    def "Saving a reservation as an non-administrator should work normally if the reservation does not already exist"() {
        given:
          def reservation = new Reservation(
                  reservationId: "my-reservation"
          )
          def expected = new Reservation(
                  id: 1234,
                  reservationId: "my-reservation",
          )

          def notificationFunction = Mock(Consumer)
          _ * paymentEngine.getPaymentNotificationFunction(reservation) >> notificationFunction

          _ * securityUtility.isCurrentUserAdmin() >> false

        when:
          def result = service.saveReservation(reservation)

        then:
          result == expected

          1 * reservationAccess.getReservation(reservation.reservationId) >> Optional.empty()

        then:
          1 * paymentEngine.normalizePaymentsAndStatus(reservation)

        then:
          1 * reservationAccess.saveReservation(reservation) >> expected

        then:
          1 * notificationFunction.accept(expected)
    }

    def "Saving a reservation as an non-administrator should only add the payments if the reservation already exists"() {
        given:
          def reservation = new Reservation(
                  reservationId: "my-reservation",
                  firstName: "NewFirstName",
                  lastName: "NewLastName",
                  payments: [
                          new Payment(id: 123, amount: 50.0, status: PaymentStatus.SUCCESSFUL, method: PaymentMethod.ONLINE),
                          new Payment(amount: 100.0, status: PaymentStatus.SUCCESSFUL, method: PaymentMethod.CHECK)
                  ]
          )
          def oldReservation = new Reservation(
                  id: 1234,
                  reservationId: "my-reservation",
                  firstName: "First",
                  lastName: "Last",
                  payments: [
                          new Payment(id: 123, amount: 50.0, status: PaymentStatus.SUCCESSFUL, method: PaymentMethod.ONLINE)
                  ]
          )
          def expected = new Reservation(
                  id: 1234,
                  reservationId: "my-reservation",
                  firstName: "First",
                  lastName: "Last",
                  payments: [
                          new Payment(id: 123, amount: 50.0, status: PaymentStatus.SUCCESSFUL, method: PaymentMethod.ONLINE),
                          new Payment(amount: 100.0, status: PaymentStatus.SUCCESSFUL, method: PaymentMethod.CHECK)
                  ]
          )

          def notificationFunction = Mock(Consumer)
          _ * paymentEngine.getPaymentNotificationFunction(expected) >> notificationFunction

          _ * securityUtility.isCurrentUserAdmin() >> false

        when:
          def result = service.saveReservation(reservation)

        then:
          result == expected

          1 * reservationAccess.getReservation(reservation.reservationId) >> Optional.of(oldReservation)

        then:
          1 * paymentEngine.normalizePaymentsAndStatus(expected)

        then:
          1 * reservationAccess.saveReservation(expected) >> expected

        then:
          1 * notificationFunction.accept(expected)
    }

    def "Saving a reservation as an non-administrator should do nothing if the reservation already exists and no payments were added"() {
        given:
          def reservation = new Reservation(
                  reservationId: "my-reservation",
                  firstName: "NewFirstName",
                  lastName: "NewLastName",
                  payments: [
                          new Payment(id: 123, amount: 50.0, status: PaymentStatus.SUCCESSFUL, method: PaymentMethod.ONLINE),
                  ]
          )
          def oldReservation = new Reservation(
                  id: 1234,
                  reservationId: "my-reservation",
                  firstName: "First",
                  lastName: "Last",
                  payments: [
                          new Payment(id: 123, amount: 50.0, status: PaymentStatus.SUCCESSFUL, method: PaymentMethod.ONLINE)
                  ]
          )
          def expected = oldReservation

          def notificationFunction = Mock(Consumer)
          _ * paymentEngine.getPaymentNotificationFunction(expected) >> notificationFunction

          _ * securityUtility.isCurrentUserAdmin() >> false

        when:
          def result = service.saveReservation(reservation)

        then:
          result == expected

          1 * reservationAccess.getReservation(reservation.reservationId) >> Optional.of(oldReservation)

          0 * paymentEngine.normalizePaymentsAndStatus(expected)
          0 * reservationAccess.saveReservation(expected) >> expected
          0 * notificationFunction.accept(expected)
    }

}
