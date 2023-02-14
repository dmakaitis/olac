package org.olac.reservation.resource.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TicketType {

    public TicketType(String description, double costPerTicket) {
        this(null, description, costPerTicket);
    }

    private final String code;
    private final String description;
    private final double costPerTicket;

}
