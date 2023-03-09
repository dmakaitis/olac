package org.olac.reservation.utility.jpa

import org.olac.reservation.utility.DateTimeUtility
import org.olac.reservation.utility.SecurityUtility
import org.olac.reservation.utility.jpa.entity.ReservationAuditEventEntity
import org.olac.reservation.utility.jpa.repository.ReservationEventRepository
import org.olac.reservation.utility.model.ReservationAuditEvent
import spock.lang.Specification

class DatastoreAuditUtilityTest extends Specification {

    def now = new Date()
    def currentUsername = "test-user"

    def repository = Mock(ReservationEventRepository)
    def securityUtility = Mock(SecurityUtility) {
        _ * getCurrentUserName() >> currentUsername
    }
    def dateTimeUtility = Mock(DateTimeUtility) {
        _ * getCurrentTime() >> now
    }

    def service = new DatastoreAuditUtility(repository, securityUtility, dateTimeUtility)

    def "We should be able to log an event"() {
        given:
          def reservationId = "my-reservation-id"
          def message = "Test audit event"

        when:
          service.logReservationEvent(reservationId, message)

        then:
          1 * repository.save(new ReservationAuditEventEntity(
                  reservationId: reservationId,
                  timestamp: now,
                  username: currentUsername,
                  description: message
          ))
    }

    def "GetReservationEvents"() {
        given:
          def reservationId = "my-reservation-id"

          def expected = [
                  new ReservationAuditEvent(
                          reservationId: reservationId,
                          timestamp: new Date(),
                          user: "bob",
                          description: "Event 1"
                  ),
                  new ReservationAuditEvent(
                          reservationId: reservationId,
                          timestamp: new Date(),
                          user: "bob",
                          description: "Event 2"
                  ),
                  new ReservationAuditEvent(
                          reservationId: reservationId,
                          timestamp: new Date(),
                          user: "sally",
                          description: "Event 3"
                  ),
                  new ReservationAuditEvent(
                          reservationId: reservationId,
                          timestamp: new Date(),
                          user: "alice",
                          description: "Event 4"
                  ),
          ]

          _ * repository.getByReservationId(reservationId) >> expected.collect {
              new ReservationAuditEventEntity(
                      reservationId: it.reservationId,
                      timestamp: it.timestamp,
                      username: it.user,
                      description: it.description
              )
          }

        expect:
          service.getReservationEvents(reservationId) == expected
    }
}
