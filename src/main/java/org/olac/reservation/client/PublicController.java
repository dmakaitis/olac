package org.olac.reservation.client;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.olac.reservation.client.form.ReservationForm;
import org.olac.reservation.client.form.TicketTypeCount;
import org.olac.reservation.config.OlacProperties;
import org.olac.reservation.manager.ReservationManager;
import org.olac.reservation.resource.model.Reservation;
import org.olac.reservation.resource.model.ReservationStatus;
import org.olac.reservation.resource.model.TicketCounts;
import org.olac.reservation.resource.model.TicketType;
import org.olac.reservation.resource.paypal.model.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.text.NumberFormat;
import java.util.*;
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
    private static final String ATTRIB_RESERVATION_ID = "reservationId";

    public static final String TEMPLATE_HOME = "home";
    public static final String TEMPLATE_TICKETS = "tickets";
    public static final String TEMPLATE_PAYMENT = "payment";
    public static final String TEMPLATE_CONFIRMATION = "confirmation";
    public static final String TEMPLATE_THANKS = "thanks";
    public static final String TEMPLATE_ERROR = "error";

    private final ReservationManager reservationManager;
    private final OlacProperties properties;

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

        model.addAttribute(ATTRIB_RESERVATION_ID, reservationId);
        model.addAttribute("amount", reservationForm.getTotal());

        List<PurchaseUnitRequest> purchaseUnits = toPurchaseUnits(reservation.getReservationId(), reservationForm);
        model.addAttribute("purchaseUnits", purchaseUnits);
        model.addAttribute("paypalClient", properties.getPaypal().getClient());

        session.removeAttribute(ATTRIB_RESERVATION_FORM);
        session.setAttribute(ATTRIB_RESERVATION_ID, reservation.getReservationId());

        return TEMPLATE_PAYMENT;
    }

    @PostMapping("/thanks")
    public String postPayment(@RequestParam CreateOrderResponse createOrderResponse, HttpSession session) {
        String transactionId = createOrderResponse.getId();
        String reservationId = (String) session.getAttribute(ATTRIB_RESERVATION_ID);

        boolean success = reservationManager.validateAndAddPayment(reservationId, transactionId);

        session.removeAttribute(ATTRIB_RESERVATION_ID);

        if (success) {
            log.info("Payment was confirmed!");
            return TEMPLATE_THANKS;
        } else {
            log.error("Failed to validate payment");
            return TEMPLATE_ERROR;
        }
    }

    @GetMapping("/payByCheck")
    public String payByCheck(Model model, HttpSession session) {
        String reservationId = (String) session.getAttribute(ATTRIB_RESERVATION_ID);

        reservationManager.sendPaymentReminder(reservationId);

        session.removeAttribute(ATTRIB_RESERVATION_ID);

        reservationManager.getReservation(reservationId).ifPresent(r -> {
            model.addAttribute("amount", r.getAmountDue());
            model.addAttribute(ATTRIB_RESERVATION_ID, r.getId());
        });

        return "thanks-check";
    }

    private List<PurchaseUnitRequest> toPurchaseUnits(String paypalId, ReservationForm reservationForm) {
        double totalAmount = reservationForm.getTotal();

        PurchaseUnitRequest purchaseUnit = PurchaseUnitRequest.builder()
                .customId(paypalId)
                .amount(AmountWithBreakdown.builder()
                        .currencyCode("USD")
                        .value(totalAmount)
                        .breakdown(AmountBreakdown.builder()
                                .itemTotal(Money.builder()
                                        .currencyCode("USD")
                                        .value(totalAmount)
                                        .build())
                                .build())
                        .build())
                .description("Omaha Lithuanian Community's 70th Anniversary Celebration on Saturday, April 22, 2023")
                .softDescriptor("70th Anniversary")
                .items(reservationForm.getTicketTypeCounts().stream()
                        .filter(c -> c.getCount() > 0)
                        .map(this::toItem)
                        .toList())
                .build();

        return singletonList(purchaseUnit);
    }

    private Item toItem(TicketTypeCount ticketTypeCount) {
        return Item.builder()
                .name(ticketTypeCount.getDescription())
                .unitAmount(Money.builder()
                        .currencyCode("USD")
                        .value(ticketTypeCount.getCostPerTicket())
                        .build())
                .quantity(ticketTypeCount.getCount().toString())
                .build();
    }

    public static String format(double value) {
        return NumberFormat.getCurrencyInstance().format(value);
    }

    private Reservation toReservation(ReservationForm form) {
        Reservation reservation = new Reservation();
        reservation.setReservationId(UUID.randomUUID().toString());
        reservation.setReservationTimestamp(new Date());

        reservation.setFirstName(form.getFirstName());
        reservation.setLastName(form.getLastName());
        reservation.setEmail(form.getEmail());
        reservation.setPhone(form.getPhone());
        reservation.setStatus(ReservationStatus.PENDING_PAYMENT);

        reservation.setTicketCounts(form.getTicketTypeCounts().stream()
                .map(t -> new TicketCounts(t.getTypeCode(), t.getCount()))
                .toList());

        Map<String, Double> typeCosts = reservationManager.getTicketTypes().stream()
                .collect(toMap(TicketType::getCode, TicketType::getCostPerTicket));

        reservation.setAmountDue(form.getTicketTypeCounts().stream()
                .mapToDouble(t -> t.getCount() * typeCosts.getOrDefault(t.getTypeCode(), 0.0))
                .sum());

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

                    return new TicketTypeCount(t.getCode(), t.getDescription(), t.getCostPerTicket(), count, total);
                })
                .toList());

        reservationForm.setTotal(grandTotal.get());
    }

}
