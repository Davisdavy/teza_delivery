package com.wafula.teza.rider.api;

import com.wafula.teza.rider.api.dto.RiderLocationResponse;
import com.wafula.teza.rider.api.dto.RiderLocationUpdateRequest;
import com.wafula.teza.rider.api.dto.RiderProfileCreateRequest;
import com.wafula.teza.rider.api.dto.RiderProfileResponse;
import com.wafula.teza.rider.api.dto.RiderProfileUpdateRequest;
import com.wafula.teza.rider.api.dto.RiderOnboardingUpdateRequest;
import com.wafula.teza.rider.application.RiderService;
import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for Rider Profile and Location endpoints.
 * Authenticated calls are secured via Spring Security configuration at /api/rider/**
 */
@RestController
@RequestMapping("/api/rider")
public class RiderController {

    private final RiderService riderService;

    public RiderController(RiderService riderService) {
        this.riderService = riderService;
    }

    @PostMapping("/profile")
    @ResponseStatus(HttpStatus.CREATED)
    public RiderProfileResponse createProfile(
            @AuthenticationPrincipal UUID currentUserId,
            @Valid @RequestBody RiderProfileCreateRequest request) {
        return riderService.createProfile(currentUserId, request);
    }

    @GetMapping("/profile")
    public RiderProfileResponse getProfile(@AuthenticationPrincipal UUID currentUserId) {
        return riderService.getProfileByUserId(currentUserId);
    }

    @GetMapping("/profile/{id}")
    public RiderProfileResponse getProfileById(
            @PathVariable UUID id,
            @AuthenticationPrincipal UUID currentUserId,
            Authentication authentication) {
        boolean isAdmin = hasAdminRole(authentication);
        return riderService.getProfileById(id, currentUserId, isAdmin);
    }

    @GetMapping("/profiles")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SUPPORT_ADMIN')")
    public List<RiderProfileResponse> getAllProfiles() {
        return riderService.getAllProfiles();
    }

    @PutMapping("/profile")
    public RiderProfileResponse updateProfile(
            @AuthenticationPrincipal UUID currentUserId,
            @Valid @RequestBody RiderProfileUpdateRequest request) {
        return riderService.updateProfile(currentUserId, request);
    }

    @PutMapping("/profile/{id}/onboarding")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SUPPORT_ADMIN')")
    public RiderProfileResponse updateOnboardingStatus(
            @PathVariable UUID id,
            @AuthenticationPrincipal UUID currentUserId,
            @Valid @RequestBody RiderOnboardingUpdateRequest request) {
        return riderService.updateOnboardingStatus(id, request.onboardingStatus(), currentUserId);
    }

    @DeleteMapping("/profile/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProfile(
            @PathVariable UUID id,
            @AuthenticationPrincipal UUID currentUserId,
            Authentication authentication) {
        boolean isAdmin = hasSuperAdminRole(authentication);
        riderService.deleteProfile(id, currentUserId, isAdmin);
    }

    @PutMapping("/location")
    public RiderLocationResponse updateLocation(
            @AuthenticationPrincipal UUID currentUserId,
            @Valid @RequestBody RiderLocationUpdateRequest request) {
        return riderService.updateLocation(currentUserId, request);
    }

    @GetMapping("/location")
    public RiderLocationResponse getLocation(@AuthenticationPrincipal UUID currentUserId) {
        return riderService.getLocation(currentUserId);
    }

    @GetMapping("/profile/{id}/location")
    public RiderLocationResponse getLocationByProfileId(@PathVariable UUID id) {
        return riderService.getLocationByProfileId(id);
    }

    private boolean hasAdminRole(Authentication authentication) {
        if (authentication == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority ->
                    grantedAuthority.getAuthority().equals("ROLE_SUPER_ADMIN") ||
                    grantedAuthority.getAuthority().equals("ROLE_SUPPORT_ADMIN"));
    }

    private boolean hasSuperAdminRole(Authentication authentication) {
        if (authentication == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_SUPER_ADMIN"));
    }
}
