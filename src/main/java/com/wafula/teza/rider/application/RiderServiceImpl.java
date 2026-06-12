package com.wafula.teza.rider.application;

import com.wafula.teza.rider.api.dto.RiderLocationResponse;
import com.wafula.teza.rider.api.dto.RiderLocationUpdateRequest;
import com.wafula.teza.rider.api.dto.RiderProfileCreateRequest;
import com.wafula.teza.rider.api.dto.RiderProfileResponse;
import com.wafula.teza.rider.api.dto.RiderProfileUpdateRequest;
import com.wafula.teza.rider.domain.OnboardingStatus;
import com.wafula.teza.rider.domain.RiderLocation;
import com.wafula.teza.rider.domain.RiderLocationRepository;
import com.wafula.teza.rider.domain.RiderProfile;
import com.wafula.teza.rider.domain.RiderProfileRepository;
import com.wafula.teza.shared.domain.Role;
import com.wafula.teza.shared.exception.ApiException;
import com.wafula.teza.user.application.UserAccount;
import com.wafula.teza.user.application.UserAccountService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RiderServiceImpl implements RiderService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RiderServiceImpl.class);

    private final RiderProfileRepository riderProfileRepository;
    private final RiderLocationRepository riderLocationRepository;
    private final UserAccountService userAccountService;

    public RiderServiceImpl(
            RiderProfileRepository riderProfileRepository,
            RiderLocationRepository riderLocationRepository,
            UserAccountService userAccountService) {
        this.riderProfileRepository = riderProfileRepository;
        this.riderLocationRepository = riderLocationRepository;
        this.userAccountService = userAccountService;
    }

    @Override
    @Transactional
    public RiderProfileResponse createProfile(UUID userId, RiderProfileCreateRequest request) {
        if (riderProfileRepository.existsByUserId(userId)) {
            throw new ApiException(HttpStatus.CONFLICT, "Rider profile already exists for this user");
        }

        // Verify user exists and has RIDER role
        UserAccount user = userAccountService.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User account not found"));
        if (user.role() != Role.RIDER) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Only users with RIDER role can create a rider profile");
        }

        RiderProfile profile = RiderProfile.builder()
                .userId(userId)
                .vehicleType(request.vehicleType())
                .vehiclePlateNum(request.vehiclePlateNum())
                .onboardingStatus(OnboardingStatus.PENDING)
                .available(false)
                .build();

        RiderProfile saved = riderProfileRepository.save(profile);
        return RiderMapper.toProfileResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public RiderProfileResponse getProfileByUserId(UUID userId) {
        RiderProfile profile = riderProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Rider profile not found"));
        return RiderMapper.toProfileResponse(profile);
    }

    @Override
    @Transactional(readOnly = true)
    public RiderProfileResponse getProfileById(UUID profileId, UUID currentUserId, boolean isAdmin) {
        RiderProfile profile = riderProfileRepository.findById(profileId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Rider profile not found"));

        if (!isAdmin && !profile.getUserId().equals(currentUserId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Access denied: you do not own this profile");
        }

        return RiderMapper.toProfileResponse(profile);
    }

    @Override
    @Transactional
    public RiderProfileResponse updateProfile(UUID userId, RiderProfileUpdateRequest request) {
        RiderProfile profile = riderProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Rider profile not found"));

        if (request.onboardingStatus() != null && request.onboardingStatus() != profile.getOnboardingStatus()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Only admins can change onboarding status");
        }

        if (request.vehicleType() != null) {
            profile.setVehicleType(request.vehicleType());
        }
        if (request.vehiclePlateNum() != null) {
            profile.setVehiclePlateNum(request.vehiclePlateNum());
        }
        if (request.available() != null) {
            profile.setAvailable(request.available());
        }

        RiderProfile updated = riderProfileRepository.save(profile);
        return RiderMapper.toProfileResponse(updated);
    }

    @Override
    @Transactional
    public RiderProfileResponse updateOnboardingStatus(UUID profileId, OnboardingStatus onboardingStatus, UUID updaterId) {
        RiderProfile profile = riderProfileRepository.findById(profileId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Rider profile not found"));
        log.info("Rider profile {} onboarding status updated to {} by administrator/user {}", profileId, onboardingStatus, updaterId);
        profile.setOnboardingStatus(onboardingStatus);
        RiderProfile updated = riderProfileRepository.save(profile);
        return RiderMapper.toProfileResponse(updated);
    }

    @Override
    @Transactional
    public void deleteProfile(UUID profileId, UUID currentUserId, boolean isAdmin) {
        RiderProfile profile = riderProfileRepository.findById(profileId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Rider profile not found"));

        if (!isAdmin && !profile.getUserId().equals(currentUserId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Access denied: you cannot delete this profile");
        }

        riderProfileRepository.delete(profile);
    }

    @Override
    @Transactional
    public RiderLocationResponse updateLocation(UUID userId, RiderLocationUpdateRequest request) {
        RiderProfile profile = riderProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Rider profile not found"));

        RiderLocation location = riderLocationRepository.findById(profile.getId())
                .orElseGet(() -> RiderLocation.builder()
                        .riderProfile(profile)
                        .build());

        location.setLatitude(request.latitude());
        location.setLongitude(request.longitude());
        location.setUpdatedAt(Instant.now());

        RiderLocation saved = riderLocationRepository.save(location);
        return RiderMapper.toLocationResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public RiderLocationResponse getLocation(UUID userId) {
        RiderProfile profile = riderProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Rider profile not found"));

        RiderLocation location = riderLocationRepository.findById(profile.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Rider location not recorded yet"));

        return RiderMapper.toLocationResponse(location);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RiderProfileResponse> findAvailableRiders() {
        return riderProfileRepository.findAllByOnboardingStatusAndAvailable(OnboardingStatus.APPROVED, true)
                .stream()
                .map(RiderMapper::toProfileResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RiderLocationResponse getLocationByProfileId(UUID profileId) {
        RiderLocation location = riderLocationRepository.findById(profileId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Rider location not found"));
        return RiderMapper.toLocationResponse(location);
    }
}
