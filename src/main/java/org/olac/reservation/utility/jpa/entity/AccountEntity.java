package org.olac.reservation.utility.jpa.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnTransformer;

@Entity
@Table(name = "ACCOUNT")
@Data
@NoArgsConstructor
public class AccountEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(unique = true)
    private String username;
    @Column(unique = true)
    @ColumnTransformer(write = "LOWER(?)")
    private String email;
    private boolean enabled = true;
    private boolean admin = false;

}
