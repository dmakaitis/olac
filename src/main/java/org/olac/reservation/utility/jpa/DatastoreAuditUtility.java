package org.olac.reservation.utility.jpa;

import lombok.RequiredArgsConstructor;
import org.olac.reservation.utility.AuditUtility;
import org.olac.reservation.utility.DateTimeUtility;
import org.olac.reservation.utility.SecurityUtility;
import org.olac.reservation.utility.jpa.entity.ReservationEventEntity;
import org.olac.reservation.utility.jpa.repository.ReservationEventRepository;
import org.olac.reservation.utility.model.ReservationEvent;
import org.springframework.stereotype.Service;

import java.util.List;

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
    public List<ReservationEvent> getReservationEvents(String reservationId) {
        return repository.getByReservationId(reservationId).stream()
                .map(this::toReservationEvent)
                .toList();
    }

    private ReservationEventEntity buildReservationEventEntity(String reservationId, String description) {
        ReservationEventEntity entity = new ReservationEventEntity();
        entity.setReservationId(reservationId);
        entity.setTimestamp(dateTimeUtility.getCurrentTime());
        entity.setUsername(securityUtility.getCurrentUserName());
        entity.setDescription(description);

        return entity;
    }

    private ReservationEvent toReservationEvent(ReservationEventEntity reservationEventEntity) {
        return ReservationEvent.builder()
                .reservationId(reservationEventEntity.getReservationId())
                .timestamp(reservationEventEntity.getTimestamp())
                .user(reservationEventEntity.getUsername())
                .description(reservationEventEntity.getDescription())
                .build();
    }

}
