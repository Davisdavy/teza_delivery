package com.wafula.teza.rider.application;

import com.wafula.teza.rider.api.dto.RiderLocationResponse;
import com.wafula.teza.rider.api.dto.RiderLocationUpdateRequest;
import com.wafula.teza.rider.api.dto.RiderProfileCreateRequest;
import com.wafula.teza.rider.api.dto.RiderProfileResponse;
import com.wafula.teza.rider.api.dto.RiderProfileUpdateRequest;
import com.wafula.teza.rider.domain.OnboardingStatus;
import java.util.List;
import java.util.UUID;

public interface RiderService {

    RiderProfileResponse createProfile(UUID userId, RiderProfileCreateRequest request);

    RiderProfileResponse getProfileByUserId(UUID userId);

    RiderProfileResponse getProfileById(UUID profileId, UUID currentUserId, boolean isAdmin);

    RiderProfileResponse updateProfile(UUID userId, RiderProfileUpdateRequest request);

    RiderProfileResponse updateOnboardingStatus(UUID profileId, OnboardingStatus onboardingStatus);

    void deleteProfile(UUID profileId, UUID currentUserId, boolean isAdmin);

    RiderLocationResponse updateLocation(UUID userId, RiderLocationUpdateRequest request);

    RiderLocationResponse getLocation(UUID userId);

    List<RiderProfileResponse> findAvailableRiders();

    RiderLocationResponse getLocationByProfileId(UUID profileId);
}
