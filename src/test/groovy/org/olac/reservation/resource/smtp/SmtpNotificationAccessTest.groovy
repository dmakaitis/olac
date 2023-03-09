package org.olac.reservation.resource.smtp

import jakarta.mail.Message
import jakarta.mail.internet.InternetAddress
import jakarta.mail.internet.MimeMessage
import org.olac.reservation.config.OlacProperties
import org.springframework.mail.javamail.JavaMailSender
import spock.lang.Specification

class SmtpNotificationAccessTest extends Specification {

    def email = "omahalac@gmail.com"
    def properties = new OlacProperties(email: email)
    def javaMailSender = Mock(JavaMailSender)

    def service = new SmtpNotificationAccess(properties, javaMailSender)

    def "We should be able to send an email"() {
        given:
          def recipient = "nobody@nowhere.com"
          def subject = "Test message"
          def message = "Testing, 1, 2, 3..."

          def mimeMessage = Mock(MimeMessage)

          _ * javaMailSender.createMimeMessage() >> mimeMessage

        when:
          service.sendNotification(recipient, subject, message)

        then:
          1 * mimeMessage.setSubject(subject, "UTF-8")
          1 * mimeMessage.setFrom(new InternetAddress(email))
          1 * mimeMessage.setRecipient(Message.RecipientType.TO, new InternetAddress(recipient))
          1 * mimeMessage.setContent(_) // Not sure how to test this is valid...

        then:
          1 * javaMailSender.send(mimeMessage)
    }
}
