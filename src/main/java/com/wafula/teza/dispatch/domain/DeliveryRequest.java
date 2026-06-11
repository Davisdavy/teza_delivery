package com.wafula.teza.dispatch.domain;

import java.util.UUID;

/**
 * Represents a delivery request needing a rider assignment, containing pickup coordinates.
 */
public record DeliveryRequest(
        UUID deliveryId,
        double pickupLatitude,
        double pickupLongitude
) {
}
