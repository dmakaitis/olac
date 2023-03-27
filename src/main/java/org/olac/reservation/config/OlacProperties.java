package org.olac.reservation.config;

import lombok.Data;
import org.olac.reservation.resource.model.Reservation;
import org.olac.reservation.resource.model.TicketType;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties("olac")
@Data
public class OlacProperties {

    private boolean disableSecurity = false;

    private int maxTickets;
    private String email;
    private PayPal paypal;
    private TestData testData;
    private Jwt jwt;
    private List<Article> articles;

    @Data
    public static class PayPal {
        private String apiBase;
        private String client;
        private String secret;
    }

    @Data
    public static class TestData {
        private boolean load = false;
        private List<TicketType> ticketTypes = new ArrayList<>();
        private List<Reservation> reservations = new ArrayList<>();
    }

    @Data
    public static class Jwt {
        private String key;
        private int timeoutMinutes;
    }

    @Data
    public static class Article {
        private String headline;
        private String headlineImage;
        private String textResource;
    }

}
