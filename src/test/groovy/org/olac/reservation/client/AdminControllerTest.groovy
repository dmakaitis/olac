package org.olac.reservation.client

import org.olac.reservation.client.form.TicketTypeForm
import org.olac.reservation.manager.ReservationManager
import org.olac.reservation.resource.model.TicketType
import org.springframework.ui.Model
import spock.lang.Specification

class AdminControllerTest extends Specification {

    def type1 = new TicketType(UUID.randomUUID().toString(), "Type 1", 25.0)
    def type2 = new TicketType(UUID.randomUUID().toString(), "Type 2", 15.0)
    def type3 = new TicketType(UUID.randomUUID().toString(), "Type 3", 75.0)

    def manager = Mock(ReservationManager) {
        _ * getTicketTypes() >> [type1, type2, type3]
    }

    def controller = new AdminController(manager)

    def model = Mock(Model)

    def "The admin page should list any defined ticket types in sorted order"() {
        when:
          def result = controller.getTicketTypes(model)

        then:
          result == "ticketTypes"

          1 * model.addAttribute("ticketTypes", [type3, type1, type2])
          1 * model.addAttribute("form", new TicketTypeForm())
    }

    def "Submitting the ticket type form should save a new ticket type"() {
        given:
          def form = new TicketTypeForm(
                  description: "My New Ticket Type",
                  cost: 42.0
          )

        when:
          def result = controller.addTicketType(form, model)

        then:
          result == "ticketTypes"

          1 * manager.saveTicketType(new TicketType("My New Ticket Type", 42.0))
          1 * model.addAttribute("ticketTypes", [type3, type1, type2])
          1 * model.addAttribute("form", new TicketTypeForm())
    }

}
