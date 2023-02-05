package org.olac.reservation.engine;

public interface TemplateEngine {

    String createReservationNotificationMessage(long reservationId, double totalAmount);

}
