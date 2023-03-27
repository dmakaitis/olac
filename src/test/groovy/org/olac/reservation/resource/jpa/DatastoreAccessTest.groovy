package org.olac.reservation.resource.jpa

import org.olac.reservation.resource.jpa.entity.PaymentEntity
import org.olac.reservation.resource.jpa.entity.ReservationEntity
import org.olac.reservation.resource.jpa.entity.ReservationTicketsEntity
import org.olac.reservation.resource.jpa.entity.TicketTypeEntity
import org.olac.reservation.resource.jpa.repository.ReservationRepository
import org.olac.reservation.resource.jpa.repository.TicketTypeRepository
import org.olac.reservation.resource.jpa.specification.ReservationSpecification
import org.olac.reservation.resource.model.*
import org.olac.reservation.utility.AuditUtility
import org.olac.reservation.utility.DateTimeUtility
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Sort
import spock.lang.Specification
import spock.lang.Unroll

import java.util.concurrent.TimeUnit
import java.util.function.Supplier

class DatastoreAccessTest extends Specification {

    def ticketTypeRepository = Mock(TicketTypeRepository)
    def reservationRepository = Mock(ReservationRepository)
    def auditUtility = Mock(AuditUtility)
    def dateTimeUtility = Mock(DateTimeUtility)
    def codeSupplier = Mock(Supplier)

    def service = new DatastoreAccess(ticketTypeRepository, reservationRepository, auditUtility, dateTimeUtility, codeSupplier)

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

    def "Deleting a ticket type should remove it from the database if it exists"() {
        given:
          def typeCode = "type1"
          def typeEntity = new TicketTypeEntity(id: 3, code: typeCode, costPerTicket: 50.0, description: "My test type")

          _ * ticketTypeRepository.findByCode(typeCode) >> Optional.of(typeEntity)

        when:
          service.deleteTicketType(typeCode)

        then:
          1 * ticketTypeRepository.delete(typeEntity)
    }

    def "Deleting a ticket type should do nothing if it doesn't exist"() {
        given:
          _ * ticketTypeRepository.findByCode(_) >> Optional.empty()

        when:
          service.deleteTicketType("some-type")

        then:
          0 * ticketTypeRepository.delete(_)
    }

    def "Deleting a reservation giving its ID should be completed using a specification"() {
        given:
          def reservationId = "my-reservation-id"
          def expectedSpecification = ReservationSpecification.withReservationId(reservationId)

        when:
          service.deleteReservation(reservationId)

        then:
          1 * reservationRepository.delete(expectedSpecification)
    }

    def "We should be able to retrieve a page of reservations"() {
        given:
          def filter = "11"
          def pageRequest = new PageRequest(page: 0, itemsPerPage: 5, sortBy: "lastName", descending: false)

          def expectedData = [
                  new Reservation(id: 1, reservationId: "test-reservation-id-1"),
                  new Reservation(id: 2, reservationId: "test-reservation-id-2"),
                  new Reservation(id: 3, reservationId: "test-reservation-id-3"),
          ]

          def total = 23

          def pageable = org.springframework.data.domain.PageRequest.of(pageRequest.getPage(), pageRequest.getItemsPerPage(), Sort.Direction.ASC, pageRequest.sortBy);
          def content = expectedData.collect { new ReservationEntity(id: it.id, reservationId: it.reservationId) }

          _ * reservationRepository.findAll(_, pageable) >> new PageImpl<ReservationEntity>(content, pageable, total)

        when:
          def result = service.getReservations(filter, pageRequest)

        then:
          result.pageNumber == pageRequest.page
          result.itemsPerPage == pageRequest.itemsPerPage
          result.sortBy == pageRequest.sortBy
          result.descending == pageRequest.descending
          result.totalItems == total

          result.data == expectedData
    }

    @Unroll
    def "The total number of reserved tickets should be correctly calculated"() {
        given:
          _ * reservationRepository.findAll() >> reservations

        expect:
          service.getReservationsStats().ticketsReserved == expected

        where:
          expected || reservations
          0        || []
          0        || [rtc(ReservationStatus.PENDING_PAYMENT, 12, [2, 3])]
          3        || [rtc(ReservationStatus.PENDING_PAYMENT, 3, [1, 2])]
          4        || [rtc(ReservationStatus.RESERVED, 12, [3, 1])]
          5        || [rtc(ReservationStatus.RESERVED, 3, [2, 3])]
          6        || [rtc(ReservationStatus.RESERVED, 3, [2, 4]), rtc(ReservationStatus.PENDING_PAYMENT, 12, [4, 2])]
          7        || [rtc(ReservationStatus.RESERVED, 3, [2, 1]), rtc(ReservationStatus.PENDING_PAYMENT, 6, [1, 3])]
          0        || [rtc(ReservationStatus.CANCELLED, 4, [5, 7])]
          9        || [rtc(ReservationStatus.CHECKED_IN, 18, [2, 7])]
    }

    def "We should be able to retrieve a reservation by reservation ID"() {
        given:
          def reservationId = "my-reservation-id"
          def expected = Optional.of(new Reservation(
                  id: 123,
                  reservationId: reservationId,
                  ticketCounts: [],
                  payments: []
          ))
          def entity = expected.map {
              new ReservationEntity(
                      id: it.id,
                      reservationId: it.reservationId,
                      tickets: [],
                      payments: []
              )
          }

          _ * reservationRepository.findOne(ReservationSpecification.withReservationId(reservationId)) >> entity

        expect:
          service.getReservation(reservationId) == expected
    }

    def "An empty optional should be returned if we try to retrieve a reservation that doesn't exist"() {
        given:
          _ * reservationRepository.findOne(_) >> Optional.empty()

        expect:
          service.getReservation("some-random-id") == Optional.empty()
    }

    def "Trying to retrieve a reservation with no ID should return nothing"() {
        when:
          def result = service.getReservation("")

        then:
          result == Optional.empty()
          0 * reservationRepository._
    }

    def "Saving a new reservation should write it to the database"() {
        given:
          def reservation = new Reservation(reservationId: "my-reservation-id", firstName: "Bob", lastName: "Smith", email: "bob@gmail.com")
          def expectedId = 255

        when:
          def result = service.saveReservation(reservation)

        then:
          1 * reservationRepository.save(new ReservationEntity(reservationId: reservation.reservationId, firstName: reservation.firstName, lastName: reservation.lastName, email: reservation.email, tickets: [], payments: [])) >>
                  new ReservationEntity(id: expectedId, reservationId: reservation.reservationId, firstName: reservation.firstName, lastName: reservation.lastName, email: reservation.email, tickets: [], payments: [])

          result.id == expectedId
          result.reservationId == reservation.reservationId
          result.firstName == reservation.firstName
          result.lastName == reservation.lastName
          result.email == reservation.email
    }

    def "Saving an existing reservation should update it in the database"() {
        given:
          def reservation = new Reservation(id: 123, reservationId: "my-reservation-id", firstName: "Bob", lastName: "Smith", email: "bob@gmail.com")

          def oldEntity = new ReservationEntity(id: reservation.id, reservationId: reservation.reservationId, firstName: "Bobby", lastName: "Smithy", email: "bsmith@gmail.com", tickets: [], payments: [])
          def newEntity = new ReservationEntity(id: reservation.id, reservationId: reservation.reservationId, firstName: reservation.firstName, lastName: reservation.lastName, email: reservation.email, tickets: [], payments: [])

        when:
          def result = service.saveReservation(reservation)

        then:
          1 * reservationRepository.findById(reservation.id) >> Optional.of(oldEntity)
          1 * reservationRepository.save(newEntity) >> newEntity

          result.id == reservation.id
          result.reservationId == reservation.reservationId
          result.firstName == reservation.firstName
          result.lastName == reservation.lastName
          result.email == reservation.email
    }

    def "Adding a payment to an existing reservation should update it in the database"() {
        given:
          def reservation = new Reservation(id: 123, reservationId: "my-reservation-id", firstName: "Bob", lastName: "Smith", email: "bob@gmail.com", payments: [new Payment(amount: 100.0, status: PaymentStatus.SUCCESSFUL, method: PaymentMethod.CHECK)])

          def oldEntity = new ReservationEntity(
                  id: reservation.id,
                  reservationId: reservation.reservationId,
                  firstName: reservation.firstName,
                  lastName: reservation.lastName,
                  email: reservation.email,
                  amountDue: 0.0,
                  tickets: [],
                  payments: []
          )
          def newEntity = new ReservationEntity(
                  id: reservation.id,
                  reservationId: reservation.reservationId,
                  firstName: reservation.firstName,
                  lastName: reservation.lastName,
                  email: reservation.email,
                  amountDue: 0.0,
                  tickets: [],
                  payments: reservation.payments.collect {
                      new PaymentEntity(
                              amount: it.amount,
                              status: it.status,
                              method: it.method,
                              createdTimestamp: it.createdTimestamp
                      )
                  }
          )
          newEntity.payments.each { it.reservation = newEntity }

        when:
          def result = service.saveReservation(reservation)

        then:
          1 * reservationRepository.findById(reservation.id) >> Optional.of(oldEntity)
          1 * reservationRepository.save(newEntity) >> newEntity

          result.id == reservation.id
          result.reservationId == reservation.reservationId
          result.firstName == reservation.firstName
          result.lastName == reservation.lastName
          result.email == reservation.email
    }

    /////////////////////////////////////////////////////////////////////////////////////////

    ReservationEntity rtc(ReservationStatus status, int daysOld, List<Integer> counts) {
        new ReservationEntity(
                status: status,
                reservationTimestamp: new Date(new Date().time - TimeUnit.DAYS.toMillis(daysOld)),
                tickets: counts.collect { new ReservationTicketsEntity(count: it) },
                payments: []
        )
    }

}
