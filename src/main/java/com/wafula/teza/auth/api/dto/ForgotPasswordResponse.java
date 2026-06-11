package com.wafula.teza.auth.api.dto;

/** Response for forgot password request carrying the mock/testing reset token. */
public record ForgotPasswordResponse(
        String email,
        String resetToken) {
}
