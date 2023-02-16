package org.olac.reservation.resource.model;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class Payment {

    private Long id;
    private double amount;
    private PaymentStatus status;
    private PaymentMethod method;
    private String notes;
    private String enteredBy;
    @Builder.Default
    private Date createdTimestamp = new Date();

}
