package org.olac.reservation.resource;

import java.util.List;

public interface TicketDatastoreAccess {

    List<TicketType> getTicketTypes();

    TicketType saveTicketType(TicketType ticketType);

}
