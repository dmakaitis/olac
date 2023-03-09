package org.olac.reservation.resource;

import org.olac.reservation.resource.model.*;

import java.util.Optional;

public interface ReservationDatastoreAccess {

    Page<Reservation> getReservations(String filter, PageRequest pageRequest);

    long getTotalTicketsReserved();

    @Deprecated
    void addPaymentToReservation(String reservationId, Payment payment);

    Optional<Reservation> getReservation(String reservationId);

    @Deprecated
    void updateReservationStatus(String reservationId, ReservationStatus newStatus);

    Reservation saveReservation(Reservation reservation);

    void deleteReservation(String reservationId);

}
