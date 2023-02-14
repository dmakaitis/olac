package org.olac.reservation.resource.paypal.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class Capture {

    private String status;
    @JsonProperty("status_details")
    private CaptureStatusDetails statusDetails;
    private Money amount;
    @JsonProperty("custom_id")
    private String customId;
    @JsonProperty("disbursement_mode")
    private String disbursementMode;
    @JsonProperty("final_capture")
    private Boolean finalCapture;
    private String id;
    @JsonProperty("invoice_id")
    private String invoiceId;
    private List<LinkDescription> links;
    @JsonProperty("processor_response")
    private ProcessorResponse processorResponse;
    @JsonProperty("seller_protection")
    private SellerProtection sellerProtection;
    @JsonProperty("seller_receivable_breakdown")
    private SellerReceivableBreakdown sellerReceivableBreakdown;
    @JsonProperty("create_time")
    private Date createTime;
    @JsonProperty("update_time")
    private Date updateTime;

}
