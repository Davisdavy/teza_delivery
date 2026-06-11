package com.wafula.teza.rider.application;

import com.wafula.teza.rider.api.dto.RiderLocationResponse;
import com.wafula.teza.rider.api.dto.RiderProfileResponse;
import com.wafula.teza.rider.domain.RiderLocation;
import com.wafula.teza.rider.domain.RiderProfile;

public final class RiderMapper {

    private RiderMapper() {
    }

    public static RiderProfileResponse toProfileResponse(RiderProfile profile) {
        if (profile == null) {
            return null;
        }
        return new RiderProfileResponse(
                profile.getId(),
                profile.getUserId(),
                profile.getVehicleType(),
                profile.getVehiclePlateNum(),
                profile.isAvailable(),
                profile.getOnboardingStatus(),
                profile.getCreatedAt(),
                profile.getUpdatedAt()
        );
    }

    public static RiderLocationResponse toLocationResponse(RiderLocation location) {
        if (location == null) {
            return null;
        }
        return new RiderLocationResponse(
                location.getId(),
                location.getLatitude(),
                location.getLongitude(),
                location.getUpdatedAt()
        );
    }
}
