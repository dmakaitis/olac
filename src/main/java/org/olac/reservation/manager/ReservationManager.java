package org.olac.reservation.manager;

import org.olac.reservation.resource.model.Payment;
import org.olac.reservation.resource.model.Reservation;
import org.olac.reservation.resource.model.TicketType;

import java.util.List;
import java.util.Optional;

public interface ReservationManager {

    List<TicketType> getTicketTypes();

    TicketType saveTicketType(TicketType ticketType);

    long createReservation(Reservation reservation);

    List<Reservation> getReservations();

    Optional<Reservation> getReservation(String reservationId);

    boolean areTicketsAvailable(long requestedTicketCount);

    boolean validateAndAddPayment(String reservationId, String paymentProcessorTransactionId);

    void addPayment(String reservationId, Payment payment);

    void sendPaymentReminder(String reservationId);

    void deleteTicketType(String ticketTypeCode);

}
