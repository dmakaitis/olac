package org.olac.reservation.resource.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TicketCounts {

    private String ticketTypeCode;
    private int count;

}
