package org.olac.reservation.utility;

import org.olac.reservation.utility.model.ReservationAuditEvent;

import java.util.List;

public interface AuditUtility {

    void logReservationEvent(String reservationId, String description);

    List<ReservationAuditEvent> getReservationEvents(String reservationId);

}
