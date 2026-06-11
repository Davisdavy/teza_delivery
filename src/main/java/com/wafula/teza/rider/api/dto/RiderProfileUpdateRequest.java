package com.wafula.teza.rider.api.dto;

import com.wafula.teza.rider.domain.OnboardingStatus;
import com.wafula.teza.rider.domain.VehicleType;
import jakarta.validation.constraints.Size;

public record RiderProfileUpdateRequest(
        VehicleType vehicleType,

        @Size(max = 32, message = "Vehicle plate number cannot exceed 32 characters")
        String vehiclePlateNum,

        OnboardingStatus onboardingStatus,

        Boolean available
) {
}
