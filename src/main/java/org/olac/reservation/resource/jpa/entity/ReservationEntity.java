package org.olac.reservation.resource.jpa.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Entity
@Data
@NoArgsConstructor
public class ReservationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id;

    private String firstName;
    private String lastName;
    private String email;
    private String phone;

    @OneToMany(cascade = CascadeType.ALL)
    private Set<ReservationTicketsEntity> tickets;

}
