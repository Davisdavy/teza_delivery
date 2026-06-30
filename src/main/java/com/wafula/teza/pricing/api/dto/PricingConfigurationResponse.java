package com.wafula.teza.pricing.api.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PricingConfigurationResponse(
        UUID id,
        BigDecimal baseFee,
        BigDecimal pricePerKilometer,
        BigDecimal pricePerMinute,
        BigDecimal minimumDeliveryFee,
        BigDecimal maximumDeliveryFee,
        boolean surgeEnabled,
        BigDecimal peakHourMultiplier,
        BigDecimal weekendMultiplier,
        BigDecimal nightMultiplier,
        UUID updatedBy,
        Instant updatedAt
) {}
