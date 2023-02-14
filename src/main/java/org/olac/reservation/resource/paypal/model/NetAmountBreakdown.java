package org.olac.reservation.resource.paypal.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class NetAmountBreakdown {

    @JsonProperty("converted_amount")
    private Money convertedAmount;
    @JsonProperty("exchange_rate")
    private ExchangeRate exchangeRate;
    @JsonProperty("payable_amount")
    private Money payableAmount;

}
