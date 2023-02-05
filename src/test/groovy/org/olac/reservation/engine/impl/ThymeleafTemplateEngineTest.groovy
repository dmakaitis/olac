package org.olac.reservation.engine.impl

import org.thymeleaf.ITemplateEngine
import spock.lang.Specification

class ThymeleafTemplateEngineTest extends Specification {

    def templateEngine = Mock(ITemplateEngine)

    def service = new ThymeleafTemplateEngine(templateEngine)

    def "Creating reservation notificaton message"() {
        given:
          def expected = "Template output"

          def reservationId = 42
          def amount = 139.39
          def formattedAmount = '$139.39'

          _ * templateEngine.process("payment-email.html", {
              it.getVariable("reservationId") == reservationId &&
                      it.getVariable("amount") == formattedAmount
          }) >> expected

        expect:
          expected == service.createReservationNotificationMessage(reservationId, amount)
    }
}
