package org.olac.reservation.manager.impl

import org.olac.reservation.config.OlacProperties
import org.olac.reservation.engine.TemplateEngine
import org.olac.reservation.resource.NotificationAccess
import org.olac.reservation.resource.PaymentProcessorAccess
import org.olac.reservation.resource.ReservationDatastoreAccess
import org.olac.reservation.resource.TicketDatastoreAccess
import org.olac.reservation.resource.model.Page
import org.olac.reservation.resource.model.PageRequest
import org.olac.reservation.resource.model.Reservation
import org.olac.reservation.resource.model.TicketType
import org.olac.reservation.utility.SecurityUtility
import spock.lang.Specification
import spock.lang.Unroll

class ReservationManagerImplTest extends Specification {

    def ticketAccess = Mock(TicketDatastoreAccess)
    def reservationAccess = Mock(ReservationDatastoreAccess)
    def templateEngine = Mock(TemplateEngine)
    def notificationAccess = Mock(NotificationAccess)
    def paymentProcessorAccess = Mock(PaymentProcessorAccess)
    def properties = new OlacProperties()
    def securityUtility = Mock(SecurityUtility)

    def service = new ReservationManagerImpl(ticketAccess, reservationAccess, templateEngine, notificationAccess, paymentProcessorAccess, properties, securityUtility)

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

    // TODO: Validate and add payment tests (break out into engine?)

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

    // TODO: Save reservation tests (both variations - break into engine?)

}
