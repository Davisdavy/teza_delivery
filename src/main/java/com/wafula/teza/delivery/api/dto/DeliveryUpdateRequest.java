package com.wafula.teza.delivery.api.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record DeliveryUpdateRequest(
        @Size(max = 255, message = "Pickup address cannot exceed 255 characters")
        String pickupAddress,

        @DecimalMin(value = "-90.0", message = "Pickup latitude must be between -90 and 90")
        @DecimalMax(value = "90.0", message = "Pickup latitude must be between -90 and 90")
        Double pickupLatitude,

        @DecimalMin(value = "-180.0", message = "Pickup longitude must be between -180 and 180")
        @DecimalMax(value = "180.0", message = "Pickup longitude must be between -180 and 180")
        Double pickupLongitude,

        @Size(max = 255, message = "Dropoff address cannot exceed 255 characters")
        String dropoffAddress,

        @DecimalMin(value = "-90.0", message = "Dropoff latitude must be between -90 and 90")
        @DecimalMax(value = "90.0", message = "Dropoff latitude must be between -90 and 90")
        Double dropoffLatitude,

        @DecimalMin(value = "-180.0", message = "Dropoff longitude must be between -180 and 180")
        @DecimalMax(value = "180.0", message = "Dropoff longitude must be between -180 and 180")
        Double dropoffLongitude,

        @PositiveOrZero(message = "Delivery fee cannot be negative")
        BigDecimal deliveryFee
) {
}
