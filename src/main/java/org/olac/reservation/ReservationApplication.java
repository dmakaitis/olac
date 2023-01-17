package org.olac.reservation;

import lombok.RequiredArgsConstructor;
import org.olac.reservation.resource.TicketRA;
import org.olac.reservation.resource.TicketType;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@RequiredArgsConstructor
public class ReservationApplication implements ApplicationRunner {

    private final TicketRA ticketRA;

    public static void main(String[] args) {
        SpringApplication.run(ReservationApplication.class, args);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        ticketRA.saveTicketType(new TicketType("Adult", 50.0));
        ticketRA.saveTicketType(new TicketType("Child 2-10", 30.0));
    }

}
