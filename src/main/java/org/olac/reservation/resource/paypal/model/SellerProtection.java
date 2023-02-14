package org.olac.reservation.resource.paypal.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class SellerProtection {

    @JsonProperty("dispute_categories")
    private List<String> disputeCategories;
    private String status;

}
