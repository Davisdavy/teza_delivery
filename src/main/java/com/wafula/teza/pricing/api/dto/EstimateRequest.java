package com.wafula.teza.pricing.api.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record EstimateRequest(
        @NotNull(message = "Pickup latitude is required")
        @DecimalMin(value = "-90.0", message = "Pickup latitude must be between -90 and 90")
        @DecimalMax(value = "90.0", message = "Pickup latitude must be between -90 and 90")
        Double pickupLatitude,

        @NotNull(message = "Pickup longitude is required")
        @DecimalMin(value = "-180.0", message = "Pickup longitude must be between -180 and 180")
        @DecimalMax(value = "180.0", message = "Pickup longitude must be between -180 and 180")
        Double pickupLongitude,

        @NotNull(message = "Dropoff latitude is required")
        @DecimalMin(value = "-90.0", message = "Dropoff latitude must be between -90 and 90")
        @DecimalMax(value = "90.0", message = "Dropoff latitude must be between -90 and 90")
        Double dropoffLatitude,

        @NotNull(message = "Dropoff longitude is required")
        @DecimalMin(value = "-180.0", message = "Dropoff longitude must be between -180 and 180")
        @DecimalMax(value = "180.0", message = "Dropoff longitude must be between -180 and 180")
        Double dropoffLongitude
) {}
