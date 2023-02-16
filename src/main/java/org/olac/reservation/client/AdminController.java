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
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static java.util.Arrays.asList;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Controller()
@RequiredArgsConstructor
public class AdminController {

    private final ReservationManager reservationManager;

    @GetMapping("/admin/ticketTypes")
    public String getTicketTypes(@RequestParam(name = "code", required = false) String ticketTypeCode, Model model) {
        List<TicketType> ticketTypes = getSortedTicketTypes();

        TicketTypeForm form = new TicketTypeForm();
        ticketTypes.stream()
                .filter(t -> t.getCode().equals(ticketTypeCode))
                .findFirst()
                .ifPresent(t -> {
                    form.setCost(t.getCostPerTicket());
                    form.setDescription(t.getDescription());
                    form.setCode(t.getCode());
                });

        model.addAttribute("ticketTypes", ticketTypes);
        model.addAttribute("form", form);

        return "admin/ticket-types";
    }

    private List<TicketType> getSortedTicketTypes() {
        return reservationManager.getTicketTypes().stream()
                .sorted(comparing(TicketType::getCostPerTicket).reversed())
                .toList();
    }

    @PostMapping("/admin/ticketTypes")
    public String addTicketType(@ModelAttribute TicketTypeForm form, Model model) {
        TicketType newType = isNotBlank(form.getCode()) ? new TicketType(form.getCode(), form.getDescription(), form.getCost()) :
                new TicketType(form.getDescription(), form.getCost());

        newType = reservationManager.saveTicketType(newType);

        return getTicketTypes(newType.getCode(), model);
    }

    @GetMapping("/admin/deleteTicketType")
    public String deleteTicketType(@RequestParam(name = "code") String code, @RequestParam(name = "confirm", required = false) String confirm, Model model) {
        if ("Y".equalsIgnoreCase(confirm)) {
            reservationManager.deleteTicketType(code);
            return "redirect:/admin/ticketTypes";
        }

        Optional<TicketType> type = reservationManager.getTicketTypes().stream()
                .filter(t -> code.equals(t.getCode()))
                .findFirst();

        if (type.isPresent()) {
            model.addAttribute("ticketType", type.get());
            return "admin/ticket-type-delete";
        } else {
            return "redirect:/admin/ticketTypes";
        }
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
        headers.add("Status");

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

                    values.add(r.getStatus().toString());

                    return values;
                })
                .toList();

        model.addAttribute("reservations", reservations);

        return "admin/reservations";
    }

}
