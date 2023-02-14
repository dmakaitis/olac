package org.olac.reservation.resource.jpa.repository;

import org.olac.reservation.resource.jpa.entity.ReservationEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface ReservationRepository extends CrudRepository<ReservationEntity, Long> {

    Optional<ReservationEntity> findByReservationId(String reservationId);

}
