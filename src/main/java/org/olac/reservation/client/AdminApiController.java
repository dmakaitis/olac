package org.olac.reservation.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.olac.reservation.manager.ReservationManager;
import org.olac.reservation.resource.model.Reservation;
import org.olac.reservation.resource.model.TicketType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static java.util.Comparator.comparing;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminApiController {

    private final ReservationManager reservationManager;

    @GetMapping(value = "ticket-types", produces = "application/json")
    List<TicketType> getTicketTypes() {
        log.info("Retrieving ticket types for admin");
        return reservationManager.getTicketTypes().stream()
                .sorted(comparing(TicketType::getCostPerTicket).reversed())
                .toList();
    }

    @GetMapping(value = "reservations", produces = "application/json")
    List<Reservation> getReservations() {
        log.info("Retrieving reservations for admin");
        return reservationManager.getReservations().stream()
                .sorted(comparing(Reservation::getLastName)
                        .thenComparing(Reservation::getFirstName)
                        .thenComparing(Reservation::getId))
                .toList();
    }

}
