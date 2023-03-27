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
import java.util.ArrayList;
import java.util.List;

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

        List<TicketType> ticketTypes = administrationManager.getTicketTypes();

        try (CSVPrinter printer = new CSVPrinter(builder, CSVFormat.Builder.create()
                .setDelimiter(',')
                .setQuote('"')
                .setRecordSeparator("\r\n")
                .setIgnoreEmptyLines(true)
                .setDuplicateHeaderMode(DuplicateHeaderMode.ALLOW_ALL)
                .setHeader(getHeaders(ticketTypes))
                .build())) {
            Page<Reservation> page = administrationManager.getReservations(filter, PageRequest.builder()
                    .page(0)
                    .itemsPerPage(3)
                    .sortBy(sort)
                    .descending(desc)
                    .build());
            while (page.getPageSize() > 0) {
                page = addPageToOutput(ticketTypes, sort, desc, filter, printer, page);
            }
        }

        return builder.toString();
    }

    private String[] getHeaders(List<TicketType> ticketTypes) {
        List<String> headers = new ArrayList<>();

        headers.add("Reservation Number");
        headers.add("Date/Time Reserved");
        headers.add("First Name");
        headers.add("Last Name");
        headers.add("Email");
        headers.add("Phone");
        headers.add("Status");

        for (TicketType ticketType : ticketTypes) {
            headers.add(ticketType.getDescription());
        }
        headers.add("Total Tickets");

        headers.add("Amount Due");
        headers.add("Payment Method");
        headers.add("Payment Received");
        headers.add("Payment Status");
        headers.add("Payment Amount");
        headers.add("Payment Notes");

        return headers.toArray(new String[0]);
    }

    private Page<Reservation> addPageToOutput(List<TicketType> ticketTypes, String sortBy, boolean desc, String filter, CSVPrinter printer, Page<Reservation> page) throws IOException {
        for (Reservation r : page.getData()) {
            List<Object> reservationData = getReservationData(ticketTypes, r);
            if (r.getPayments().isEmpty()) {
                printer.printRecord(addPaymentData(reservationData, null));
            } else {
                for (Payment p : r.getPayments()) {
                    printer.printRecord(addPaymentData(reservationData, p));

                    // For subsequent rows for the same reservation, blank out the reservation data...
                    for (int i = 0; i < reservationData.size(); i++) {
                        reservationData.set(i, "");
                    }
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

    private List<Object> addPaymentData(List<Object> reservationData, Payment p) {
        List<Object> rVal = new ArrayList<>(reservationData);

        rVal.add(p == null ? "" : p.getMethod());
        rVal.add(p == null ? "" : p.getCreatedTimestamp());
        rVal.add(p == null ? "" : p.getStatus());
        rVal.add(p == null ? "" : p.getAmount());
        rVal.add(p == null ? "" : p.getNotes());

        return rVal;
    }

    private List<Object> getReservationData(List<TicketType> ticketTypes, Reservation reservation) {
        List<Object> data = new ArrayList<>();

        data.add(reservation.getId());
        data.add(reservation.getReservationTimestamp());
        data.add(reservation.getFirstName());
        data.add(reservation.getLastName());
        data.add(reservation.getEmail());
        data.add(reservation.getPhone());
        data.add(reservation.getStatus());

        for (TicketType ticketType : ticketTypes) {
            data.add(reservation.getTicketCounts().stream()
                    .filter(t -> ticketType.getCode().equals(t.getTicketTypeCode()))
                    .mapToInt(TicketCounts::getCount)
                    .sum());
        }
        data.add(reservation.getTicketCounts().stream()
                .mapToInt(TicketCounts::getCount)
                .sum());

        data.add(reservation.getAmountDue());

        return data;
    }


}
