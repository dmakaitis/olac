package org.olac.reservation.manager.impl;

import lombok.RequiredArgsConstructor;
import org.olac.reservation.engine.TemplateEngine;
import org.olac.reservation.manager.ReservationManager;
import org.olac.reservation.resource.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

@Service
@RequiredArgsConstructor
public class ReservationManagerImpl implements ReservationManager {

    private final TicketDatastoreAccess ticketDatastoreAccess;
    private final ReservationDatastoreAccess reservationDatastoreAccess;
    private final TemplateEngine templateEngine;
    private final NotificationAccess notificationAccess;

    @Override
    public void saveTicketType(TicketType ticketType) {
        ticketDatastoreAccess.saveTicketType(ticketType);
    }

    @Override
    public List<TicketType> getTicketTypes() {
        return ticketDatastoreAccess.getTicketTypes();
    }

    @Override
    public long createReservation(Reservation reservation) {
        long reservationId = reservationDatastoreAccess.createReservation(reservation);
        double amount = calculateReservationAmount(reservation);
        String message = templateEngine.createReservationNotificationMessage(reservationId, amount);
        notificationAccess.sentNotification(reservation.getEmail(), "Reservation Confirmation", message);
        return reservationId;
    }

    @Override
    public List<Reservation> getReservations() {
        return reservationDatastoreAccess.getReservations();
    }

    private double calculateReservationAmount(Reservation reservation) {
        Map<String, Double> typeCosts = getTicketTypes().stream()
                .collect(toMap(TicketType::getCode, TicketType::getCostPerTicket));

        return reservation.getTicketCounts().stream()
                .mapToDouble(c -> c.getCount() * typeCosts.getOrDefault(c.getTicketTypeCode(), 0.0))
                .sum();
    }

}
