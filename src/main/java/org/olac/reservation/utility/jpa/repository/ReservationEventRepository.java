package org.olac.reservation.utility.jpa.repository;

import org.olac.reservation.utility.jpa.entity.ReservationAuditEventEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ReservationEventRepository extends CrudRepository<ReservationAuditEventEntity, Long> {

    List<ReservationAuditEventEntity> getByReservationId(String reservationId);

}
