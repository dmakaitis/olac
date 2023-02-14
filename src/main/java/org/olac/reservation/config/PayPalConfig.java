package org.olac.reservation.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Feign;
import feign.auth.BasicAuthRequestInterceptor;
import feign.form.FormEncoder;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import org.olac.reservation.resource.paypal.OAuth2RequestInterceptor;
import org.olac.reservation.resource.paypal.OAuthClient;
import org.olac.reservation.resource.paypal.PayPalClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PayPalConfig {

    @Bean
    public OAuthClient oAuthClient(OlacProperties properties, ObjectMapper mapper) {
        return Feign.builder()
                .encoder(new FormEncoder(new JacksonEncoder(mapper)))
                .decoder(new JacksonDecoder(mapper))
                .requestInterceptor(new BasicAuthRequestInterceptor(properties.getPaypalClient(), properties.getPaypalSecret()))
                .target(OAuthClient.class, "https://api-m.sandbox.paypal.com");
    }

    @Bean
    public PayPalClient payPalClient(OAuthClient oAuthClient, ObjectMapper mapper) {
        return Feign.builder()
                .encoder(new JacksonEncoder(mapper))
                .decoder(new JacksonDecoder(mapper))
                .requestInterceptor(new OAuth2RequestInterceptor(oAuthClient))
                .target(PayPalClient.class, "https://api.sandbox.paypal.com");
    }

}
