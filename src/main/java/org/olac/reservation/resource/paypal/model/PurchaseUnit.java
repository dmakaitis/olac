package org.olac.reservation.resource.paypal.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class PurchaseUnit {

    private AmountWithBreakdown amount;
    @JsonProperty("custom_id")
    private String customId;
    private String description;
    private String id;
    @JsonProperty("invoice_id")
    private String invoiceId;
    private List<Item> items;
    private Payee payee;
    @JsonProperty("payment_instruction")
    private PaymentInstruction paymentInstruction;
    private PaymentCollection payments;
    @JsonProperty("reference_id")
    private String referenceId;
    private ShippingDetail shipping;
    @JsonProperty("soft_descriptor")
    private String softDescriptor;

}
