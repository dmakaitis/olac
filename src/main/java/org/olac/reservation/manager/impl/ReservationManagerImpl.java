package org.olac.reservation.manager.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.olac.reservation.config.OlacProperties;
import org.olac.reservation.engine.PaymentEngine;
import org.olac.reservation.manager.AdministrationManager;
import org.olac.reservation.manager.ReservationManager;
import org.olac.reservation.resource.ReservationDatastoreAccess;
import org.olac.reservation.resource.TicketDatastoreAccess;
import org.olac.reservation.resource.model.*;
import org.olac.reservation.utility.SecurityUtility;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationManagerImpl implements ReservationManager, AdministrationManager {

    private final PaymentEngine paymentEngine;
    private final TicketDatastoreAccess ticketDatastoreAccess;
    private final ReservationDatastoreAccess reservationDatastoreAccess;
    private final SecurityUtility securityUtility;
    private final OlacProperties properties;

    @Override
    public TicketType saveTicketType(TicketType ticketType) {
        return ticketDatastoreAccess.saveTicketType(ticketType);
    }

    @Override
    public List<TicketType> getTicketTypes() {
        return ticketDatastoreAccess.getTicketTypes();
    }

    @Override
    public Page<Reservation> getReservations(String filter, PageRequest pageRequest) {
        Page<Reservation> page = reservationDatastoreAccess.getReservations(filter, pageRequest);
        page.setExt(reservationDatastoreAccess.getReservationsStats());
        return page;
    }

    @Override
    public boolean areTicketsAvailable(long requestedTicketCount) {
        long availableTickets = properties.getMaxTickets() - reservationDatastoreAccess.getReservationsStats().getTicketsReserved();
        return requestedTicketCount <= availableTickets;
    }

    @Override
    public boolean validateAndAddPayment(String reservationId, String paymentProcessorTransactionId) {
        Optional<Reservation> optional = reservationDatastoreAccess.getReservation(reservationId);
        if (optional.isEmpty()) {
            return false;
        }
        Reservation reservation = optional.get();

        if (paymentEngine.validateAndAddOnlinePayment(reservation, paymentProcessorTransactionId)) {
            saveReservation(reservation, true);
            return true;
        }

        return false;
    }

    @Override
    public void deleteTicketType(String ticketTypeCode) {
        ticketDatastoreAccess.deleteTicketType(ticketTypeCode);
    }

    @Override
    public void deleteReservation(String reservationId) {
        reservationDatastoreAccess.deleteReservation(reservationId);
    }

    @Override
    public Reservation saveReservation(Reservation reservation) {
        if (securityUtility.isCurrentUserAdmin()) {
            return saveReservation(reservation, true);
        }

        // If the user is not an admin, they can only save the full reservation if it doesn't already exist
        Optional<Reservation> oldReservationOptional = reservationDatastoreAccess.getReservation(reservation.getReservationId());
        if (oldReservationOptional.isEmpty()) {
            return saveReservation(reservation, true);
        }

        // Non administrators can only add new payments...
        Reservation oldReservation = oldReservationOptional.get();
        List<Payment> payments = new ArrayList<>(oldReservation.getPayments());
        int startPaymentsSize = payments.size();
        reservation.getPayments().stream()
                .filter(p -> p.getId() == null)
                .forEach(payments::add);

        if (startPaymentsSize != payments.size()) {
            oldReservation.setPayments(payments);
            return saveReservation(oldReservation, true);
        }

        return oldReservation;
    }

    @Override
    public Reservation saveReservation(Reservation reservation, boolean sendNotification) {
        paymentEngine.normalizePaymentsAndStatus(reservation);

        // We need to determine which notification to send before we save the reservation, but we can't actually send the
        // notification until after the reservation has been saved...
        Consumer<Reservation> sendReservationFunction = sendNotification ? paymentEngine.getPaymentNotificationFunction(reservation) : r -> {
        };

        // Save the reservations, generating the reservation number if it doesn't already exist
        reservation = reservationDatastoreAccess.saveReservation(reservation);

        // Send whatever notification we determined was needed now that we have a reservation number
        sendReservationFunction.accept(reservation);

        return reservation;
    }

}
