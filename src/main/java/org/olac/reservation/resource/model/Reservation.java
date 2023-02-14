package org.olac.reservation.resource.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class Reservation {

    private Long id;
    private String reservationId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private Date reservationTimestamp;
    private List<TicketCounts> ticketCounts = new ArrayList<>();
    private double amountDue;
    private List<Payment> payments = new ArrayList<>();

}
