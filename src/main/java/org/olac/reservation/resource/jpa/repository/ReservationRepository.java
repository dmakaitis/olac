package org.olac.reservation.resource.jpa.repository;

import org.olac.reservation.resource.jpa.entity.ReservationEntity;
import org.springframework.data.repository.CrudRepository;

public interface ReservationRepository extends CrudRepository<ReservationEntity, Long> {

}
