package org.olac.reservation.client;

import lombok.RequiredArgsConstructor;
import org.olac.reservation.client.form.TicketTypeForm;
import org.olac.reservation.resource.TicketDatastoreAccess;
import org.olac.reservation.resource.TicketType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

import static java.util.Comparator.comparing;

@Controller()
@RequiredArgsConstructor
public class AdminController {

    private final TicketDatastoreAccess ticketDatastoreAccess;

    @GetMapping("/admin/ticketTypes")
    public String getTicketTypes(Model model) {
        List<TicketType> ticketTypes = ticketDatastoreAccess.getTicketTypes().stream()
                .sorted(comparing(TicketType::getCostPerTicket).reversed())
                .toList();

        model.addAttribute("ticketTypes", ticketTypes);
        model.addAttribute("form", new TicketTypeForm());

        return "ticketTypes";
    }

    @PostMapping("/admin/ticketTypes")
    public String addTicketType(@ModelAttribute TicketTypeForm form, Model model) {
        TicketType newType = new TicketType(form.getDescription(), form.getCost());
        ticketDatastoreAccess.saveTicketType(newType);

        return getTicketTypes(model);
    }

}
