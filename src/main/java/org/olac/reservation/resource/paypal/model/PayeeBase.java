package org.olac.reservation.resource.paypal.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PayeeBase {

    @JsonProperty("email_address")
    private String emailAddress;
    @JsonProperty("merchant_id")
    private String merchantId;

}
