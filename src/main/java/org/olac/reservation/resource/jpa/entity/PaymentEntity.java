package org.olac.reservation.resource.jpa.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.olac.reservation.resource.model.PaymentMethod;
import org.olac.reservation.resource.model.PaymentStatus;

import java.util.Date;

@Entity
@Data
@NoArgsConstructor
public class PaymentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "RESERVATION_ID", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private ReservationEntity reservation;

    private double amount;
    private PaymentStatus status;
    private PaymentMethod method;
    private String notes;
    private String enteredBy;
    private Date createdTimestamp = new Date();
}
