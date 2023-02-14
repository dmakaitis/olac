package org.olac.reservation.resource.paypal.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class SellerReceivableBreakdown {

    @JsonProperty("gross_amount")
    private Money grossAmount;
    @JsonProperty("exchange_rate")
    private ExchangeRate exchangeRate;
    @JsonProperty("net_amount")
    private Money netAmount;
    @JsonProperty("paypal_fee")
    private Money paypalFee;
    @JsonProperty("paypal_fee_in_receivable_currency")
    private Money paypalFeeInReceivableCurrency;
    @JsonProperty("platform_fees")
    private List<PlatformFee> platformFees;
    @JsonProperty("receivable_amount")
    private Money receivableAmount;

}
