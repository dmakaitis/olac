package org.olac.reservation.resource.model;

import lombok.*;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    private Long id;
    private double amount;
    private PaymentStatus status;
    private PaymentMethod method;
    private String notes;
    private String enteredBy;
    @Builder.Default
    @EqualsAndHashCode.Exclude
    private Date createdTimestamp = new Date();

}
