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

    private int maxTickets;
    private String email;
    private PayPal paypal;
    private TestData testData;

    @Data
    public static class PayPal {

        private String apiBase;
        private String client;
        private String secret;

    }

    @Data
    public static class TestData {
        boolean load = false;
        List<TicketType> ticketTypes = new ArrayList<>();
        List<Reservation> reservations = new ArrayList<>();
    }
}
