package org.olac.reservation.resource.jpa.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Data
@NoArgsConstructor
public class ReservationTicketsEntity {

    public ReservationTicketsEntity(ReservationEntity reservation, TicketTypeEntity ticketType, int count) {
        this.reservation = reservation;
        this.ticketType = ticketType;
        this.count = count;
    }

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn(name = "RESERVATION_ID", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private ReservationEntity reservation;

    @ManyToOne
    @JoinColumn(name = "TYPE_ID", nullable = false)
    private TicketTypeEntity ticketType;

    private int count;

}
