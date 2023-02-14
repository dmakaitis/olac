package org.olac.reservation.resource.paypal.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CardFromRequest {

    private String expiry;
    @JsonProperty("last_digits")
    private String lastDigits;

}
