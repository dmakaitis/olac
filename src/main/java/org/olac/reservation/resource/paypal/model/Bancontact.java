package org.olac.reservation.resource.paypal.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Bancontact {

    private String bic;
    @JsonProperty("card_last_digits")
    private String cardLastDigits;
    @JsonProperty("country_code")
    private String countryCode;
    @JsonProperty("iban_last_chars")
    private String ibanLastChars;
    private String name;

}
