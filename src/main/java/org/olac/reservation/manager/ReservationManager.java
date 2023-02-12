package org.olac.reservation.manager;

import org.olac.reservation.resource.Reservation;
import org.olac.reservation.resource.TicketType;

import java.util.List;

public interface ReservationManager {

    List<TicketType> getTicketTypes();

    void saveTicketType(TicketType ticketType);

    long createReservation(Reservation reservation);

    List<Reservation> getReservations();

    boolean areTicketsAvailable(long requestedTicketCount);

}
