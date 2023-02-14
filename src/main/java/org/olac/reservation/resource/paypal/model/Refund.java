package org.olac.reservation.resource.paypal.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class Refund {

    private String status;
    @JsonProperty("status_details")
    private RefundStatusDetails statusDetails;
    private Money amount;
    private String id;
    @JsonProperty("invoice_id")
    private String invoiceId;
    private List<LinkDescription> links;
    @JsonProperty("note_to_payor")
    private String noteToPayor;
    @JsonProperty("seller_payable_breakdown")
    private SellerPayableBreakdown sellerPayableBreakdown;
    @JsonProperty("create_time")
    private Date createTime;
    @JsonProperty("update_time")
    private Date updateTime;

}
