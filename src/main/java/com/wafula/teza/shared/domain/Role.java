package com.wafula.teza.shared.domain;

/**
 * Application roles, part of the shared kernel because both {@code user} (identity
 * attribute) and {@code auth} (Spring Security authorities, JWT claim) depend on them.
 * Mapped to authorities as {@code ROLE_<name>}.
 *
 * <p>{@link #CUSTOMER} and {@link #MERCHANT} are self-service (public registration);
 * {@link #ADMIN} is seeded from configuration and {@link #RIDER} is provisioned by
 * the rider module — neither may be obtained through the public registration endpoint.
 */
public enum Role {
    SUPER_ADMIN,
    SUPPORT_ADMIN,
    MERCHANT,
    RIDER,
    CUSTOMER
}
