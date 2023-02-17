package org.olac.reservation.resource.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TicketType {

    public TicketType(String description, double costPerTicket) {
        this(null, description, costPerTicket);
    }

    private String code;
    private String description;
    private double costPerTicket;

}
