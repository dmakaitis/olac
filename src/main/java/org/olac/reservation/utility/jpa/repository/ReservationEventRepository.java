package org.olac.reservation.utility.jpa.repository;

import org.olac.reservation.utility.jpa.entity.ReservationAuditEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReservationEventRepository extends JpaRepository<ReservationAuditEventEntity, Long> {

    List<ReservationAuditEventEntity> getByReservationId(String reservationId);

}
