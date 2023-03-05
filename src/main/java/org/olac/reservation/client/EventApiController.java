package org.olac.reservation.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.olac.reservation.manager.AdministrationManager;
import org.olac.reservation.resource.model.Reservation;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static java.util.Comparator.comparing;

@RestController
@RequestMapping("/api/event")
@RequiredArgsConstructor
@Slf4j
public class EventApiController {

    private final AdministrationManager administrationManager;

    @GetMapping("reservations")
    List<Reservation> getReservations() {
        log.debug("Retrieving reservations");
        return administrationManager.getReservations().stream()
                .sorted(comparing(Reservation::getLastName)
                        .thenComparing(Reservation::getFirstName)
                        .thenComparing(Reservation::getId))
                .toList();
    }

    @PutMapping("reservations/{reservationId}")
    public void saveReservation(@PathVariable String reservationId, @RequestBody Reservation reservation) {
        log.debug("Updating reservation {} => {}", reservationId, reservation);
        administrationManager.saveReservation(reservation);
    }

}
