package org.olac.reservation.resource;

import org.olac.reservation.resource.model.Page;
import org.olac.reservation.resource.model.PageRequest;
import org.olac.reservation.resource.model.Reservation;
import org.olac.reservation.resource.model.ReservationStats;

import java.util.Optional;

public interface ReservationDatastoreAccess {

    Page<Reservation> getReservations(String filter, PageRequest pageRequest);

    ReservationStats getReservationsStats();

    Optional<Reservation> getReservation(String reservationId);

    Reservation saveReservation(Reservation reservation);

    void deleteReservation(String reservationId);

}
