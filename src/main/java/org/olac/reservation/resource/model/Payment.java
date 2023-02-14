package org.olac.reservation.resource.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Payment {

    private Long id;
    private double amount;
    private PaymentStatus status;

}
