package org.olac.reservation.resource;

import lombok.Data;

import java.util.List;

@Data
public class Reservation {

    private String firstName;
    private String lastName;
    private String email;
    private String phone;

    private List<TicketCounts> ticketCounts;

}
