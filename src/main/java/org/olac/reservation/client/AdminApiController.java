package org.olac.reservation.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.olac.reservation.manager.AdministrationManager;
import org.olac.reservation.resource.model.Reservation;
import org.olac.reservation.resource.model.TicketType;
import org.olac.reservation.utility.SecurityUtility;
import org.olac.reservation.utility.model.Account;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static java.util.Comparator.comparing;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminApiController {

    private final AdministrationManager administrationManager;
    private final SecurityUtility securityUtility;

    @GetMapping("ticket-types")
    List<TicketType> getTicketTypes() {
        log.info("Retrieving ticket types for admin");
        return administrationManager.getTicketTypes().stream()
                .sorted(comparing(TicketType::getCostPerTicket).reversed())
                .toList();
    }

    @PostMapping("ticket-types")
    void saveTicketType(@RequestBody TicketType type) {
        administrationManager.saveTicketType(type);
    }

    @DeleteMapping("ticket-types")
    void deleteTicketType(@RequestParam String code) {
        administrationManager.deleteTicketType(code);
    }

    @GetMapping("reservations")
    List<Reservation> getReservations() {
        log.info("Retrieving reservations for admin");
        return administrationManager.getReservations().stream()
                .sorted(comparing(Reservation::getLastName)
                        .thenComparing(Reservation::getFirstName)
                        .thenComparing(Reservation::getId))
                .toList();
    }

    @GetMapping("accounts")
    List<Account> getAccounts() {
        return securityUtility.getAccounts().stream()
                .sorted(comparing(Account::getUsername))
                .toList();
    }

    @PostMapping("accounts")
    Account createAccount(@RequestBody NewAccountRequest request) {
        Account rVal = securityUtility.createAccount(request.getUsername(), request.getEmail(), request.isAdmin());
        if (rVal.isEnabled() != request.isEnabled()) {
            rVal.setEnabled(request.isEnabled());
            securityUtility.updateAccount(rVal);
        }
        return rVal;
    }


    @PutMapping("accounts/{username}")
    void updateAccount(@PathVariable String username, @RequestBody UpdateAccountRequest request) {
        if (!"admin".equals(username) && (request.getAdmin() != null || request.getEnabled() != null)) {
            securityUtility.findAccount(username).ifPresent(account -> {
                if (request.getAdmin() != null) {
                    account.setAdmin(request.getAdmin());
                }
                if (request.getEnabled() != null) {
                    account.setEnabled(request.getEnabled());
                }
                securityUtility.updateAccount(account);
            });
        }
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class NewAccountRequest {
        private String username;
        private String email;
        private boolean admin;
        private boolean enabled;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class UpdateAccountRequest {
        private String email;
        private Boolean admin;
        private Boolean enabled;
    }

}
