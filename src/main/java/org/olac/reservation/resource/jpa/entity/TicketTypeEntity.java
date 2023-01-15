package org.olac.reservation.resource.jpa.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class TicketTypeEntity {

    public TicketTypeEntity(String ticketType, double ticketCost) {
        this.ticketType = ticketType;
        this.ticketCost = ticketCost;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String ticketType;
    private double ticketCost;

}
