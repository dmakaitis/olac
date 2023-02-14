package org.olac.reservation.client

import jakarta.servlet.http.HttpSession
import org.olac.reservation.client.form.ReservationForm
import org.olac.reservation.client.form.TicketTypeCount
import org.olac.reservation.manager.ReservationManager
import org.olac.reservation.resource.model.TicketType
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import spock.lang.Specification

class PublicControllerTest extends Specification {

    def type1 = new TicketType(UUID.randomUUID().toString(), "Type 1", 25.0)
    def type2 = new TicketType(UUID.randomUUID().toString(), "Type 2", 15.0)
    def type3 = new TicketType(UUID.randomUUID().toString(), "Type 3", 75.0)

    def manager = Mock(ReservationManager) {
        _ * getTicketTypes() >> [type1, type2, type3]
    }

    def controller = new PublicController(manager)

    def model = Mock(Model)
    def session = Mock(HttpSession)

    def "The home page controller should place a form containing available tickets into the model"() {
        given:
          def expectedForm = new ReservationForm(
                  ticketTypeCounts: [
                          new TicketTypeCount(type3.code, type3.description, '$75.00', 0, '$0.00'),
                          new TicketTypeCount(type1.code, type1.description, '$25.00', 0, '$0.00'),
                          new TicketTypeCount(type2.code, type2.description, '$15.00', 0, '$0.00'),
                  ]
          )

        when:
          def result = controller.tickets(expectedForm, session)

        then:
          result == "tickets"
    }

    def "Posting a new reservation should save it to the database"() {
        given:
          def model = Mock(Model)
          def bindingResult = Mock(BindingResult) {
              _ * hasErrors() >> false
          }

          def form = new ReservationForm(
                  firstName: "Darius",
                  lastName: "Makaitis",
                  email: "dmakaitis@gmail.com",
                  phone: "402-880-8442",
                  ticketTypeCounts: [
                          new TicketTypeCount(typeCode: type1.code, count: 5),
                          new TicketTypeCount(typeCode: type2.code, count: 3)
                  ]
          )

        when:
          def result = controller.postReservation(form, bindingResult, model, session)

        then:
          result == "confirmation"

          1 * manager.areTicketsAvailable(8) >> true
          1 * session.setAttribute("reservationForm", form)
    }
}
