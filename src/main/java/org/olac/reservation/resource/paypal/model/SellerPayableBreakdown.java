package org.olac.reservation.resource.paypal.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class SellerPayableBreakdown {

    @JsonProperty("gross_amount")
    private Money grossAmount;
    @JsonProperty("net_amount")
    private Money netAmount;
    @JsonProperty("net_amount_breakdown")
    private List<NetAmountBreakdown> netAmountBreakdown;
    @JsonProperty("net_amount_in_receivable_currency")
    private Money netAmountInReceivableCurrency;
    @JsonProperty("paypal_fee")
    private Money paypalFee;
    @JsonProperty("paypal_fee_in_receivable_currency")
    private Money paypalFeeInReceivableCurrency;
    @JsonProperty("platform_fees")
    private List<PlatformFee> platformFees;
    @JsonProperty("total_refunded_amount")
    private Money totalRefundedAmount;

}
