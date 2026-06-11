package com.wafula.teza.delivery.api.dto;

import com.wafula.teza.delivery.domain.OfferStatus;
import java.time.Instant;
import java.util.UUID;

public record DeliveryOfferResponse(
        UUID id,
        UUID deliveryId,
        UUID riderId,
        OfferStatus status,
        Instant expiresAt,
        Instant createdAt,
        Instant updatedAt
) {
}
