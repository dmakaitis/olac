package org.olac.reservation.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.servlet.http.HttpServletRequest;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.olac.reservation.config.OlacProperties;
import org.olac.reservation.manager.ReservationManager;
import org.olac.reservation.resource.model.Reservation;
import org.olac.reservation.resource.model.ReservationStatus;
import org.olac.reservation.resource.model.TicketCounts;
import org.olac.reservation.resource.model.TicketType;
import org.olac.reservation.resource.paypal.model.CreateOrderResponse;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static java.util.Comparator.comparing;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
@Slf4j
public class PublicApiController {

    private final ReservationManager reservationManager;
    private final OlacProperties properties;

    @GetMapping("client-config")
    ClientConfiguration getClientConfiguration(HttpServletRequest httpServletRequest) {
        log.debug("Retrieving client configuration");

        // Check to see if the request contains a cookie indicating we should show the login button:
        boolean showLogin = false;
        if (httpServletRequest.getCookies() != null) {
            showLogin = Arrays.stream(httpServletRequest.getCookies())
                    .anyMatch(c -> AuthApiController.COOKIE_SHOW_LOGIN.equals(c.getName()) &&
                            AuthApiController.COOKIE_VALUE_YES.equals(c.getValue()));
        }

        return ClientConfiguration.builder()
                .showLogin(showLogin)
                .enableReservations(properties.isEnableReservations())
                .payPal(ClientConfiguration.PayPayConfig.builder()
                        .apiBase(properties.getPaypal().getApiBase())
                        .clientId(properties.getPaypal().getClient())
                        .build())
                .build();
    }

    @GetMapping("ticket-types")
    List<TicketType> getTicketTypes() {
        log.debug("Retrieving ticket types");
        return reservationManager.getTicketTypes().stream()
                .sorted(comparing(TicketType::getCostPerTicket).reversed())
                .toList();
    }

    @GetMapping("new-reservation-id")
    String generateReservationId() {
        log.debug("Generating a new reservation ID");
        return UUID.randomUUID().toString();
    }

    @PostMapping("reservations")
    long createReservation(@RequestBody NewReservationRequest request) {
        log.debug("Creating a new reservation: {}", request);

        Reservation newReservation = new Reservation();
        newReservation.setReservationId(request.getReservationId());
        newReservation.setFirstName(request.getFirstName());
        newReservation.setLastName(request.getLastName());
        newReservation.setEmail(request.getEmail());
        newReservation.setPhone(request.getPhone());
        newReservation.setTicketCounts(request.getTicketCounts());

        newReservation.setStatus(ReservationStatus.PENDING_PAYMENT);

        newReservation = reservationManager.saveReservation(newReservation, request.getPayPayPayment() == null);

        if (request.getPayPayPayment() != null) {
            reservationManager.validateAndAddPayment(newReservation.getReservationId(), request.getPayPayPayment().getId());
        }

        return newReservation.getId();
    }

    @GetMapping("reservations/_available")
    boolean checkTicketAvailability(@RequestParam long ticketCount) {
        log.debug("Checking for available ticket count: {}", ticketCount);
        return reservationManager.areTicketsAvailable(ticketCount);
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClientConfiguration {
        private PayPayConfig payPal;
        @Builder.Default
        private boolean showLogin = false;
        private boolean enableReservations;

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class PayPayConfig {
            private String apiBase;
            private String clientId;
        }
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class NewReservationRequest {
        private String reservationId;
        private String firstName;
        private String lastName;
        private String email;
        private String phone;
        private List<TicketCounts> ticketCounts = new ArrayList<>();
        private CreateOrderResponse payPayPayment;
    }

}
