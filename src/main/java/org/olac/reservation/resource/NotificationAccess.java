package org.olac.reservation.resource;

public interface NotificationAccess {

    void sendReservationConfirmation(long reservationId, Reservation reservation);

}
