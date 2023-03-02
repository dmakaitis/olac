package org.olac.reservation.manager;

import org.olac.reservation.resource.model.Reservation;
import org.olac.reservation.resource.model.TicketType;

import java.util.List;

public interface AdministrationManager {

    List<TicketType> getTicketTypes();

    TicketType saveTicketType(TicketType ticketType);

    void deleteTicketType(String ticketTypeCode);

    List<Reservation> getReservations();

    Reservation saveReservation(Reservation reservation);

}
