package org.olac.reservation.resource;

import org.olac.reservation.resource.model.*;

import java.util.Optional;

public interface ReservationDatastoreAccess {

    long createReservation(Reservation reservation);

    Page<Reservation> getReservations(String filter, PageRequest pageRequest);

    long getTotalTicketsReserved();

    void addPaymentToReservation(String reservationId, Payment payment);

    Optional<Reservation> getReservation(String reservationId);

    void updateReservationStatus(String reservationId, ReservationStatus newStatus);

    Reservation saveReservation(Reservation reservation);

    void deleteReservation(String reservationId);

}
