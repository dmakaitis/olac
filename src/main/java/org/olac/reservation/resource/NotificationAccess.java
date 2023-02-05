package org.olac.reservation.resource;

public interface NotificationAccess {

    void sentNotification(String recipient, String subject, String htmlMessage);

}
