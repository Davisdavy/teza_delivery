package com.wafula.teza.auth.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** Body for requesting password reset code/token. */
public record ForgotPasswordRequest(
        @Email @NotBlank String email) {
}
