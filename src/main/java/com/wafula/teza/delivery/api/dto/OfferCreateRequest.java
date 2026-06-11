package com.wafula.teza.delivery.api.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record OfferCreateRequest(
        @NotNull(message = "Rider ID is required")
        UUID riderId,

        @NotNull(message = "Expiry duration (seconds) is required")
        Long durationSeconds
) {
}
