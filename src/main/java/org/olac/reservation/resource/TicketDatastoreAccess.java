package org.olac.reservation.resource;

import org.olac.reservation.resource.model.TicketType;

import java.util.List;

public interface TicketDatastoreAccess {

    List<TicketType> getTicketTypes();

    TicketType saveTicketType(TicketType ticketType);

    void deleteTicketType(String ticketTypeCode);

}
