package org.olac.reservation;

import lombok.RequiredArgsConstructor;
import org.olac.reservation.resource.TicketDatastoreAccess;
import org.olac.reservation.resource.TicketType;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@RequiredArgsConstructor
@EnableAsync
@EnableConfigurationProperties
@ConfigurationPropertiesScan
public class ReservationApplication implements ApplicationRunner {

    private final TicketDatastoreAccess ticketDatastoreAccess;

    public static void main(String[] args) {
        SpringApplication.run(ReservationApplication.class, args);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (ticketDatastoreAccess.getTicketTypes().isEmpty()) {
            ticketDatastoreAccess.saveTicketType(new TicketType("Adult", 50.0));
            ticketDatastoreAccess.saveTicketType(new TicketType("Child 2-12", 30.0));
        }
    }

}
