package org.olac.reservation.resource.jpa;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.olac.reservation.resource.ReservationDatastoreAccess;
import org.olac.reservation.resource.TicketDatastoreAccess;
import org.olac.reservation.resource.jpa.entity.PaymentEntity;
import org.olac.reservation.resource.jpa.entity.ReservationEntity;
import org.olac.reservation.resource.jpa.entity.ReservationTicketsEntity;
import org.olac.reservation.resource.jpa.entity.TicketTypeEntity;
import org.olac.reservation.resource.jpa.repository.ReservationRepository;
import org.olac.reservation.resource.jpa.repository.TicketTypeRepository;
import org.olac.reservation.resource.model.Payment;
import org.olac.reservation.resource.model.Reservation;
import org.olac.reservation.resource.model.TicketCounts;
import org.olac.reservation.resource.model.TicketType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toSet;
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
    public void deleteTicketType(String ticketTypeCode) {
        Optional<TicketTypeEntity> type = ticketTypeRepository.findByCode(ticketTypeCode);
        type.ifPresent(ticketTypeRepository::delete);
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

    @Override
    public void addPaymentToReservation(String reservationId, Payment payment) {
        Optional<ReservationEntity> reservationEntity = reservationRepository.findByReservationId(reservationId);
        reservationEntity.ifPresent(e -> {
            e.getPayments().add(toPaymentEntity(payment, e));
            reservationRepository.save(e);
        });
    }

    @Override
    public Optional<Reservation> getReservation(String reservationId) {
        return reservationRepository.findByReservationId(reservationId)
                .map(this::toReservation);
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
        entity.setReservationId(reservation.getReservationId());
        entity.setFirstName(reservation.getFirstName());
        entity.setLastName(reservation.getLastName());
        entity.setEmail(reservation.getEmail());
        entity.setPhone(reservation.getPhone());
        entity.setReservationTimestamp(reservation.getReservationTimestamp());
        entity.setAmountDue(reservation.getAmountDue());
        entity.setTickets(reservation.getTicketCounts().stream()
                .map(t -> ticketTypeRepository.findByCode(t.getTicketTypeCode())
                        .map(c -> new ReservationTicketsEntity(entity, c, t.getCount())))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toSet()));
        entity.setPayments(reservation.getPayments().stream()
                .map(p -> toPaymentEntity(p, entity))
                .collect(toSet()));
        return entity;
    }

    private Reservation toReservation(ReservationEntity entity) {
        Reservation reservation = new Reservation();

        reservation.setId(entity.getId());
        reservation.setReservationId(entity.getReservationId());
        reservation.setFirstName(entity.getFirstName());
        reservation.setLastName(entity.getLastName());
        reservation.setEmail(entity.getEmail());
        reservation.setPhone(entity.getPhone());
        reservation.setReservationTimestamp(entity.getReservationTimestamp());
        reservation.setAmountDue(entity.getAmountDue());

        reservation.setTicketCounts(entity.getTickets().stream()
                .map(t -> new TicketCounts(t.getTicketType().getCode(), t.getCount()))
                .toList());
        reservation.setPayments(entity.getPayments().stream()
                .map(this::toPayment)
                .toList());

        return reservation;
    }

    private PaymentEntity toPaymentEntity(Payment payment, ReservationEntity reservation) {
        PaymentEntity entity = new PaymentEntity();

        entity.setId(payment.getId());
        entity.setReservation(reservation);
        entity.setAmount(payment.getAmount());
        entity.setStatus(payment.getStatus());

        return entity;
    }

    private Payment toPayment(PaymentEntity entity) {
        return Payment.builder()
                .id(entity.getId())
                .amount(entity.getAmount())
                .status(entity.getStatus())
                .build();
    }

}
