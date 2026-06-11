package com.wafula.teza.auth.api.dto;

import jakarta.validation.constraints.NotBlank;

/** Body for {@code POST /api/auth/refresh} and {@code POST /api/auth/logout}. */
public record RefreshTokenRequest(
        @NotBlank String refreshToken) {
}
