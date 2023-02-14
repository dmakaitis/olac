package org.olac.reservation.resource;

import org.olac.reservation.resource.model.Payment;
import org.olac.reservation.resource.model.Reservation;

import java.util.List;

public interface ReservationDatastoreAccess {

    long createReservation(Reservation reservation);

    List<Reservation> getReservations();

    void addPaymentToReservation(String reservationId, Payment payment);

}
