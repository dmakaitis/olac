package org.olac.reservation.utility.jpa.repository;

import org.olac.reservation.utility.jpa.entity.ReservationEventEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ReservationEventRepository extends CrudRepository<ReservationEventEntity, Long> {

    List<ReservationEventEntity> getByReservationId(String reservationId);

}
