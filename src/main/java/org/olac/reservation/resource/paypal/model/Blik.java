package org.olac.reservation.resource.paypal.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Blik {

    @JsonProperty("country_code")
    private String countryCode;
    private String email;
    private String name;

}
