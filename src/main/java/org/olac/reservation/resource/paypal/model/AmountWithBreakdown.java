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
public class AmountWithBreakdown {

    /**
     * The three-character ISO-4217 currency code that identifies the currency.
     * <p>
     * (required)
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
     * <p>
     * (required)
     */
    private String value;

    /**
     * The breakdown of the amount. Breakdown provides details such as total item amount, total tax amount, shipping,
     * handling, insurance, and discounts, if any.
     */
    private AmountBreakdown breakdown;

}
