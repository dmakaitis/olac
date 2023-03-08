package org.olac.reservation.manager;

import org.olac.reservation.resource.model.Reservation;
import org.olac.reservation.resource.model.TicketType;

import java.util.List;

public interface ReservationManager {

    List<TicketType> getTicketTypes();

    boolean areTicketsAvailable(long requestedTicketCount);

    boolean validateAndAddPayment(String reservationId, String paymentProcessorTransactionId);

    Reservation saveReservation(Reservation reservation, boolean sendNotification);

}
