package org.olac.reservation.client.paypal;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AmountBreakdown {

    /**
     * The subtotal for all items. Required if the request includes purchase_units[].items[].unit_amount. Must equal the
     * sum of (items[].unit_amount * items[].quantity) for all items. item_total.value can not be a negative number.
     */
    @JsonProperty("item_total")
    private Money itemTotal;

}
