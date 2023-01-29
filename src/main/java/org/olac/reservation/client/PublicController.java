package org.olac.reservation.client;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.olac.reservation.client.form.ReservationForm;
import org.olac.reservation.client.form.TicketTypeCount;
import org.olac.reservation.resource.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.text.NumberFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.Collections.emptyList;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toMap;

@Controller
@RequiredArgsConstructor
@Slf4j
public class PublicController {

    private final TicketDatastoreAccess ticketDatastoreAccess;
    private final ReservationDatastoreAccess reservationDatastoreAccess;
    private final NotificationAccess notificationAccess;

    @GetMapping("/")
    public String home(ReservationForm reservationForm, HttpSession session) {
        ReservationForm oldForm = (ReservationForm) session.getAttribute("reservationForm");
        if (oldForm != null) {
            reservationForm.setFirstName(oldForm.getFirstName());
            reservationForm.setLastName(oldForm.getLastName());
            reservationForm.setEmail(oldForm.getEmail());
            reservationForm.setPhone(oldForm.getPhone());
            reservationForm.setTicketTypeCounts(oldForm.getTicketTypeCounts());
        }

        fixTicketTypes(reservationForm);

        return "home";
    }

    @PostMapping("/")
    public String postReservation(@Valid ReservationForm reservationForm, BindingResult result, HttpSession session) {
        fixTicketTypes(reservationForm);

        if (result.hasErrors()) {
            return "home";
        }

        session.setAttribute("reservationForm", reservationForm);

        return "confirmation";
    }

    @GetMapping("/confirm")
    public String confirmReservation(HttpSession session, Model model) {
        ReservationForm reservationForm = (ReservationForm) session.getAttribute("reservationForm");

        Reservation reservation = toReservation(reservationForm);

        long reservationId = reservationDatastoreAccess.createReservation(reservation);
        notificationAccess.sendReservationConfirmation(reservationId, reservation);

        model.addAttribute("reservationId", reservationId);
        model.addAttribute("amount", reservationForm.getTotal());

        return "payment";
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

    private void fixTicketTypes(ReservationForm reservationForm) {
        List<TicketTypeCount> oldTypes = Optional.ofNullable(reservationForm.getTicketTypeCounts())
                .orElse(emptyList());

        Map<String, Integer> typeCounts = oldTypes.stream()
                .collect(toMap(TicketTypeCount::getTypeCode, TicketTypeCount::getCount));

        AtomicReference<Double> grandTotal = new AtomicReference<>(0.0);

        reservationForm.setTicketTypeCounts(ticketDatastoreAccess.getTicketTypes().stream()
                .sorted(comparing(TicketType::getCostPerTicket).reversed())
                .map(t -> {
                    int count = typeCounts.getOrDefault(t.getCode(), 0);
                    double total = count * t.getCostPerTicket();

                    grandTotal.accumulateAndGet(total, (a, b) -> a + b);

                    return new TicketTypeCount(t.getCode(), t.getDescription(), format(t.getCostPerTicket()), count, format(total));
                })
                .toList());

        reservationForm.setTotal(format(grandTotal.get()));
    }

}
