package com.wafula.teza.notification.application;

import com.wafula.teza.notification.domain.Notification;
import java.util.List;
import java.util.UUID;

/**
 * Service port orchestrating notification use cases.
 */
public interface NotificationService {

    List<Notification> getNotificationsForUser(UUID userId);

    void markAsRead(UUID notificationId, UUID userId);

    void markAllAsRead(UUID userId);

    Notification createNotification(UUID userId, String title, String message);
}
