package org.olac.reservation.client.paypal;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class PurchaseUnitRequest {

    /**
     * <p>The total order amount with an optional breakdown that provides details, such as the total item amount, total tax amount, shipping, handling, insurance, and discounts, if any.</p>
     * <p>If you specify amount.breakdown, the amount equals item_total plus tax_total plus shipping plus handling plus insurance minus shipping_discount minus discount.</p>
     * <p>The amount must be a positive number. For listed of supported currencies and decimal precision, see the PayPal REST APIs Currency Codes.</p>
     */
    private AmountWithBreakdown amount;

    /**
     * The purchase description.
     */
    private String description;

    /**
     * The API caller-provided external invoice ID for this order.
     */
    @JsonProperty("invoice_id")
    private String invoiceId;

    /**
     * An array of items that the customer purchases from the merchant.
     */
    private List<Item> items;

}
