package com.wafula.teza.shared.event;

import com.wafula.teza.delivery.domain.DeliveryStatus;
import java.util.UUID;

/**
 * Domain event published when a delivery order changes status or is created.
 */
public record DeliveryStatusChangedEvent(
        UUID deliveryId,
        DeliveryStatus oldStatus,
        DeliveryStatus newStatus,
        UUID customerId,
        UUID merchantId,
        UUID riderId,
        String pickupAddress,
        String dropoffAddress
) {
}
