package com.wafula.teza.merchant.api;

import com.wafula.teza.merchant.api.dto.MerchantCreateRequest;
import com.wafula.teza.merchant.api.dto.MerchantResponse;
import com.wafula.teza.merchant.api.dto.MerchantUpdateRequest;
import com.wafula.teza.merchant.application.MerchantService;
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
 * REST controller for Merchant Profile endpoints.
 * Authenticated calls are secured via Spring Security configuration at /api/merchant/**
 */
@RestController
@RequestMapping("/api/merchant")
public class MerchantController {

    private final MerchantService merchantService;

    public MerchantController(MerchantService merchantService) {
        this.merchantService = merchantService;
    }

    @PostMapping("/profile")
    @ResponseStatus(HttpStatus.CREATED)
    public MerchantResponse createProfile(
            @AuthenticationPrincipal UUID currentUserId,
            @Valid @RequestBody MerchantCreateRequest request) {
        return merchantService.createProfile(currentUserId, request);
    }

    @GetMapping("/profile")
    public MerchantResponse getProfile(@AuthenticationPrincipal UUID currentUserId) {
        return merchantService.getProfileByUserId(currentUserId);
    }

    @GetMapping("/profile/{id}")
    public MerchantResponse getProfileById(
            @PathVariable UUID id,
            @AuthenticationPrincipal UUID currentUserId,
            Authentication authentication) {
        boolean isAdmin = hasAdminRole(authentication);
        return merchantService.getProfileById(id, currentUserId, isAdmin);
    }

    @GetMapping("/profiles")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SUPPORT_ADMIN')")
    public List<MerchantResponse> getAllProfiles() {
        return merchantService.getAllProfiles();
    }

    @PutMapping("/profile")
    public MerchantResponse updateProfile(
            @AuthenticationPrincipal UUID currentUserId,
            @Valid @RequestBody MerchantUpdateRequest request) {
        return merchantService.updateProfile(currentUserId, request);
    }

    @DeleteMapping("/profile/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProfile(
            @PathVariable UUID id,
            @AuthenticationPrincipal UUID currentUserId,
            Authentication authentication) {
        boolean isAdmin = hasSuperAdminRole(authentication);
        merchantService.deleteProfile(id, currentUserId, isAdmin);
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
