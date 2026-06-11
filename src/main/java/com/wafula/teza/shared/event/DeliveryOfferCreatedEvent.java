package com.wafula.teza.shared.event;

import java.util.UUID;

/**
 * Domain event published when a new delivery offer is dispatched to a rider.
 */
public record DeliveryOfferCreatedEvent(
        UUID offerId,
        UUID deliveryId,
        UUID riderProfileId,
        UUID riderUserId,
        long durationSeconds
) {
}
