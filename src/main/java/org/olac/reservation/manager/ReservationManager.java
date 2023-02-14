package org.olac.reservation.manager;

import org.olac.reservation.resource.model.Payment;
import org.olac.reservation.resource.model.Reservation;
import org.olac.reservation.resource.model.TicketType;

import java.util.List;

public interface ReservationManager {

    List<TicketType> getTicketTypes();

    void saveTicketType(TicketType ticketType);

    long createReservation(Reservation reservation);

    List<Reservation> getReservations();

    boolean areTicketsAvailable(long requestedTicketCount);

    boolean validateAndAddPayment(String reservationId, String paymentProcessorTransactionId);

    void addPayment(String reservationId, Payment payment);

}
