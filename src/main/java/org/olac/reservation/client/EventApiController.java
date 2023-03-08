package org.olac.reservation.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.DuplicateHeaderMode;
import org.olac.reservation.manager.AdministrationManager;
import org.olac.reservation.resource.model.*;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/event")
@RequiredArgsConstructor
@Slf4j
public class EventApiController {

    private final AdministrationManager administrationManager;

    @GetMapping("reservations")
    Page<Reservation> getReservations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int perPage,
            @RequestParam(defaultValue = "reservationTimestamp") String sortBy,
            @RequestParam(defaultValue = "true") boolean desc,
            @RequestParam(defaultValue = "") String filter
    ) {
        log.debug("Retrieving reservations - page: {}, perPage: {}, sortBy: {}, desc: {}, filter: {}", page, perPage, sortBy, desc, filter);

        PageRequest pageRequest = PageRequest.builder()
                .page(page)
                .itemsPerPage(perPage)
                .sortBy("null".equals(sortBy) ? null : sortBy)
                .descending(desc)
                .build();

        return administrationManager.getReservations(filter, pageRequest);
    }

    @PutMapping("reservations/{reservationId}")
    public void saveReservation(@PathVariable String reservationId, @RequestBody Reservation reservation) {
        log.debug("Updating reservation {} => {}", reservationId, reservation);
        administrationManager.saveReservation(reservation);
    }

    @GetMapping(value = "reservations.csv", produces = "text/csv")
    String getReservationsAsCsv(
            @RequestParam(defaultValue = "reservationTimestamp") String sortBy,
            @RequestParam(defaultValue = "true") boolean desc,
            @RequestParam(defaultValue = "") String filter
    ) throws IOException {
        String sort = "null".equals(sortBy) ? null : sortBy;
        StringBuilder builder = new StringBuilder();

        try (CSVPrinter printer = new CSVPrinter(builder, CSVFormat.Builder.create()
                .setDelimiter(',')
                .setQuote('"')
                .setRecordSeparator("\r\n")
                .setIgnoreEmptyLines(true)
                .setDuplicateHeaderMode(DuplicateHeaderMode.ALLOW_ALL)
                .setHeader(
                        "Reservation Number",
                        "Date/Time Reserved",
                        "First Name",
                        "Last Name",
                        "Email",
                        "Phone",
                        "Status",
                        "Tickets",
                        "Amount Due",
                        "Payment Method",
                        "Payment Received",
                        "Payment Status",
                        "Payment Amount",
                        "Payment Notes")
                .build())) {
            Page<Reservation> page = administrationManager.getReservations(filter, PageRequest.builder()
                    .page(0)
                    .itemsPerPage(3)
                    .sortBy(sort)
                    .descending(desc)
                    .build());
            while (page.getPageSize() > 0) {
                page = addPageToOutput(sort, desc, filter, printer, page);
            }
        }

        return builder.toString();
    }

    private Page<Reservation> addPageToOutput(String sortBy, boolean desc, String filter, CSVPrinter printer, Page<Reservation> page) throws IOException {
        for (Reservation r : page.getData()) {
            if (r.getPayments().isEmpty()) {
                printer.printRecord(
                        r.getId(),
                        r.getReservationTimestamp(),
                        r.getFirstName(),
                        r.getLastName(),
                        r.getEmail(),
                        r.getPhone(),
                        r.getStatus(),
                        r.getTicketCounts().stream()
                                .mapToInt(TicketCounts::getCount)
                                .sum(),
                        r.getAmountDue(),
                        "",
                        "",
                        "",
                        "",
                        "");
            } else {
                boolean first = true;

                for (Payment p : r.getPayments()) {
                    if (first) {
                        printer.printRecord(
                                r.getId(),
                                r.getReservationTimestamp(),
                                r.getFirstName(),
                                r.getLastName(),
                                r.getEmail(),
                                r.getPhone(),
                                r.getStatus(),
                                r.getTicketCounts().stream()
                                        .mapToInt(TicketCounts::getCount)
                                        .sum(),
                                r.getAmountDue(),
                                p.getMethod(),
                                p.getCreatedTimestamp(),
                                p.getStatus(),
                                p.getAmount(),
                                p.getNotes());
                    } else {
                        printer.printRecord(
                                "",
                                "",
                                "",
                                "",
                                "",
                                "",
                                "",
                                "",
                                "",
                                p.getMethod(),
                                p.getCreatedTimestamp(),
                                p.getStatus(),
                                p.getAmount(),
                                p.getNotes());
                    }

                    first = false;
                }
            }
        }

        page = administrationManager.getReservations(filter, PageRequest.builder()
                .page(page.getPageNumber() + 1)
                .itemsPerPage(page.getItemsPerPage())
                .sortBy(sortBy)
                .descending(desc)
                .build());
        return page;
    }

}
