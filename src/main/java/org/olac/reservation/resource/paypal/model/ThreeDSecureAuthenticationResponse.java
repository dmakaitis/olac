package org.olac.reservation.resource.paypal.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ThreeDSecureAuthenticationResponse {

    @JsonProperty("authentication_status")
    private String authenticationStatus;
    @JsonProperty("enrollment_status")
    private String enrollmentStatus;

}
