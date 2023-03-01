package org.olac.reservation.utility.jpa.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Table(name = "AUDIT_EVENT")
@Data
@NoArgsConstructor
public class ReservationAuditEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String reservationId;
    private Date timestamp;
    private String username;
    @Column(length = 1024)
    private String description;

}
