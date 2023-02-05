package org.olac.reservation.engine.impl;

import lombok.RequiredArgsConstructor;
import org.olac.reservation.client.PublicController;
import org.springframework.stereotype.Service;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
public class ThymeleafTemplateEngine implements org.olac.reservation.engine.TemplateEngine {

    private final ITemplateEngine templateEngine;

    @Override
    public String createReservationNotificationMessage(long reservationId, double totalAmount) {
        Context ctx = new Context();
        ctx.setVariable("reservationId", reservationId);
        ctx.setVariable("amount", PublicController.format(totalAmount));

        return templateEngine.process("payment-email.html", ctx);
    }

}
