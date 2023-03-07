package org.olac.reservation.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.olac.reservation.manager.AdministrationManager;
import org.olac.reservation.resource.model.Page;
import org.olac.reservation.resource.model.PageRequest;
import org.olac.reservation.resource.model.Reservation;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/event")
@RequiredArgsConstructor
@Slf4j
public class EventApiController {

    private final AdministrationManager administrationManager;

    @GetMapping("reservations")
    Page<Reservation> getReservations(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int perPage,
            @RequestParam(defaultValue = "reservationTimestamp") String sortBy,
            @RequestParam(defaultValue = "true") boolean desc
    ) {
        log.debug("Retrieving reservations - page: {}, perPage: {}, sortBy: {}, desc: {}", page, perPage, sortBy, desc);

        PageRequest pageRequest = PageRequest.builder()
                .page(page)
                .itemsPerPage(perPage)
                .sortBy("null".equals(sortBy) ? null : sortBy)
                .descending(desc)
                .build();

        return administrationManager.getReservations(pageRequest);
    }

    @PutMapping("reservations/{reservationId}")
    public void saveReservation(@PathVariable String reservationId, @RequestBody Reservation reservation) {
        log.debug("Updating reservation {} => {}", reservationId, reservation);
        administrationManager.saveReservation(reservation);
    }

}
