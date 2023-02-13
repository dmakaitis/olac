package org.olac.reservation.client;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.olac.reservation.client.form.ReservationForm;
import org.olac.reservation.client.form.TicketTypeCount;
import org.olac.reservation.client.paypal.*;
import org.olac.reservation.manager.ReservationManager;
import org.olac.reservation.resource.Reservation;
import org.olac.reservation.resource.TicketCounts;
import org.olac.reservation.resource.TicketType;
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
import static java.util.Collections.singletonList;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toMap;

@Controller
@RequiredArgsConstructor
@Slf4j
public class PublicController {

    private static final String ATTRIB_RESERVATION_FORM = "reservationForm";
    public static final String TEMPLATE_HOME = "home";
    public static final String TEMPLATE_TICKETS = "tickets";
    public static final String TEMPLATE_PAYMENT = "payment";
    public static final String TEMPLATE_CONFIRMATION = "confirmation";

    private final ReservationManager reservationManager;

    @GetMapping("/")
    public String home() {
        return TEMPLATE_HOME;
    }

    @GetMapping("/tickets")
    public String tickets(ReservationForm reservationForm, HttpSession session) {
        ReservationForm oldForm = (ReservationForm) session.getAttribute(ATTRIB_RESERVATION_FORM);
        if (oldForm != null) {
            reservationForm.setFirstName(oldForm.getFirstName());
            reservationForm.setLastName(oldForm.getLastName());
            reservationForm.setEmail(oldForm.getEmail());
            reservationForm.setPhone(oldForm.getPhone());
            reservationForm.setTicketTypeCounts(oldForm.getTicketTypeCounts());
        }

        fixTicketTypes(reservationForm);

        session.removeAttribute(ATTRIB_RESERVATION_FORM);

        return TEMPLATE_TICKETS;
    }

    @PostMapping("/tickets")
    public String postReservation(@Valid ReservationForm reservationForm, BindingResult result, Model model, HttpSession session) {
        fixTicketTypes(reservationForm);

        if (result.hasErrors()) {
            return TEMPLATE_TICKETS;
        }

        if (!reservationManager.areTicketsAvailable(reservationForm.getTicketTypeCounts().stream()
                .mapToLong(TicketTypeCount::getCount)
                .sum())) {
            model.addAttribute("ticketCountError", "Not enough tickets are available");
            return TEMPLATE_TICKETS;
        }

        session.setAttribute(ATTRIB_RESERVATION_FORM, reservationForm);

        return TEMPLATE_CONFIRMATION;
    }

    @GetMapping("/confirm")
    public String confirmReservation(HttpSession session, Model model) {
        ReservationForm reservationForm = (ReservationForm) session.getAttribute(ATTRIB_RESERVATION_FORM);

        Reservation reservation = toReservation(reservationForm);

        long reservationId = reservationManager.createReservation(reservation);

        model.addAttribute("reservationId", reservationId);
        model.addAttribute("amount", reservationForm.getTotal());

        List<PurchaseUnitRequest> purchaseUnits = toPurchaseUnits(reservationId, reservationForm);
        model.addAttribute("purchaseUnits", purchaseUnits);

        session.removeAttribute(ATTRIB_RESERVATION_FORM);

        return TEMPLATE_PAYMENT;
    }

    private List<PurchaseUnitRequest> toPurchaseUnits(long reservationId, ReservationForm reservationForm) {
        String totalAmount = reservationForm.getTotal().replaceAll("[$]", "");

        PurchaseUnitRequest purchaseUnit = new PurchaseUnitRequest();

        purchaseUnit.setAmount(new AmountWithBreakdown(
                "USD",
                totalAmount,
                new AmountBreakdown(new Money("USD", totalAmount))
        ));
        purchaseUnit.setDescription("Omaha Lithuanian Community's 70th Anniversary Celebration on Saturday, April 22, 2023");
        purchaseUnit.setInvoiceId(Long.toString(reservationId));
        purchaseUnit.setItems(reservationForm.getTicketTypeCounts().stream()
                .map(this::toItem)
                .toList());

        return singletonList(purchaseUnit);
    }

    private Item toItem(TicketTypeCount ticketTypeCount) {
        Item item = new Item();

        item.setName(ticketTypeCount.getDescription());
        item.setUnitAmount(new Money("USD", ticketTypeCount.getCostPerTicket().replaceAll("[$]", "")));
        item.setQuantity(ticketTypeCount.getCount().toString());

        return item;
    }

    public static String format(double value) {
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

        reservationForm.setTicketTypeCounts(reservationManager.getTicketTypes().stream()
                .sorted(comparing(TicketType::getCostPerTicket).reversed())
                .map(t -> {
                    int count = typeCounts.getOrDefault(t.getCode(), 0);
                    double total = count * t.getCostPerTicket();

                    grandTotal.accumulateAndGet(total, Double::sum);

                    return new TicketTypeCount(t.getCode(), t.getDescription(), format(t.getCostPerTicket()), count, format(total));
                })
                .toList());

        reservationForm.setTotal(format(grandTotal.get()));
    }

}
