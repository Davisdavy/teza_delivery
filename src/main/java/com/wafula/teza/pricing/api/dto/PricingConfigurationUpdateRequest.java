package com.wafula.teza.pricing.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record PricingConfigurationUpdateRequest(
        @NotNull(message = "Base fee is required")
        @DecimalMin(value = "0.0", message = "Base fee must be greater than or equal to 0")
        BigDecimal baseFee,

        @NotNull(message = "Price per kilometer is required")
        @DecimalMin(value = "0.0", message = "Price per kilometer must be greater than or equal to 0")
        BigDecimal pricePerKilometer,

        @NotNull(message = "Price per minute is required")
        @DecimalMin(value = "0.0", message = "Price per minute must be greater than or equal to 0")
        BigDecimal pricePerMinute,

        @NotNull(message = "Minimum delivery fee is required")
        @DecimalMin(value = "0.0", message = "Minimum delivery fee must be greater than or equal to 0")
        BigDecimal minimumDeliveryFee,

        @NotNull(message = "Maximum delivery fee is required")
        @DecimalMin(value = "0.0", message = "Maximum delivery fee must be greater than or equal to 0")
        BigDecimal maximumDeliveryFee,

        @NotNull(message = "Surge enabled is required")
        Boolean surgeEnabled,

        @NotNull(message = "Peak hour multiplier is required")
        @DecimalMin(value = "1.0", message = "Peak hour multiplier must be greater than or equal to 1.0")
        BigDecimal peakHourMultiplier,

        @NotNull(message = "Weekend multiplier is required")
        @DecimalMin(value = "1.0", message = "Weekend multiplier must be greater than or equal to 1.0")
        BigDecimal weekendMultiplier,

        @NotNull(message = "Night multiplier is required")
        @DecimalMin(value = "1.0", message = "Night multiplier must be greater than or equal to 1.0")
        BigDecimal nightMultiplier
) {}
