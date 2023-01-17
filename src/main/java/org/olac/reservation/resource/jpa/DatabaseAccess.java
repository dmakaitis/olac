package org.olac.reservation.resource.jpa;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.olac.reservation.resource.Reservation;
import org.olac.reservation.resource.ReservationRA;
import org.olac.reservation.resource.TicketRA;
import org.olac.reservation.resource.TicketType;
import org.olac.reservation.resource.jpa.entity.ReservationEntity;
import org.olac.reservation.resource.jpa.entity.ReservationTicketsEntity;
import org.olac.reservation.resource.jpa.entity.TicketTypeEntity;
import org.olac.reservation.resource.jpa.repository.ReservationRepository;
import org.olac.reservation.resource.jpa.repository.TicketTypeRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.apache.commons.lang3.StringUtils.isBlank;

@Service
@RequiredArgsConstructor
@Slf4j
public class DatabaseAccess implements TicketRA, ReservationRA {

    private final TicketTypeRepository ticketTypeRepository;
    private final ReservationRepository reservationRepository;

    @Override
    public List<TicketType> getTicketTypes() {
        return StreamSupport.stream(ticketTypeRepository.findAll().spliterator(), false)
                .map(DatabaseAccess::toTicketType)
                .toList();
    }

    @Override
    public TicketType saveTicketType(TicketType ticketType) {
        TicketTypeEntity entity = getTicketTypeEntity(ticketType.getCode());

        entity.setDescription(ticketType.getDescription());
        entity.setCostPerTicket(ticketType.getCostPerTicket());

        entity = ticketTypeRepository.save(entity);

        return toTicketType(entity);
    }

    @Override
    public long createReservation(Reservation reservation) {
        ReservationEntity reservationEntity = toEntity(reservation);

        log.info("Saving reservation: {}", reservationEntity);

        reservationEntity = reservationRepository.save(reservationEntity);

        log.info("Saved reservation: {}", reservationEntity);

        return reservationEntity.getId();
    }

    private TicketTypeEntity getTicketTypeEntity(String code) {
        TicketTypeEntity entity;
        if (isBlank(code)) {
            entity = new TicketTypeEntity(UUID.randomUUID().toString(), "unknown", 0.0);
        } else {
            entity = ticketTypeRepository.findByCode(code)
                    .orElseGet(() -> new TicketTypeEntity(UUID.randomUUID().toString(), "unknown", 0.0));
        }
        return entity;
    }

    private static TicketType toTicketType(TicketTypeEntity entity) {
        return new TicketType(entity.getCode(), entity.getDescription(), entity.getCostPerTicket());
    }

    private ReservationEntity toEntity(Reservation reservation) {
        ReservationEntity entity = new ReservationEntity();

        entity.setFirstName(reservation.getFirstName());
        entity.setLastName(reservation.getLastName());
        entity.setEmail(reservation.getEmail());
        entity.setPhone(reservation.getPhone());

        entity.setTickets(reservation.getTicketCounts().stream()
                .map(t -> ticketTypeRepository.findByCode(t.getTicketTypeCode())
                        .map(c -> new ReservationTicketsEntity(entity, c, t.getCount())))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet()));

        return entity;
    }
}
