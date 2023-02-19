package org.olac.reservation.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.olac.reservation.manager.AdministrationManager;
import org.olac.reservation.resource.model.Reservation;
import org.olac.reservation.resource.model.TicketType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static java.util.Comparator.comparing;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminApiController {

    private final AdministrationManager administrationManager;

    @GetMapping("ticket-types")
    List<TicketType> getTicketTypes() {
        log.info("Retrieving ticket types for admin");
        return administrationManager.getTicketTypes().stream()
                .sorted(comparing(TicketType::getCostPerTicket).reversed())
                .toList();
    }

    @PostMapping("ticket-types")
    void saveTicketType(@RequestBody TicketType type) {
        administrationManager.saveTicketType(type);
    }

    @DeleteMapping("ticket-types")
    void deleteTicketType(@RequestParam String code) {
        administrationManager.deleteTicketType(code);
    }

    @GetMapping("reservations")
    List<Reservation> getReservations() {
        log.info("Retrieving reservations for admin");
        return administrationManager.getReservations().stream()
                .sorted(comparing(Reservation::getLastName)
                        .thenComparing(Reservation::getFirstName)
                        .thenComparing(Reservation::getId))
                .toList();
    }

}
