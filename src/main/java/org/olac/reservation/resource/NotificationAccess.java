package org.olac.reservation.resource;

public interface NotificationAccess {

    void sendNotification(String recipient, String subject, String htmlMessage);

}
