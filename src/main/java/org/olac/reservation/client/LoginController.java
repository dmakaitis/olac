package org.olac.reservation.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@Slf4j
public class LoginController {

    @GetMapping("/login")
    public String initLogin(Model model) {
        model.addAttribute("showMenu", false);
        return "login";
    }

}
