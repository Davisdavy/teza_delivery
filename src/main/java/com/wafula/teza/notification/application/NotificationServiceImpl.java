package com.wafula.teza.notification.application;

import com.wafula.teza.notification.domain.Notification;
import com.wafula.teza.notification.domain.NotificationRepository;
import com.wafula.teza.notification.domain.NotificationStatus;
import com.wafula.teza.notification.domain.NotificationToken;
import com.wafula.teza.notification.domain.NotificationTokenRepository;
import com.wafula.teza.notification.infrastructure.FirebasePushService;
import com.wafula.teza.shared.exception.ApiException;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
    private final NotificationTokenRepository notificationTokenRepository;
    private final FirebasePushService firebasePushService;

    public NotificationServiceImpl(
            NotificationRepository notificationRepository,
            NotificationTokenRepository notificationTokenRepository,
            FirebasePushService firebasePushService) {
        this.notificationRepository = notificationRepository;
        this.notificationTokenRepository = notificationTokenRepository;
        this.firebasePushService = firebasePushService;
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
        return createNotification(userId, title, message, null);
    }

    @Override
    @Transactional
    public Notification createNotification(UUID userId, String title, String message, Map<String, String> extraData) {
        Notification notification = Notification.builder()
                .userId(userId)
                .title(title)
                .message(message)
                .status(NotificationStatus.UNREAD)
                .createdAt(Instant.now())
                .build();

        Notification saved = notificationRepository.save(notification);

        // Simulate sending outbound notification (SMS/Push/Email)
        log.info("[OUTBOUND NOTIFICATION] Sending PUSH/SMS to user {}: Title: \"{}\", Message: \"{}\", Extra: {}", 
                userId, title, message, extraData);

        try {
            List<NotificationToken> activeTokens = notificationTokenRepository.findByUserIdAndActive(userId, true);
            if (!activeTokens.isEmpty()) {
                Map<String, String> data = new HashMap<>();
                if (extraData != null) {
                    data.putAll(extraData);
                }
                data.put("title", title);
                data.put("message", message);
                data.put("userId", userId.toString());
                if (!data.containsKey("type")) {
                    if (title != null && title.toLowerCase().contains("offer")) {
                        data.put("type", "OFFER");
                    } else {
                        data.put("type", "NOTIFICATION");
                    }
                }

                for (NotificationToken tokenEntity : activeTokens) {
                    firebasePushService.sendPushNotification(tokenEntity.getToken(), title, message, data);
                    tokenEntity.setLastSeenAt(Instant.now());
                    notificationTokenRepository.save(tokenEntity);
                }
            }
        } catch (Exception e) {
            log.error("Failed to send push notification to user {}: {}", userId, e.getMessage(), e);
        }

        return saved;
    }

    @Override
    @Transactional
    public void registerDeviceToken(UUID userId, String token, String deviceId, String deviceType, String appVersion) {
        Optional<NotificationToken> existingOpt = notificationTokenRepository.findByToken(token);
        Instant now = Instant.now();
        if (existingOpt.isPresent()) {
            NotificationToken existing = existingOpt.get();
            existing.setUserId(userId);
            existing.setDeviceId(deviceId);
            existing.setDeviceType(deviceType);
            existing.setAppVersion(appVersion);
            existing.setActive(true);
            existing.setLastSeenAt(now);
            existing.setUpdatedAt(now);
            notificationTokenRepository.save(existing);
        } else {
            NotificationToken newToken = NotificationToken.builder()
                    .userId(userId)
                    .token(token)
                    .deviceId(deviceId)
                    .deviceType(deviceType)
                    .appVersion(appVersion)
                    .active(true)
                    .createdAt(now)
                    .lastSeenAt(now)
                    .updatedAt(now)
                    .build();
            notificationTokenRepository.save(newToken);
        }
    }

    @Override
    @Transactional
    public void unregisterDeviceToken(UUID userId, String token) {
        notificationTokenRepository.findByToken(token).ifPresent(existing -> {
            if (existing.getUserId().equals(userId)) {
                existing.setActive(false);
                existing.setUpdatedAt(Instant.now());
                notificationTokenRepository.save(existing);
            }
        });
    }
}
