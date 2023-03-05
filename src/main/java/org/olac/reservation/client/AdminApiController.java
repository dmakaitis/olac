package org.olac.reservation.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.olac.reservation.manager.AdministrationManager;
import org.olac.reservation.resource.model.TicketType;
import org.olac.reservation.utility.AuditUtility;
import org.olac.reservation.utility.SecurityUtility;
import org.olac.reservation.utility.model.Account;
import org.olac.reservation.utility.model.ReservationAuditEvent;
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
    private final AuditUtility auditUtility;

    //---------------------- Ticket Types ---------------------------

    @PostMapping("ticket-types")
    void saveTicketType(@RequestBody TicketType type) {
        log.debug("Saving ticket type: {}", type);
        administrationManager.saveTicketType(type);
    }

    @DeleteMapping("ticket-types")
    void deleteTicketType(@RequestParam String code) {
        log.debug("Deleting ticket type: {}", code);
        administrationManager.deleteTicketType(code);
    }

    //---------------------- Reservations ---------------------------

    @GetMapping("reservations/{reservationId}/audit")
    public List<ReservationAuditEvent> getReservationAudit(@PathVariable String reservationId) {
        log.debug("Retrieving audit events for reservation {}", reservationId);
        return auditUtility.getReservationEvents(reservationId);
    }

    @DeleteMapping("reservations/{reservationId}")
    void deleteReservation(@PathVariable String reservationId) {
        log.debug("Deleting reservation: {}", reservationId);
        administrationManager.deleteReservation(reservationId);
    }

    //----------------------------- Accounts ------------------------------

    @GetMapping("accounts")
    List<Account> getAccounts() {
        log.debug("Retrieving accounts");
        return securityUtility.getAccounts().stream()
                .sorted(comparing(Account::getUsername))
                .toList();
    }

    @PostMapping("accounts")
    Account createAccount(@RequestBody NewAccountRequest request) {
        log.debug("Creating new account: {}", request);
        Account rVal = securityUtility.createAccount(request.getUsername(), request.getEmail(), request.isAdmin());
        if (rVal.isEnabled() != request.isEnabled()) {
            rVal.setEnabled(request.isEnabled());
            securityUtility.updateAccount(rVal);
        }
        return rVal;
    }


    @PutMapping("accounts/{username}")
    void updateAccount(@PathVariable String username, @RequestBody UpdateAccountRequest request) {
        log.debug("Updating account: {} => {}", username, request);
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
