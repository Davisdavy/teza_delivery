package com.wafula.teza.user.api.dto;

import com.wafula.teza.shared.domain.Role;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String email,
        Role role,
        boolean enabled
) {
}
