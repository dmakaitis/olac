package org.olac.reservation.client.paypal;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Item {

    /**
     * The item name or title.
     */
    private String name;
    /**
     * The item quantity. Must be a whole number.
     */
    private String quantity;

    @JsonProperty("unit_amount")
    private Money unitAmount;

}
