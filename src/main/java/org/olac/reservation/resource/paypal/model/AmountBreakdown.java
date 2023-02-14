package org.olac.reservation.resource.paypal.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AmountBreakdown {

    /**
     * The discount for all items within a given purchase_unit. discount.value can not be a negative number.
     */
    private Money discount;

    /**
     * The handling fee for all items within a given purchase_unit. handling.value can not be a negative number.
     */
    private Money handling;

    /**
     * The insurance fee for all items within a given purchase_unit. insurance.value can not be a negative number.
     */
    private Money insurance;

    /**
     * The subtotal for all items. Required if the request includes purchase_units[].items[].unit_amount. Must equal the
     * sum of (items[].unit_amount * items[].quantity) for all items. item_total.value can not be a negative number.
     */
    @JsonProperty("item_total")
    private Money itemTotal;

    /**
     * The shipping fee for all items within a given purchase_unit. shipping.value can not be a negative number.
     */
    private Money shipping;

    /**
     * The shipping discount for all items within a given purchase_unit. shipping_discount.value can not be a negative
     * number.
     */
    @JsonProperty("shipping_discount")
    private Money shippingDiscount;

    /**
     * The total tax for all items. Required if the request includes purchase_units.items.tax. Must equal the sum of
     * (items[].tax * items[].quantity) for all items. tax_total.value can not be a negative number.
     */
    @JsonProperty("tax_total")
    private Money taxTotal;

}
