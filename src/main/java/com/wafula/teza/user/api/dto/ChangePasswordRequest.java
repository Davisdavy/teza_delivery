package com.wafula.teza.user.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Body for authenticated password change requests. */
public record ChangePasswordRequest(
        @NotBlank String oldPassword,
        @NotBlank @Size(min = 6, message = "Password must be at least 6 characters") String newPassword) {
}
