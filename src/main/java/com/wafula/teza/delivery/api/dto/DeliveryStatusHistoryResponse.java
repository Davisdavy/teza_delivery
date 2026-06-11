package com.wafula.teza.delivery.api.dto;

import com.wafula.teza.delivery.domain.DeliveryStatus;
import java.time.Instant;
import java.util.UUID;

public record DeliveryStatusHistoryResponse(
        UUID id,
        UUID deliveryId,
        DeliveryStatus status,
        UUID changedByUserId,
        String reason,
        Instant createdAt
) {
}
