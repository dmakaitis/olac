package org.olac.reservation.resource.paypal.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AuthorizationWithAdditionalData {

    @JsonProperty("processor_response")
    private ProcessorResponse processorResponse;

}
