package org.olac.reservation.engine;

import org.olac.reservation.resource.model.Reservation;

public interface TemplateEngine {

    String createPaymentReceivedConfirmation(Reservation reservation);

    String createPaymentInstructions(Reservation reservation);

}
