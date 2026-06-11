package com.wafula.teza.auth.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** Body for {@code POST /api/auth/login}. */
public record LoginRequest(
        @Email @NotBlank String email,
        @NotBlank String password) {
}
