package com.wafula.teza.auth.api.dto;

import com.wafula.teza.shared.domain.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Body for {@code POST /api/auth/register}.
 *
 * @param role requested role; the service rejects anything other than CUSTOMER or MERCHANT.
 */
public record RegisterRequest(
        @Email @NotBlank String email,
        @NotBlank @Size(min = 8, max = 100) String password,
        @NotNull Role role) {
}
