package org.olac.reservation.resource.paypal.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ShippingDetail {

    private AddressPortable address;
    private Object name;
    private String type;

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

        @JsonProperty("full_name")
        private String fullName;

    }

}
