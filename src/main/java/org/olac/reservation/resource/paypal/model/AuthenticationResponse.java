package org.olac.reservation.resource.paypal.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AuthenticationResponse {

    @JsonProperty("liability_shift")
    private String liabilityShift;
    @JsonProperty("three_d_secure")
    private ThreeDSecureAuthenticationResponse threeDSecure;

}
