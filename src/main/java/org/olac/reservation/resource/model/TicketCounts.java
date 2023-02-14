package org.olac.reservation.resource.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TicketCounts {

    private String ticketTypeCode;
    private int count;

}
