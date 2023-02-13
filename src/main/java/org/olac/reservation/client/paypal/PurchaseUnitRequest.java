package org.olac.reservation.client.paypal;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PurchaseUnitRequest {

    /**
     * <p>The total order amount with an optional breakdown that provides details, such as the total item amount, total tax amount, shipping, handling, insurance, and discounts, if any.</p>
     * <p>If you specify amount.breakdown, the amount equals item_total plus tax_total plus shipping plus handling plus insurance minus shipping_discount minus discount.</p>
     * <p>The amount must be a positive number. For listed of supported currencies and decimal precision, see the PayPal REST APIs Currency Codes.</p>
     * <p>
     * (required)
     */
    private AmountWithBreakdown amount;

    /**
     * The API caller-provided external ID. Used to reconcile client transactions with PayPal transactions. Appears in
     * transaction and settlement reports but is not visible to the payer.
     */
    @JsonProperty("custom_id")
    private String customId;

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

    /**
     * The merchant who receives payment for this transaction.
     */
    // private Payee payee;

    /**
     * Any additional payment instructions to be consider during payment processing. This processing instruction is
     * applicable for Capturing an order or Authorizing an Order.
     */
    // @JsonProperty("payment_instruction")
    // private PaymentInstruction paymentInstruction;

    /**
     * The API caller-provided external ID for the purchase unit. Required for multiple purchase units when you must
     * update the order through PATCH. If you omit this value and the order contains only one purchase unit, PayPal sets
     * this value to default.
     */
    @JsonProperty("reference_id")
    private String referenceId;

    /**
     * The name and address of the person to whom to ship the items.
     */
    // private ShippingDetail shipping;

    /**
     * The soft descriptor is the dynamic text used to construct the statement descriptor that appears on a payer's card
     * statement.
     */
    @JsonProperty("soft_descriptor")
    private String softDescriptor;

}
