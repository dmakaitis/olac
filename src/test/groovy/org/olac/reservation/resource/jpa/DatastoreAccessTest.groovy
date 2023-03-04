package org.olac.reservation.resource.jpa

import org.olac.reservation.resource.jpa.entity.ReservationEntity
import org.olac.reservation.resource.jpa.entity.ReservationTicketsEntity
import org.olac.reservation.resource.jpa.entity.TicketTypeEntity
import org.olac.reservation.resource.jpa.repository.ReservationRepository
import org.olac.reservation.resource.jpa.repository.TicketTypeRepository
import org.olac.reservation.resource.model.Reservation
import org.olac.reservation.resource.model.TicketCounts
import org.olac.reservation.resource.model.TicketType
import org.olac.reservation.utility.AuditUtility
import org.olac.reservation.utility.FormatUtility
import spock.lang.Specification

import java.util.function.Supplier

class DatastoreAccessTest extends Specification {

    def ticketTypeRepository = Mock(TicketTypeRepository)
    def reservationRepository = Mock(ReservationRepository)
    def auditUtility = Mock(AuditUtility)
    def formatUtility = Mock(FormatUtility)
    def codeSupplier = Mock(Supplier)

    def service = new DatastoreAccess(ticketTypeRepository, reservationRepository, auditUtility, formatUtility, codeSupplier)

    def "Get ticket types should return an empty list if no types have been defined"() {
        given:
          ticketTypeRepository.findAll() >> []

        expect:
          service.getTicketTypes() == []
    }

    def "Get ticket types should return a single type if only one type has been defined"() {
        given:
          def code = UUID.randomUUID().toString()
          def description = "Test Type"
          def cost = 15.0

          ticketTypeRepository.findAll() >> [
                  new TicketTypeEntity(
                          id: 1,
                          code: code,
                          description: description,
                          costPerTicket: cost
                  )
          ]

        expect:
          service.getTicketTypes() == [
                  new TicketType(code, description, cost)
          ]
    }

    def "Get ticket types should return all the defined ticket types"() {
        given:
          def type1 = new TicketType(UUID.randomUUID().toString(), "Test Type 1", 15.0)
          def type2 = new TicketType(UUID.randomUUID().toString(), "Test Type 2", 30.0)
          def type3 = new TicketType(UUID.randomUUID().toString(), "Test Type 3", 50.0)
          def type4 = new TicketType(UUID.randomUUID().toString(), "Test Type 4", 75.0)
          def type5 = new TicketType(UUID.randomUUID().toString(), "Test Type 5", 100.0)

          _ * ticketTypeRepository.findAll() >> [
                  new TicketTypeEntity(id: 1, code: type1.code, description: type1.description, costPerTicket: type1.costPerTicket),
                  new TicketTypeEntity(id: 2, code: type2.code, description: type2.description, costPerTicket: type2.costPerTicket),
                  new TicketTypeEntity(id: 3, code: type3.code, description: type3.description, costPerTicket: type3.costPerTicket),
                  new TicketTypeEntity(id: 4, code: type4.code, description: type4.description, costPerTicket: type4.costPerTicket),
                  new TicketTypeEntity(id: 5, code: type5.code, description: type5.description, costPerTicket: type5.costPerTicket)
          ]

        expect:
          service.getTicketTypes() == [
                  type1, type2, type3, type4, type5
          ]
    }

    def "Saving a new ticket type should insert a new record into the database"() {
        given:
          def description = "New Test Type"
          def cost = 42.0

          def ticketType = new TicketType(description, cost)
          def newCode = "abcdefg"

          1 * codeSupplier.get() >> newCode

        when:
          def result = service.saveTicketType(ticketType)

        then:
          result == new TicketType(newCode, description, cost)

          1 * ticketTypeRepository.save(new TicketTypeEntity(
                  code: newCode,
                  description: description,
                  costPerTicket: cost)
          ) >> new TicketTypeEntity(
                  id: 123,
                  code: newCode,
                  description: description,
                  costPerTicket: cost
          )
          0 * ticketTypeRepository.findByCode(_) >> Optional.empty()
    }

    def "Saving an existing ticket type should update the definition and cost"() {
        given:
          def code = "my-ticket-code"
          def newDescription = "New Description"
          def newCost = 56.0

          def ticketType = new TicketType(code, newDescription, newCost)

        when:
          def result = service.saveTicketType(ticketType)

        then:
          result == ticketType

          0 * codeSupplier.get()
          1 * ticketTypeRepository.save(new TicketTypeEntity(
                  id: 415,
                  code: code,
                  description: newDescription,
                  costPerTicket: newCost)
          ) >> new TicketTypeEntity(
                  id: 415,
                  code: code,
                  description: newDescription,
                  costPerTicket: newCost
          )
          1 * ticketTypeRepository.findByCode(code) >> Optional.of(new TicketTypeEntity(
                  id: 415,
                  code: code,
                  description: "Old Description",
                  costPerTicket: 42.0
          ))
    }

    def "Creating a reservation should save it to the database and return the ID"() {
        given:
          def expectedId = 903845

          def reservation = new Reservation(
                  reservationId: 'abcd',
                  firstName: "Darius",
                  lastName: "Makaitis",
                  email: "dmakaitis@gmail.com",
                  phone: "402-880-8442",
                  ticketCounts: [
                          new TicketCounts("type-a", 7),
                          new TicketCounts("type-b", 12)
                  ]
          )

          def typeA = new TicketTypeEntity(
                  id: 13,
                  code: "type-a",
                  description: "Test Type A",
                  costPerTicket: 47.0
          )
          def typeB = new TicketTypeEntity(
                  id: 53,
                  code: "type-b",
                  description: "Test Type B",
                  costPerTicket: 32.0
          )

          _ * ticketTypeRepository.findByCode("type-a") >> Optional.of(typeA)
          _ * ticketTypeRepository.findByCode("type-b") >> Optional.of(typeB)

        when:
          def result = service.createReservation(reservation)

        then:
          result == expectedId

          1 * reservationRepository.save(new ReservationEntity(
                  reservationId: 'abcd',
                  firstName: "Darius",
                  lastName: "Makaitis",
                  email: "dmakaitis@gmail.com",
                  phone: "402-880-8442",
                  amountDue: 0.0,
                  tickets: [
                          new ReservationTicketsEntity(ticketType: typeA, count: 7),
                          new ReservationTicketsEntity(ticketType: typeB, count: 12)
                  ],
                  payments: []
          )) >> new ReservationEntity(
                  id: expectedId,
                  reservationId: 'abcd',
                  firstName: "Darius",
                  lastName: "Makaitis",
                  email: "dmakaitis@gmail.com",
                  phone: "402-880-8442",
                  amountDue: 0.0,
                  tickets: [
                          new ReservationTicketsEntity(id: 63, ticketType: typeA, count: 7),
                          new ReservationTicketsEntity(id: 64, ticketType: typeB, count: 12)
                  ]
          )
    }
}
