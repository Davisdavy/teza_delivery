package com.wafula.teza.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Settings for seeding the first {@code ADMIN} account, bound from
 * {@code teza.security.bootstrap-admin.*}. Credentials should be supplied via
 * environment variables, never committed.
 *
 * @param enabled  whether to attempt seeding on startup.
 * @param email    admin email; seeding is skipped if an account with it exists.
 * @param password raw password, encoded before persisting.
 */
@ConfigurationProperties(prefix = "teza.security.bootstrap-admin")
public record BootstrapAdminProperties(boolean enabled, String email, String password) {
}
