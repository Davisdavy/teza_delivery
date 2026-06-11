package com.wafula.teza.notification.application;

import com.wafula.teza.notification.domain.Notification;
import com.wafula.teza.notification.domain.NotificationRepository;
import com.wafula.teza.notification.domain.NotificationStatus;
import com.wafula.teza.shared.exception.ApiException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Orchestrator implementing NotificationService.
 */
@Service
public class NotificationServiceImpl implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);

    private final NotificationRepository notificationRepository;

    public NotificationServiceImpl(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notification> getNotificationsForUser(UUID userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    @Transactional
    public void markAsRead(UUID notificationId, UUID userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Notification not found"));

        if (!notification.getUserId().equals(userId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Access denied: you cannot access this notification");
        }

        notification.setStatus(NotificationStatus.READ);
        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void markAllAsRead(UUID userId) {
        List<Notification> unread = notificationRepository.findByUserIdAndStatus(userId, NotificationStatus.UNREAD);
        for (Notification notification : unread) {
            notification.setStatus(NotificationStatus.READ);
        }
        notificationRepository.saveAll(unread);
    }

    @Override
    @Transactional
    public Notification createNotification(UUID userId, String title, String message) {
        Notification notification = Notification.builder()
                .userId(userId)
                .title(title)
                .message(message)
                .status(NotificationStatus.UNREAD)
                .createdAt(Instant.now())
                .build();

        Notification saved = notificationRepository.save(notification);

        // Simulate sending outbound notification (SMS/Push/Email)
        log.info("[OUTBOUND NOTIFICATION] Sending PUSH/SMS to user {}: Title: \"{}\", Message: \"{}\"", 
                userId, title, message);

        return saved;
    }
}
