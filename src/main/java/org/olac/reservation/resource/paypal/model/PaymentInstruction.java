package org.olac.reservation.resource.paypal.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class PaymentInstruction {

    @JsonProperty("disbursement_mode")
    private String disbursementMode;
    @JsonProperty("payee_pricing_tier_id")
    private String payeePricingTierId;
    @JsonProperty("payee_receivable_fx_rate_id")
    private String payeeReceivableFxRateId;
    @JsonProperty("platform_fees")
    private List<PlatformFee> platformFees;

}
