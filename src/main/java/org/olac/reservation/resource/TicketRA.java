package org.olac.reservation.resource;

import java.util.List;

public interface TicketRA {

    List<TicketType> getTicketTypes();

    void saveTicketType(TicketType ticketType);

    void renameTicketType(String oldName, String newName);

}
