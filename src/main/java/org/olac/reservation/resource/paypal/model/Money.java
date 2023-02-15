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
public class Money {

    /**
     * The three-character ISO-4217 currency code that identifies the currency. (required)
     */
    @JsonProperty("currency_code")
    private String currencyCode;
    /**
     * The value, which might be:
     * <ul>
     * <li>An integer for currencies like JPY that are not typically fractional.</li>
     * <li>A decimal fraction for currencies like TND that are subdivided into thousandths.</li>
     * </ul>
     * For the required number of decimal places for a currency code, see Currency Codes.
     * (required)
     */
    private Double value;

}
