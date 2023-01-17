package org.olac.reservation.resource;

import java.util.List;

public interface TicketRA {

    List<TicketType> getTicketTypes();

    TicketType saveTicketType(TicketType ticketType);

}
