package org.olac.reservation.resource.paypal.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PaypalWalletResponse {

    @JsonProperty("account_id")
    private String accountId;
    private AddressPortable address;
    @JsonProperty("birth_date")
    private String birthDate;
    @JsonProperty("email_address")
    private String emailAddress;
    private Name name;
    @JsonProperty("phone_number")
    private Phone phoneNumber;
    @JsonProperty("phone_type")
    private String phoneType;
    @JsonProperty("tax_info")
    private TaxInfo taxInfo;

    @Data
    public static class AddressPortable {

        @JsonProperty("country_code")
        private String countryCode;
        @JsonProperty("address_line_1")
        private String addressLine1;
        @JsonProperty("address_line_2")
        private String addressLine2;
        @JsonProperty("admin_area_1")
        private String adminArea1;
        @JsonProperty("admin_area_2")
        private String adminArea2;
        @JsonProperty("postal_code")
        private String postalCode;

    }

    @Data
    public static class Name {

        @JsonProperty("given_name")
        private String givenName;
        private String surname;

    }

    @Data
    public static class Phone {

        @JsonProperty("national_number")
        private String nationalNumber;

    }

}
