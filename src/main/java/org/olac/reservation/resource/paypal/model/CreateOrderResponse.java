package org.olac.reservation.resource.paypal.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateOrderResponse {

    @JsonProperty("create_time")
    private Date createTime;
    private String id;
    private String intent;
    private List<LinkDescription> links;
    private PaymentSourceResponse paymentSource;
    @JsonProperty("processing_instruction")
    private String processingInstruction;
    @JsonProperty("purchase_units")
    private List<PurchaseUnit> purchaseUnits;
    private String status;
    @JsonProperty("update_time")
    private Date updateTime;

    public static CreateOrderResponse valueOf(String json) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, CreateOrderResponse.class);
    }

}
