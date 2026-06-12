package com.wafula.teza.user.api;

import com.wafula.teza.shared.exception.ApiException;
import com.wafula.teza.user.api.dto.UserResponse;
import com.wafula.teza.user.api.dto.UserUpdateRequest;
import com.wafula.teza.user.api.dto.ChangePasswordRequest;
import com.wafula.teza.user.application.UserAccount;
import com.wafula.teza.user.application.UserAccountService;
import com.wafula.teza.user.application.UserMapper;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for managing user accounts.
 * Authenticated calls are secured via Spring Security configuration at /api/users/**
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserAccountService userAccountService;

    public UserController(UserAccountService userAccountService) {
        this.userAccountService = userAccountService;
    }

    @GetMapping("/me")
    public UserResponse getMe(@AuthenticationPrincipal UUID currentUserId) {
        UserAccount account = userAccountService.findById(currentUserId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User account not found"));
        return UserMapper.toResponse(account);
    }

    @PutMapping("/me")
    public UserResponse updateMe(
            @AuthenticationPrincipal UUID currentUserId,
            @Valid @RequestBody UserUpdateRequest request) {
        // Regular users can only update their own email
        UserAccount updated = userAccountService.update(currentUserId, request.email(), null, null, currentUserId);
        return UserMapper.toResponse(updated);
    }

    @PutMapping("/me/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(
            @AuthenticationPrincipal UUID currentUserId,
            @Valid @RequestBody ChangePasswordRequest request) {
        userAccountService.changePassword(currentUserId, request.oldPassword(), request.newPassword());
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SUPPORT_ADMIN')")
    public List<UserResponse> getAllUsers() {
        return userAccountService.findAll().stream()
                .map(UserMapper::toResponse)
                .toList();
    }

    @GetMapping("/{id}")
    public UserResponse getUserById(
            @PathVariable UUID id,
            @AuthenticationPrincipal UUID currentUserId,
            Authentication authentication) {
        if (!hasAdminRole(authentication) && !currentUserId.equals(id)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Access denied: you cannot view other user accounts");
        }
        UserAccount account = userAccountService.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User account not found"));
        return UserMapper.toResponse(account);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SUPPORT_ADMIN')")
    public UserResponse updateUserById(
            @PathVariable UUID id,
            @AuthenticationPrincipal UUID currentUserId,
            @Valid @RequestBody UserUpdateRequest request) {
        UserAccount updated = userAccountService.update(id, request.email(), request.enabled(), request.role(), currentUserId);
        return UserMapper.toResponse(updated);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(
            @PathVariable UUID id,
            @AuthenticationPrincipal UUID currentUserId,
            Authentication authentication) {
        if (!hasSuperAdminRole(authentication) && !currentUserId.equals(id)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Access denied: you cannot delete other user accounts");
        }
        userAccountService.delete(id);
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
