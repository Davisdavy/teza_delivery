package com.wafula.teza.rider.api.dto;

import com.wafula.teza.rider.domain.VehicleType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RiderProfileCreateRequest(
        @NotNull(message = "Vehicle type is required")
        VehicleType vehicleType,

        @Size(max = 32, message = "Vehicle plate number cannot exceed 32 characters")
        String vehiclePlateNum
) {
}
