package org.olac.reservation.resource.paypal.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class TaxInfo {

    @JsonProperty("tax_id")
    private String taxId;
    @JsonProperty("tax_id_type")
    private String taxIdType;

}
