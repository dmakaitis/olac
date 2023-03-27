package org.olac.reservation.resource.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ReservationStats {

    final long ticketsOrdered;
    final long ticketsReserved;
    final long ticketsPaid;
    final BigDecimal amountDue;
    final BigDecimal amountPaid;

    public static final ReservationStats ZERO = ReservationStats.builder()
            .ticketsOrdered(0)
            .ticketsReserved(0)
            .ticketsPaid(0)
            .amountDue(BigDecimal.ZERO)
            .amountPaid(BigDecimal.ZERO)
            .build();

    public ReservationStats add(ReservationStats augand) {
        return ReservationStats.builder()
                .ticketsOrdered(ticketsOrdered + augand.ticketsOrdered)
                .ticketsReserved(ticketsReserved + augand.ticketsReserved)
                .ticketsPaid(ticketsPaid + augand.ticketsPaid)
                .amountDue(amountDue.add(augand.amountDue))
                .amountPaid(amountPaid.add(augand.amountPaid))
                .build();
    }

}
