package com.wafula.teza.notification.api.dto;

import com.wafula.teza.notification.domain.NotificationStatus;
import java.time.Instant;
import java.util.UUID;

/**
 * REST response payload representing a user notification.
 */
public record NotificationResponse(
        UUID id,
        UUID userId,
        String title,
        String message,
        NotificationStatus status,
        Instant createdAt
) {
}
