package com.wafula.teza.shared.event;

import com.wafula.teza.shared.domain.Role;
import java.util.UUID;

/** Event published when a user's role is updated. */
public record UserRoleChangedEvent(
        UUID userId,
        Role oldRole,
        Role newRole) {
}
