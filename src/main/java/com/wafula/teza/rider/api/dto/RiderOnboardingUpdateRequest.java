package com.wafula.teza.rider.api.dto;

import com.wafula.teza.rider.domain.OnboardingStatus;
import jakarta.validation.constraints.NotNull;

/** Request body for admin to update a rider's onboarding status. */
public record RiderOnboardingUpdateRequest(
        @NotNull OnboardingStatus onboardingStatus) {
}
