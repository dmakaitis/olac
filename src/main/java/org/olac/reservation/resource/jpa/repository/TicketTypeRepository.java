package org.olac.reservation.resource.jpa.repository;

import org.olac.reservation.resource.jpa.entity.TicketTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TicketTypeRepository extends JpaRepository<TicketTypeEntity, Long> {

    Optional<TicketTypeEntity> findByCode(String code);

}
