package com.wafula.teza.delivery.api.dto;

import com.wafula.teza.delivery.domain.DeliveryStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DeliveryStatusUpdateRequest(
        @NotNull(message = "Status is required")
        DeliveryStatus status,

        @Size(max = 255, message = "Reason cannot exceed 255 characters")
        String reason,

        String otp
) {
}
