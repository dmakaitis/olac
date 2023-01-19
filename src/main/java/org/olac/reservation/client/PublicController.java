package org.olac.reservation.client;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.olac.reservation.client.form.ReservationForm;
import org.olac.reservation.client.form.TicketTypeCount;
import org.olac.reservation.resource.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.text.NumberFormat;

import static java.util.Comparator.comparing;

@Controller
@RequiredArgsConstructor
@Slf4j
public class PublicController {

    private final TicketDatastoreAccess ticketDatastoreAccess;
    private final ReservationDatastoreAccess reservationDatastoreAccess;

    @GetMapping("/")
    public String home(Model model) {
        ReservationForm form = new ReservationForm();
        form.setTicketTypeCounts(ticketDatastoreAccess.getTicketTypes().stream()
                .sorted(comparing(TicketType::getCostPerTicket).reversed())
                .map(t -> new TicketTypeCount(t.getCode(), t.getDescription(), format(t.getCostPerTicket()), 0))
                .toList());
        model.addAttribute("form", form);

        return "home";
    }

    @PostMapping("/reservation")
    public String postReservation(@Valid @ModelAttribute ReservationForm form, Model model) {
        Reservation reservation = toReservation(form);

        long reservationId = reservationDatastoreAccess.createReservation(reservation);

        model.addAttribute("reservationId", reservationId);

        return "confirmation";
    }

    private static String format(double value) {
        return NumberFormat.getCurrencyInstance().format(value);
    }

    private Reservation toReservation(ReservationForm form) {
        Reservation reservation = new Reservation();
        reservation.setFirstName(form.getFirstName());
        reservation.setLastName(form.getLastName());
        reservation.setEmail(form.getEmail());
        reservation.setPhone(form.getPhone());

        reservation.setTicketCounts(form.getTicketTypeCounts().stream()
                .map(t -> new TicketCounts(t.getTypeCode(), t.getCount()))
                .toList());

        return reservation;
    }

}
