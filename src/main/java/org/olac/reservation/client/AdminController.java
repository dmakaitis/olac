package org.olac.reservation.client;

import lombok.RequiredArgsConstructor;
import org.olac.reservation.client.form.TicketTypeForm;
import org.olac.reservation.manager.ReservationManager;
import org.olac.reservation.resource.model.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

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
public class AdminController {

    private final ReservationManager reservationManager;

    @GetMapping("/admin/ticketTypes")
    public String getTicketTypes(Model model) {
        List<TicketType> ticketTypes = getSortedTicketTypes();

        model.addAttribute("ticketTypes", ticketTypes);
        model.addAttribute("form", new TicketTypeForm());

        return "ticketTypes";
    }

    private List<TicketType> getSortedTicketTypes() {
        List<TicketType> ticketTypes = reservationManager.getTicketTypes().stream()
                .sorted(comparing(TicketType::getCostPerTicket).reversed())
                .toList();
        return ticketTypes;
    }

    @PostMapping("/admin/ticketTypes")
    public String addTicketType(@ModelAttribute TicketTypeForm form, Model model) {
        TicketType newType = new TicketType(form.getDescription(), form.getCost());
        reservationManager.saveTicketType(newType);

        return getTicketTypes(model);
    }

    @GetMapping("/admin/reservations")
    public String getAllReservations(Model model) {
        List<TicketType> ticketTypes = getSortedTicketTypes();

        // Calculate headers:

        List<String> headers = new ArrayList<>();
        headers.add("ID");
        headers.add("Last Name");
        headers.add("First Name");
        headers.add("Email");
        headers.add("Phone");

        for (TicketType ticketType : ticketTypes) {
            headers.add(ticketType.getDescription());
        }

        headers.add("Total");
        headers.add("Paid");

        model.addAttribute("headers", headers);

        // Calculate all reservation rows:

        List<List<String>> reservations = reservationManager.getReservations().stream()
                .sorted(comparing(Reservation::getLastName)
                        .thenComparing(Reservation::getFirstName)
                        .thenComparing(Reservation::getId))
                .map(r -> {
                    List<String> values = new ArrayList<>(asList(
                            r.getId().toString(),
                            r.getLastName(),
                            r.getFirstName(),
                            r.getEmail(),
                            r.getPhone()
                    ));

                    Map<String, TicketCounts> typesMap = r.getTicketCounts().stream()
                            .collect(toMap(TicketCounts::getTicketTypeCode, Function.identity()));

                    double total = 0.0;

                    for (TicketType ticketType : ticketTypes) {
                        Optional<TicketCounts> counts = Optional.ofNullable(typesMap.get(ticketType.getCode()));
                        int amt = counts.map(TicketCounts::getCount).orElse(0);
                        total += amt * ticketType.getCostPerTicket();

                        values.add(Integer.toString(amt));
                    }

                    values.add(PublicController.format(total));
                    values.add(PublicController.format(r.getPayments().stream()
                            .filter(p -> p.getStatus() == PaymentStatus.SUCCESSFUL)
                            .mapToDouble(Payment::getAmount)
                            .sum()));

                    return values;
                })
                .toList();

        model.addAttribute("reservations", reservations);

        return "reservations";
    }

}
