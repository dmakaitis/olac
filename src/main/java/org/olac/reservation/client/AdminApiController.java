package org.olac.reservation.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.olac.reservation.manager.AdministrationManager;
import org.olac.reservation.resource.model.Reservation;
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
        log.debug("Retrieving reservations");
        return administrationManager.getReservations().stream()
                .sorted(comparing(Reservation::getLastName)
                        .thenComparing(Reservation::getFirstName)
                        .thenComparing(Reservation::getId))
                .toList();
    }

    @GetMapping("reservations/{reservationId}/audit")
    public List<ReservationAuditEvent> getReservationAudit(@PathVariable String reservationId) {
        log.debug("Retrieving audit events for reservation {}", reservationId);
        return auditUtility.getReservationEvents(reservationId);
    }

    @PutMapping("reservations/{reservationId}")
    public void saveReservation(@PathVariable String reservationId, @RequestBody Reservation reservation) {
        log.debug("Updating reservation {}", reservationId);
        administrationManager.saveReservation(reservation);
    }

    @GetMapping("accounts")
    List<Account> getAccounts() {
        log.debug("Retrieving accounts");
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
