package com.wafula.teza.rider.api.dto;

import java.time.Instant;
import java.util.UUID;

public record RiderLocationResponse(
        UUID riderProfileId,
        double latitude,
        double longitude,
        Instant updatedAt
) {
}
