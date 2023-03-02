package org.olac.reservation.utility.jpa;

import lombok.RequiredArgsConstructor;
import org.olac.reservation.utility.AuditUtility;
import org.olac.reservation.utility.DateTimeUtility;
import org.olac.reservation.utility.SecurityUtility;
import org.olac.reservation.utility.jpa.entity.ReservationAuditEventEntity;
import org.olac.reservation.utility.jpa.repository.ReservationEventRepository;
import org.olac.reservation.utility.model.ReservationAuditEvent;
import org.springframework.stereotype.Service;

import java.util.List;

import static java.util.Comparator.comparing;

@Service
@RequiredArgsConstructor
public class DatastoreAuditUtility implements AuditUtility {

    private final ReservationEventRepository repository;
    private final SecurityUtility securityUtility;
    private final DateTimeUtility dateTimeUtility;

    @Override
    public void logReservationEvent(String reservationId, String description) {
        repository.save(buildReservationEventEntity(reservationId, description));
    }

    @Override
    public List<ReservationAuditEvent> getReservationEvents(String reservationId) {
        return repository.getByReservationId(reservationId).stream()
                .map(this::toReservationEvent)
                .sorted(comparing(ReservationAuditEvent::getTimestamp))
                .toList();
    }

    private ReservationAuditEventEntity buildReservationEventEntity(String reservationId, String description) {
        ReservationAuditEventEntity entity = new ReservationAuditEventEntity();
        entity.setReservationId(reservationId);
        entity.setTimestamp(dateTimeUtility.getCurrentTime());
        entity.setUsername(securityUtility.getCurrentUserName());
        entity.setDescription(description);

        return entity;
    }

    private ReservationAuditEvent toReservationEvent(ReservationAuditEventEntity reservationEventEntity) {
        return ReservationAuditEvent.builder()
                .reservationId(reservationEventEntity.getReservationId())
                .timestamp(reservationEventEntity.getTimestamp())
                .user(reservationEventEntity.getUsername())
                .description(reservationEventEntity.getDescription())
                .build();
    }

}
