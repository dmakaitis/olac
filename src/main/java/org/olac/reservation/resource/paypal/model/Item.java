package org.olac.reservation.resource.paypal.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Item {

    /**
     * The item name or title. (required)
     */
    private String name;

    /**
     * The item quantity. Must be a whole number. (required)
     */
    private String quantity;

    /**
     * The item price or rate per unit. If you specify unit_amount, purchase_units[].amount.breakdown.item_total is
     * required. Must equal unit_amount * quantity for all items. unit_amount.value can not be a negative number.
     * (required)
     */
    @JsonProperty("unit_amount")
    private Money unitAmount;

    private String category;

    /**
     * The detailed item description.
     */
    private String description;

    /**
     * The stock keeping unit (SKU) for the item.
     */
    private String sku;

    /**
     * The item tax for each unit. If tax is specified, purchase_units[].amount.breakdown.tax_total is required. Must
     * equal tax * quantity for all items. tax.value can not be a negative number.
     */
    private Money tax;

}
