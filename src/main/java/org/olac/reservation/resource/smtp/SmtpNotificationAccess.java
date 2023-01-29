package org.olac.reservation.resource.smtp;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.olac.reservation.resource.NotificationAccess;
import org.olac.reservation.resource.Reservation;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmtpNotificationAccess implements NotificationAccess {

    private final JavaMailSender javaMailSender;
    private final TemplateEngine templateEngine;

    @Override
    public void sendReservationConfirmation(long reservationId, Reservation reservation) {
        try {
            Context ctx = new Context();
            ctx.setVariable("reservationId", reservationId);
            ctx.setVariable("amount", "$???.??");

            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper message = new MimeMessageHelper(mimeMessage, true, StandardCharsets.UTF_8.name());
            message.setSubject("Reservation Confirmation");
            message.setFrom("dmakaitis@gmail.com");
            message.setTo(reservation.getEmail());

            String htmlContent = templateEngine.process("payment-email.html", ctx);
            message.setText(htmlContent, true);

            javaMailSender.send(mimeMessage);
        } catch (Exception e) {
            log.error("Failed to send email notification", e);
        }
    }

}
