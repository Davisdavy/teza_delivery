package com.wafula.teza.user.api.dto;

import com.wafula.teza.shared.domain.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UserUpdateRequest(
        @Email(message = "Invalid email format")
        @Size(max = 255, message = "Email cannot exceed 255 characters")
        String email,

        Boolean enabled,

        Role role
) {
}
