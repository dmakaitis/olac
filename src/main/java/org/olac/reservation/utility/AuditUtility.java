package org.olac.reservation.utility;

import org.olac.reservation.utility.model.ReservationEvent;

import java.util.List;

public interface AuditUtility {

    void logReservationEvent(String reservationId, String description);

    List<ReservationEvent> getReservationEvents(String reservationId);

}
