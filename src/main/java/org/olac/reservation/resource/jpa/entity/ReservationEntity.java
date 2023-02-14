package org.olac.reservation.resource.jpa.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
public class ReservationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String reservationId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private Date reservationTimestamp;
    private double amountDue;

    @OneToMany(cascade = CascadeType.ALL)
    private Set<ReservationTicketsEntity> tickets;
    @OneToMany(cascade = CascadeType.ALL)
    private Set<PaymentEntity> payments;

}
