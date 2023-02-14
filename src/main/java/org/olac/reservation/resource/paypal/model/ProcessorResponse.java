package org.olac.reservation.resource.paypal.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ProcessorResponse {

    @JsonProperty("avs_code")
    private String avsCode;
    @JsonProperty("cvv_code")
    private String cvvCode;
    @JsonProperty("payment_advice_code")
    private String paymentAdviceCode;
    @JsonProperty("response_code")
    private String responseCode;

}
