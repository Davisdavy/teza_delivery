package com.wafula.teza.user.application;

import com.wafula.teza.shared.domain.Role;
import java.util.UUID;

/**
 * Public, read-only view of a user account — the user module's cross-module contract.
 *
 * <p>Includes {@code passwordHash} because the {@code auth} module must verify
 * credentials; callers that only need identity should ignore it.
 */
public record UserAccount(
        UUID id,
        String email,
        String passwordHash,
        Role role,
        boolean enabled) {
}
