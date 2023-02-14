package org.olac.reservation.resource.paypal.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ExchangeRate {

    @JsonProperty("source_currency")
    private String sourceCurrency;
    @JsonProperty("target_currency")
    private String targetCurrency;
    private String value;

}
