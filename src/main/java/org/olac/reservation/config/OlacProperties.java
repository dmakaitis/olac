package org.olac.reservation.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("olac")
@Data
public class OlacProperties {

    private int maxTickets;
    private String email;
    private PayPal paypal;

    @Data
    public static class PayPal {

        private String apiBase;
        private String client;
        private String secret;

    }

}
