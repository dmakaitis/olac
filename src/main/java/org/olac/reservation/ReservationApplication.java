package org.olac.reservation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.olac.reservation.config.OlacProperties;
import org.olac.reservation.resource.ReservationDatastoreAccess;
import org.olac.reservation.resource.TicketDatastoreAccess;
import org.olac.reservation.resource.model.Payment;
import org.olac.reservation.resource.model.Reservation;
import org.olac.reservation.resource.model.TicketType;
import org.olac.reservation.utility.SecurityUtility;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

import static java.util.stream.Collectors.toMap;

@SpringBootApplication
@RequiredArgsConstructor
@EnableAsync
@EnableConfigurationProperties
@ConfigurationPropertiesScan
@Slf4j
public class ReservationApplication implements ApplicationRunner {

    private final OlacProperties properties;
    private final TicketDatastoreAccess ticketDatastoreAccess;
    private final ReservationDatastoreAccess reservationDatastoreAccess;
    private final SecurityUtility securityUtility;

    public static void main(String[] args) {
        SpringApplication.run(ReservationApplication.class, args);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // Make sure we at least have an admin account...
        if (securityUtility.getAccounts().isEmpty()) {
            log.warn("Creating default admin account.");
            securityUtility.createAccount("admin", "dmakaitis@gmail.com", true);
        }

        // Verify that we have the admin account created...
        if (!securityUtility.getAccounts().isEmpty()) {
            log.debug("Verified that we now have at least one account");
        }

        if (properties.getTestData().isLoad()) {
            for (TicketType ticketType : properties.getTestData().getTicketTypes()) {
                ticketDatastoreAccess.saveTicketType(ticketType);
            }

            Map<String, Double> typeCostMap = properties.getTestData().getTicketTypes().stream()
                    .collect(toMap(TicketType::getCode, TicketType::getCostPerTicket));

            for (Reservation reservation : properties.getTestData().getReservations()) {
                reservation.setReservationId(UUID.randomUUID().toString());
                reservation.setReservationTimestamp(new Date());
                reservation.setAmountDue(reservation.getTicketCounts().stream()
                        .mapToDouble(t -> t.getCount() * typeCostMap.getOrDefault(t.getTicketTypeCode(), 0.0))
                        .sum());
                for (Payment payment : reservation.getPayments()) {
                    payment.setCreatedTimestamp(new Date());
                }

                reservationDatastoreAccess.saveReservation(reservation);
            }
        }
    }

}
