package org.olac.reservation.manager.impl;

import lombok.RequiredArgsConstructor;
import org.olac.reservation.manager.ReservationManager;
import org.olac.reservation.resource.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationManagerImpl implements ReservationManager {

    private final TicketDatastoreAccess ticketDatastoreAccess;
    private final ReservationDatastoreAccess reservationDatastoreAccess;
    private final NotificationAccess notificationAccess;

    @Override
    public void saveTicketType(TicketType ticketType) {
        ticketDatastoreAccess.saveTicketType(ticketType);
    }

    @Override
    public List<TicketType> getTicketTypes() {
        return ticketDatastoreAccess.getTicketTypes();
    }

    @Override
    public long createReservation(Reservation reservation) {
        long reservationId = reservationDatastoreAccess.createReservation(reservation);
        notificationAccess.sendReservationConfirmation(reservationId, reservation);
        return reservationId;
    }

    @Override
    public List<Reservation> getReservations() {
        return reservationDatastoreAccess.getReservations();
    }

}
