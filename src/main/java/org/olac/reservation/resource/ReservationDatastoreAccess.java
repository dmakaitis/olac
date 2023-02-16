package org.olac.reservation.resource;

import org.olac.reservation.resource.model.Payment;
import org.olac.reservation.resource.model.Reservation;
import org.olac.reservation.resource.model.ReservationStatus;

import java.util.List;
import java.util.Optional;

public interface ReservationDatastoreAccess {

    long createReservation(Reservation reservation);

    List<Reservation> getReservations();

    void addPaymentToReservation(String reservationId, Payment payment);

    Optional<Reservation> getReservation(String reservationId);

    void updateReservationStatus(String reservationId, ReservationStatus newStatus);

}
