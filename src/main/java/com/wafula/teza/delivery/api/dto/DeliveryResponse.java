package com.wafula.teza.delivery.api.dto;

import com.wafula.teza.delivery.domain.DeliveryStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record DeliveryResponse(
        UUID id,
        UUID merchantId,
        UUID customerId,
        UUID riderId,
        String pickupAddress,
        double pickupLatitude,
        double pickupLongitude,
        String dropoffAddress,
        double dropoffLatitude,
        double dropoffLongitude,
        DeliveryStatus status,
        BigDecimal deliveryFee,
        Instant acceptedAt,
        Instant pickedUpAt,
        Instant deliveredAt,
        Instant cancelledAt,
        Instant createdAt,
        Instant updatedAt
) {
}
