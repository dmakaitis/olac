package org.olac.reservation.resource.paypal.model;

import lombok.Data;

import java.util.List;

@Data
public class PaymentCollection {

    private List<AuthorizationWithAdditionalData> authorizations;
    private List<Capture> captures;
    private List<Refund> refunds;

}
