package org.olac.reservation.resource.smtp;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.olac.reservation.config.OlacProperties;
import org.olac.reservation.resource.NotificationAccess;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmtpNotificationAccess implements NotificationAccess {

    private final OlacProperties properties;
    private final JavaMailSender javaMailSender;

    @Override
    @Async
    public void sentNotification(String recipient, String subject, String htmlMessage) {
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper message = new MimeMessageHelper(mimeMessage, true, StandardCharsets.UTF_8.name());
            message.setSubject(subject);
            message.setFrom(properties.getEmail());
            message.setTo(recipient);

            message.setText(htmlMessage, true);

            javaMailSender.send(mimeMessage);
        } catch (Exception e) {
            log.error("Failed to send email notification", e);
        }
    }
}
