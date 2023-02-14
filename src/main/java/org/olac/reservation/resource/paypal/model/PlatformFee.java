package org.olac.reservation.resource.paypal.model;

import lombok.Data;

@Data
public class PlatformFee {

    private Money amount;
    private PayeeBase payee;

}
