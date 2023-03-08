package org.olac.reservation.manager;

import org.olac.reservation.resource.model.*;

import java.util.List;
import java.util.Optional;

public interface ReservationManager {

    List<TicketType> getTicketTypes();

    Page<Reservation> getReservations(String filter, PageRequest pageRequest);

    Optional<Reservation> getReservation(String reservationId);

    boolean areTicketsAvailable(long requestedTicketCount);

    boolean validateAndAddPayment(String reservationId, String paymentProcessorTransactionId);

    void addPayment(String reservationId, Payment payment);

    Reservation saveReservation(Reservation reservation, boolean sendNotification);

}
