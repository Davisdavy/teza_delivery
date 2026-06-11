package com.wafula.teza.rider.api.dto;

import com.wafula.teza.rider.domain.OnboardingStatus;
import com.wafula.teza.rider.domain.VehicleType;
import java.time.Instant;
import java.util.UUID;

public record RiderProfileResponse(
        UUID id,
        UUID userId,
        VehicleType vehicleType,
        String vehiclePlateNum,
        boolean available,
        OnboardingStatus onboardingStatus,
        Instant createdAt,
        Instant updatedAt
) {
}
