package org.olac.reservation.engine.impl

import org.olac.reservation.resource.TicketDatastoreAccess
import org.olac.reservation.resource.model.*
import org.olac.reservation.utility.impl.FormatUtilityImpl
import org.thymeleaf.ITemplateEngine
import org.thymeleaf.context.Context
import spock.lang.Specification

class ThymeleafTemplateEngineTest extends Specification {

    def static final ADULT_TYPE = "adult-type"
    def static final CHILD_TYPE = "child-type"

    def templateEngine = Mock(ITemplateEngine)
    def ticketDatastoreAccess = Mock(TicketDatastoreAccess) {
        _ * getTicketTypes() >> [
                new TicketType(ADULT_TYPE, "Adult", 50.0),
                new TicketType(CHILD_TYPE, "Child", 20.0)
        ]
    }
    def formattingUtilities = new FormattingUtilities(new FormatUtilityImpl())

    def service = new ThymeleafTemplateEngine(templateEngine, ticketDatastoreAccess, formattingUtilities)

    def reservation = new Reservation(
            ticketCounts: [
                    new TicketCounts(ADULT_TYPE, 3),
                    new TicketCounts(CHILD_TYPE, 2)
            ],
            amountDue: 190.0,
            payments: [
                    new Payment(status: PaymentStatus.SUCCESSFUL, amount: 100.0),
                    new Payment(status: PaymentStatus.FAILED, amount: 90.0),
                    new Payment(status: PaymentStatus.PENDING, amount: 25.0)
            ]
    )

    def isExpectedContext = { Context context ->
        context.getVariable("reservation") == reservation
    }

    def "Creating a payment confirmation notification"() {
        given:
          def expected = "My expected output"

          _ * templateEngine.process("email/payment-confirmation.html", {
              it.getVariable("reservation") == reservation &&
                      it.getVariable("totalCharges") == 190.0 &&
                      it.getVariable("paymentReceived") == 100.0 &&
                      it.getVariable("amountDue") == 90.0 &&
                      it.getVariable("ticketCounts") == [
                      new ThymeleafTemplateEngine.TicketRow(description: "Adult", cost: 50.0, count: 3, total: 150.0),
                      new ThymeleafTemplateEngine.TicketRow(description: "Child", cost: 20.0, count: 2, total: 40.0)
              ] &&
                      it.getVariable("tutil") == formattingUtilities
          }) >> expected

        expect:
          service.createPaymentReceivedConfirmation(reservation) == expected
    }

    def "Creating a payment instructions notification"() {
        given:
          def expected = "My expected output"

          _ * templateEngine.process("email/payment-instructions.html", {
              it.getVariable("reservation") == reservation &&
                      it.getVariable("totalCharges") == 190.0 &&
                      it.getVariable("paymentReceived") == 100.0 &&
                      it.getVariable("amountDue") == 90.0 &&
                      it.getVariable("ticketCounts") == [
                      new ThymeleafTemplateEngine.TicketRow(description: "Adult", cost: 50.0, count: 3, total: 150.0),
                      new ThymeleafTemplateEngine.TicketRow(description: "Child", cost: 20.0, count: 2, total: 40.0)
              ] &&
                      it.getVariable("tutil") == formattingUtilities
          }) >> expected

        expect:
          service.createPaymentInstructions(reservation) == expected
    }

//    def "Creating reservation notification message"() {
//        given:
//          def expected = "Template output"
//
//          def reservationId = 42
//          def amount = 139.39
//          def formattedAmount = '$139.39'
//
//          _ * templateEngine.process("payment-email.html", {
//              it.getVariable("reservationId") == reservationId &&
//                      it.getVariable("amount") == formattedAmount
//          }) >> expected
//
//        expect:
//          expected == service.createReservationNotificationMessage(reservationId, amount)
//    }

}
