package org.olac.reservation.resource.paypal.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CardResponse {

    @JsonProperty("authentication_result")
    private AuthenticationResponse authenticationResult;
    private String brand;
    private String expiry;
    @JsonProperty("from_request")
    private CardFromRequest fromRequest;
    @JsonProperty("last_digits")
    private String lastDigits;
    private String name;
    private String type;

}
