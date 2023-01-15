package org.olac.reservation.resource;

import lombok.Data;

import java.util.List;

@Data
public class Reservation {

    private String name;
    private String email;
    private String phone;

    private List<TicketCounts> ticketCounts;

}
