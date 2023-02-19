package org.olac.reservation.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.olac.reservation.manager.ReservationManager;
import org.olac.reservation.resource.model.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static java.util.Arrays.asList;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toMap;

@Controller()
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final ReservationManager reservationManager;

    private List<TicketType> getSortedTicketTypes() {
        return reservationManager.getTicketTypes().stream()
                .sorted(comparing(TicketType::getCostPerTicket).reversed())
                .toList();
    }

    @GetMapping("/admin/reservations")
    public String getAllReservations(Model model) {
        List<TicketType> ticketTypes = getSortedTicketTypes();

        // Calculate headers:

        List<String> headers = new ArrayList<>();
        headers.add("Number");
        headers.add("Last Name");
        headers.add("First Name");
        headers.add("Email");
        headers.add("Phone");
        headers.add("Status");

        headers.add("Total Due");
        headers.add("Total Paid");

        model.addAttribute("headers", headers);

        // Calculate all reservation rows:

        List<ReservationRowValues> rows = reservationManager.getReservations().stream()
                .sorted(comparing(Reservation::getLastName)
                        .thenComparing(Reservation::getFirstName)
                        .thenComparing(Reservation::getId))
                .map(r -> {
                    List<String> values = new ArrayList<>(asList(
                            r.getId().toString(),
                            r.getLastName(),
                            r.getFirstName(),
                            r.getEmail(),
                            r.getPhone(),
                            r.getStatus() == null ? "" : r.getStatus().toString()
                    ));

                    // Add ticket type counts:

                    Map<String, TicketCounts> typesMap = r.getTicketCounts().stream()
                            .collect(toMap(TicketCounts::getTicketTypeCode, Function.identity()));

                    double total = 0.0;

                    for (TicketType ticketType : ticketTypes) {
                        Optional<TicketCounts> counts = Optional.ofNullable(typesMap.get(ticketType.getCode()));
                        int amt = counts.map(TicketCounts::getCount).orElse(0);
                        total += amt * ticketType.getCostPerTicket();
                    }

                    // Add total due and total paid:

                    values.add(PublicController.format(total));
                    values.add(PublicController.format(r.getPayments().stream()
                            .filter(p -> p.getStatus() == PaymentStatus.SUCCESSFUL)
                            .mapToDouble(Payment::getAmount)
                            .sum()));

                    return new ReservationRowValues(r.getReservationId(), values);
                })
                .toList();

        model.addAttribute("rows", rows);

        return "admin/reservations";
    }

    @GetMapping("/admin/reservation")
    public String viewReservation(@RequestParam("id") String reservationId, Model model) {
        Optional<Reservation> reservation = reservationManager.getReservation(reservationId);

        if (!reservation.isPresent()) {
            return "redirect:/admin/reservations";
        }

        Map<String, TicketType> typeMap = reservationManager.getTicketTypes().stream()
                .collect(toMap(TicketType::getCode, Function.identity()));
        ReservationDetailForm form = new ReservationDetailForm(reservation.get(), typeMap);

        model.addAttribute("form", form);
        return "admin/reservation";
    }

    @PostMapping("/admin/reservation")
    public String saveReservation(ReservationDetailForm form) {
        log.info("Saving reservation: {}", form);
        reservationManager.getReservation(form.getReservationId()).ifPresent(reservation -> {
            reservation.setFirstName(form.getFirstName());
            reservation.setLastName(form.getLastName());
            reservation.setEmail(form.getEmail());
            reservation.setPhone(form.getPhone());
            reservation.setStatus(form.getStatus());

            reservationManager.saveReservation(reservation);
        });
        return "redirect:/admin/reservations";
    }

    @Data
    @AllArgsConstructor
    public static class ReservationRowValues {

        private String id;
        private List<String> values;

    }

    @Data
    @NoArgsConstructor
    public static class ReservationDetailForm {

        private long id;
        private String reservationId;
        private String firstName;
        private String lastName;
        private String email;
        private String phone;
        private ReservationStatus status;
        private List<TicketCount> ticketCounts;
        private List<Payment> payments;

        public ReservationDetailForm(Reservation reservation, Map<String, TicketType> ticketTypes) {
            this.reservationId = reservation.getReservationId();
            this.firstName = reservation.getFirstName();
            this.lastName = reservation.getLastName();
            this.email = reservation.getEmail();
            this.phone = reservation.getPhone();
            this.status = reservation.getStatus();

            this.ticketCounts = reservation.getTicketCounts().stream()
                    .map(t -> new TicketCount(t.getTicketTypeCode(), ticketTypes.get(t.getTicketTypeCode()).getDescription(), t.getCount()))
                    .toList();
            this.payments = reservation.getPayments().stream()
                    .map(p -> new Payment(p.getAmount(), p.getStatus(), p.getMethod(), p.getNotes(), p.getEnteredBy()))
                    .toList();
        }

        @Data
        @AllArgsConstructor
        public static class TicketCount {
            private String code;
            private String description;
            private int count;
        }

        @Data
        @AllArgsConstructor
        public static class Payment {
            private double amount;
            private PaymentStatus status;
            private PaymentMethod method;
            private String notes;
            private String enteredBy;
        }
    }

}
