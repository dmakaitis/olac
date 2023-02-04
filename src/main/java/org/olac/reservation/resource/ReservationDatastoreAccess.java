package org.olac.reservation.resource;

import java.util.List;

public interface ReservationDatastoreAccess {

    long createReservation(Reservation reservation);

    List<Reservation> getReservations();

}
