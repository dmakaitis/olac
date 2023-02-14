package org.olac.reservation.resource.paypal.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class P24 {

    @JsonProperty("country_code")
    private String countryCode;
    private String email;
    @JsonProperty("method_description")
    private String methodDescription;
    @JsonProperty("method_id")
    private String methodId;
    private String name;
    @JsonProperty("payment_descriptor")
    private String paymentDescriptor;

}
