package org.olac.reservation.client;

import org.olac.reservation.resource.TicketRA;
import org.olac.reservation.resource.TicketType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class HomeController {

    private final TicketRA ticketRA;

    @Autowired
    public HomeController(TicketRA ticketRA) {
        this.ticketRA = ticketRA;
    }

    @GetMapping("/")
    public String home(Model model) {
        List<TicketType> ticketTypes = ticketRA.getTicketTypes();
        model.addAttribute("name", ticketTypes.toString());
        return "home";
    }

    @GetMapping("/save")
    public String save(@RequestParam String type, @RequestParam double cost, Model model) {
        TicketType ticketType = new TicketType(type, cost);
        ticketRA.saveTicketType(ticketType);
        return home(model);
    }

}
