package org.olac.reservation.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.olac.reservation.exception.OlacException;
import org.olac.reservation.resource.paypal.model.CreateOrderResponse;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Slf4j
public class CreateOrderResponseConverter implements Converter<String, CreateOrderResponse> {

    private final ObjectMapper mapper;

    @Override
    public CreateOrderResponse convert(String source) {
        try {
            return mapper.readValue(source, CreateOrderResponse.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse CreateOrderResponse from JSON", e);
            throw new OlacException("Failed to parse CreateOrderResponse from JSON", e);
        }
    }

}
