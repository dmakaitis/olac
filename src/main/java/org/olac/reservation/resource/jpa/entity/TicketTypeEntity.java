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

    public TicketTypeEntity(String code, String description, double costPerTicket) {
        this.code = code;
        this.description = description;
        this.costPerTicket = costPerTicket;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String code;
    private String description;
    private double costPerTicket;

}
