package org.olac.reservation.resource.jpa;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.olac.reservation.resource.*;
import org.olac.reservation.resource.jpa.entity.ReservationEntity;
import org.olac.reservation.resource.jpa.entity.ReservationTicketsEntity;
import org.olac.reservation.resource.jpa.entity.TicketTypeEntity;
import org.olac.reservation.resource.jpa.repository.ReservationRepository;
import org.olac.reservation.resource.jpa.repository.TicketTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.apache.commons.lang3.StringUtils.isBlank;

@Service
@RequiredArgsConstructor
@Slf4j
public class DatastoreAccess implements TicketDatastoreAccess, ReservationDatastoreAccess {

    private final TicketTypeRepository ticketTypeRepository;
    private final ReservationRepository reservationRepository;

    private final Supplier<String> codeSupplier;

    @Autowired
    public DatastoreAccess(TicketTypeRepository ticketTypeRepository, ReservationRepository reservationRepository) {
        this(ticketTypeRepository, reservationRepository, () -> UUID.randomUUID().toString());
    }

    @Override
    public List<TicketType> getTicketTypes() {
        return StreamSupport.stream(ticketTypeRepository.findAll().spliterator(), false)
                .map(DatastoreAccess::toTicketType)
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

        reservationEntity = reservationRepository.save(reservationEntity);

        return reservationEntity.getId();
    }

    @Override
    public List<Reservation> getReservations() {
        return StreamSupport.stream(reservationRepository.findAll().spliterator(), false)
                .map(this::toReservation)
                .toList();
    }

    private TicketTypeEntity getTicketTypeEntity(String code) {
        TicketTypeEntity entity;
        if (isBlank(code)) {
            entity = new TicketTypeEntity(codeSupplier.get(), "unknown", 0.0);
        } else {
            entity = ticketTypeRepository.findByCode(code)
                    .orElseGet(() -> new TicketTypeEntity(codeSupplier.get(), "unknown", 0.0));
        }
        return entity;
    }

    private static TicketType toTicketType(TicketTypeEntity entity) {
        return new TicketType(entity.getCode(), entity.getDescription(), entity.getCostPerTicket());
    }

    private ReservationEntity toEntity(Reservation reservation) {
        ReservationEntity entity = new ReservationEntity();

        entity.setId(reservation.getId());
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

    private Reservation toReservation(ReservationEntity entity) {
        Reservation reservation = new Reservation();

        reservation.setId(entity.getId());
        reservation.setFirstName(entity.getFirstName());
        reservation.setLastName(entity.getLastName());
        reservation.setEmail(entity.getEmail());
        reservation.setPhone(entity.getPhone());

        reservation.setTicketCounts(entity.getTickets().stream()
                .map(t -> new TicketCounts(t.getTicketType().getCode(), t.getCount()))
                .toList());

        return reservation;
    }

}
