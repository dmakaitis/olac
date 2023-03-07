package org.olac.reservation.resource.jpa;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.olac.reservation.resource.ReservationDatastoreAccess;
import org.olac.reservation.resource.TicketDatastoreAccess;
import org.olac.reservation.resource.jpa.entity.PaymentEntity;
import org.olac.reservation.resource.jpa.entity.ReservationEntity;
import org.olac.reservation.resource.jpa.entity.ReservationTicketsEntity;
import org.olac.reservation.resource.jpa.entity.TicketTypeEntity;
import org.olac.reservation.resource.jpa.repository.ReservationRepository;
import org.olac.reservation.resource.jpa.repository.TicketTypeRepository;
import org.olac.reservation.resource.model.*;
import org.olac.reservation.utility.AuditUtility;
import org.olac.reservation.utility.FormatUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.olac.reservation.resource.jpa.specification.ReservationSpecification.withReservationId;

@Service
@RequiredArgsConstructor
@Slf4j
public class DatastoreAccess implements TicketDatastoreAccess, ReservationDatastoreAccess {

    private final TicketTypeRepository ticketTypeRepository;
    private final ReservationRepository reservationRepository;
    private final AuditUtility auditUtility;
    private final FormatUtility formatUtility;

    private final Supplier<String> codeSupplier;

    @Autowired
    public DatastoreAccess(TicketTypeRepository ticketTypeRepository, ReservationRepository reservationRepository, AuditUtility auditUtility, FormatUtility formatUtility) {
        this(ticketTypeRepository, reservationRepository, auditUtility, formatUtility, () -> UUID.randomUUID().toString());
    }

    @Override
    @Transactional
    public List<TicketType> getTicketTypes() {
        return StreamSupport.stream(ticketTypeRepository.findAll().spliterator(), false)
                .map(DatastoreAccess::toTicketType)
                .toList();
    }

    @Override
    @Transactional
    public TicketType saveTicketType(TicketType ticketType) {
        TicketTypeEntity entity = getTicketTypeEntity(ticketType.getCode());

        entity.setDescription(ticketType.getDescription());
        entity.setCostPerTicket(ticketType.getCostPerTicket());

        entity = ticketTypeRepository.save(entity);

        return toTicketType(entity);
    }

    @Override
    @Transactional
    public void deleteTicketType(String ticketTypeCode) {
        ticketTypeRepository.findByCode(ticketTypeCode)
                .ifPresent(ticketTypeRepository::delete);
    }

    @Override
    public void deleteReservation(String reservationId) {
        reservationRepository.delete(withReservationId(reservationId));
    }

    @Override
    @Transactional
    public long createReservation(Reservation reservation) {
        ReservationEntity reservationEntity = toEntity(reservation);

        reservationEntity = reservationRepository.save(reservationEntity);

        auditUtility.logReservationEvent(reservation.getReservationId(), String.format("New reservation created for %s %s",
                reservation.getFirstName(),
                reservation.getLastName()));

        return reservationEntity.getId();
    }

    @Override
    @Transactional
    public Page<Reservation> getReservations(org.olac.reservation.resource.model.PageRequest pageRequest) {
        Specification<ReservationEntity> specification = null;

        Sort.Direction direction = pageRequest.isDescending() ? Sort.Direction.DESC : Sort.Direction.ASC;
        String property = pageRequest.getSortBy();

        Pageable pageable = property == null ?
                PageRequest.of(pageRequest.getPage(), pageRequest.getItemsPerPage()) :
                PageRequest.of(pageRequest.getPage(), pageRequest.getItemsPerPage(), direction, property);

        return toPage(reservationRepository.findAll(specification, pageable)
                .map(this::toReservation));
    }

    @Override
    public long getTotalTicketsReserved() {
        return reservationRepository.findAll().stream()
                .filter(r -> r.getStatus() == ReservationStatus.RESERVED)
                .mapToInt(r -> r.getTickets().stream()
                        .mapToInt(ReservationTicketsEntity::getCount)
                        .sum())
                .sum();
    }

    @Override
    @Transactional
    public void addPaymentToReservation(String reservationId, Payment payment) {
        Optional<ReservationEntity> reservationEntity = reservationRepository.findOne(withReservationId(reservationId));
        reservationEntity.ifPresent(e -> {
            e.getPayments().add(toPaymentEntity(payment, e));
            reservationRepository.save(e);

            auditUtility.logReservationEvent(reservationId, String.format("Added payment of %s, paid  by %s",
                    formatUtility.formatCurrencty(payment.getAmount()),
                    payment.getMethod()));
        });
    }

    @Override
    @Transactional
    public Optional<Reservation> getReservation(String reservationId) {
        if (isBlank(reservationId)) {
            return Optional.empty();
        }

        return reservationRepository.findOne(withReservationId(reservationId))
                .map(this::toReservation);
    }

    @Override
    @Transactional
    public void updateReservationStatus(String reservationId, ReservationStatus newStatus) {
        reservationRepository.findOne(withReservationId(reservationId)).ifPresent(r -> {
            ReservationStatus oldStatus = r.getStatus();
            r.setStatus(newStatus);
            reservationRepository.save(r);

            auditUtility.logReservationEvent(reservationId, String.format("Updated reservation status: %s => %s",
                    oldStatus,
                    newStatus));
        });
    }

    @Override
    public Reservation saveReservation(Reservation reservation) {
        boolean newReservation = true;

        if (reservation.getId() != null) {
            Optional<ReservationEntity> reservationEntity = reservationRepository.findById(reservation.getId());
            if (reservationEntity.isPresent()) {
                auditUtility.logReservationEvent(reservation.getReservationId(), String.format("Updating reservation with changes - %s",
                        getChangedFields(reservation, reservationEntity.get())));

                newReservation = false;
            }
        }

        if (newReservation) {
            auditUtility.logReservationEvent(reservation.getReservationId(), String.format("Saving new reservation for %s %s",
                    reservation.getFirstName(),
                    reservation.getLastName()));
        }

        return toReservation(reservationRepository.save(toEntity(reservation)));
    }

    @NotNull
    private String getChangedFields(Reservation reservation, ReservationEntity entity) {
        // Figure out what changed
        List<String> changedFields = new ArrayList<>();

        addFieldIfChanged(changedFields, "reservationId", reservation.getReservationId(), entity.getReservationId());
        addFieldIfChanged(changedFields, "firstName", reservation.getFirstName(), entity.getFirstName());
        addFieldIfChanged(changedFields, "lastName", reservation.getLastName(), entity.getLastName());
        addFieldIfChanged(changedFields, "email", reservation.getEmail(), entity.getEmail());
        addFieldIfChanged(changedFields, "phone", reservation.getPhone(), entity.getPhone());
        addFieldIfChanged(changedFields, "status", reservation.getStatus(), entity.getStatus());
        addFieldIfChanged(changedFields, "amount due", reservation.getAmountDue(), entity.getAmountDue());

        // These require a bit more logic...
        if (didTicketCountsChange(reservation.getTicketCounts(), entity.getTickets())) {
            changedFields.add("ticket counts");
        }
        if (didPaymentsChange(reservation.getPayments(), entity.getPayments())) {
            double oldTotal = entity.getPayments().stream()
                    .mapToDouble(PaymentEntity::getAmount)
                    .sum();
            double newTotal = reservation.getPayments().stream()
                    .mapToDouble(Payment::getAmount)
                    .sum();

            changedFields.add(String.format("payments: %s => %s", oldTotal, newTotal));
        }

        String formattedChanges = changedFields.stream()
                .collect(Collectors.joining(", "));
        if (formattedChanges.length() > 1024) {
            formattedChanges = formattedChanges.substring(0, 1020) + "...";
        }
        return formattedChanges;
    }

    private boolean didTicketCountsChange(List<TicketCounts> newValue, Set<ReservationTicketsEntity> oldValue) {
        Set<TicketCounts> newTicketCounts = new HashSet<>(newValue);
        Set<TicketCounts> oldTicketCounts = oldValue.stream()
                .map(c -> new TicketCounts(c.getTicketType().getCode(), c.getCount()))
                .collect(toSet());

        return !newTicketCounts.equals(oldTicketCounts);
    }

    private boolean didPaymentsChange(List<Payment> newValue, Set<PaymentEntity> oldValue) {
        Set<Payment> newPayments = new HashSet<>(newValue);
        Set<Payment> oldPayments = oldValue.stream()
                .map(this::toPayment)
                .collect(toSet());

        return !newPayments.equals(oldPayments);
    }

    private void addFieldIfChanged(List<String> changedFields, String fieldName, Object newValue, Object oldValue) {
        if (!Objects.equals(oldValue, newValue)) {
            changedFields.add(String.format("%s: %s => %s", fieldName, oldValue, newValue));
        }
    }

    private TicketTypeEntity getTicketTypeEntity(String code) {
        TicketTypeEntity entity;
        if (isBlank(code)) {
            entity = new TicketTypeEntity(codeSupplier.get(), "unknown", 0.0);
        } else {
            entity = ticketTypeRepository.findByCode(code)
                    .orElseGet(() -> new TicketTypeEntity(code, "unknown", 0.0));
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
        entity.setStatus(reservation.getStatus());
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
        reservation.setStatus(entity.getStatus());
        reservation.setReservationTimestamp(entity.getReservationTimestamp());
        reservation.setAmountDue(entity.getAmountDue());

        reservation.setTicketCounts(entity.getTickets().stream()
                .map(t -> new TicketCounts(t.getTicketType().getCode(), t.getCount()))
                .toList());
        reservation.setPayments(entity.getPayments().stream()
                .map(this::toPayment)
                .sorted(comparing(Payment::getCreatedTimestamp))
                .toList());

        return reservation;
    }

    private PaymentEntity toPaymentEntity(Payment payment, ReservationEntity reservation) {
        PaymentEntity entity = new PaymentEntity();

        entity.setReservation(reservation);
        entity.setAmount(payment.getAmount());
        entity.setStatus(payment.getStatus());
        entity.setMethod(payment.getMethod());
        entity.setNotes(payment.getNotes());
        entity.setEnteredBy(payment.getEnteredBy());
        entity.setCreatedTimestamp(payment.getCreatedTimestamp());

        return entity;
    }

    private Payment toPayment(PaymentEntity entity) {
        return Payment.builder()
                .id(entity.getId())
                .amount(entity.getAmount())
                .status(entity.getStatus())
                .method(entity.getMethod())
                .notes(entity.getNotes())
                .enteredBy(entity.getEnteredBy())
                .createdTimestamp(entity.getCreatedTimestamp())
                .build();
    }

    private static <T> Page<T> toPage(org.springframework.data.domain.Page<T> page) {
        return Page.<T>builder()
                .pageNumber(page.getNumber())
                .itemsPerPage(page.getSize())
                .totalItems(page.getTotalElements())
                .data(page.getContent())
                .descending(page.getSort().stream()
                        .map(Sort.Order::getDirection)
                        .map(Sort.Direction::isDescending)
                        .findFirst().orElse(false))
                .sortBy(page.getSort().stream()
                        .map(Sort.Order::getProperty)
                        .findFirst()
                        .orElse(null))
                .build();
    }

}
