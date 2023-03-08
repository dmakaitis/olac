package org.olac.reservation.manager;

import org.olac.reservation.resource.model.Page;
import org.olac.reservation.resource.model.PageRequest;
import org.olac.reservation.resource.model.Reservation;
import org.olac.reservation.resource.model.TicketType;

import java.util.List;

public interface AdministrationManager {

    List<TicketType> getTicketTypes();

    TicketType saveTicketType(TicketType ticketType);

    void deleteTicketType(String ticketTypeCode);

    Page<Reservation> getReservations(String filter, PageRequest pageRequest);

    Reservation saveReservation(Reservation reservation);

    void deleteReservation(String reservationId);

}
