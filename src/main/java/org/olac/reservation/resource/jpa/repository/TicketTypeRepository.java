package org.olac.reservation.resource.jpa.repository;

import org.olac.reservation.resource.jpa.entity.TicketTypeEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface TicketTypeRepository extends CrudRepository<TicketTypeEntity, Long> {

    Optional<TicketTypeEntity> findByTicketType(String ticketType);

}
