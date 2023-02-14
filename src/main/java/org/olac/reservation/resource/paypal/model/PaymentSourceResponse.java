package org.olac.reservation.resource.paypal.model;

import lombok.Data;

@Data
public class PaymentSourceResponse {

    private Bancontact banContact;
    private Blik blik;
    private CardResponse card;
    private Eps eps;
    private Giropay giropay;
    private Ideal ideal;
    private MyBank mybank;
    private P24 p24;
    private PaypalWalletResponse paypal;
    private Sofort sofort;
    private Trustly trustly;

}
