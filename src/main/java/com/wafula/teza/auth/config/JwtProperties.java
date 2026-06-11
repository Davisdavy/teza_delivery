package com.wafula.teza.auth.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Externalised JWT settings, bound from {@code teza.security.jwt.*}.
 *
 * @param secret         HMAC signing secret; must be at least 32 bytes (256 bits) for HS256.
 * @param issuer         value placed in the {@code iss} claim and verified on parse.
 * @param accessTokenTtl lifetime of access tokens (short-lived).
 * @param refreshTokenTtl lifetime of refresh tokens (long-lived).
 */
@ConfigurationProperties(prefix = "teza.security.jwt")
public record JwtProperties(
        String secret,
        String issuer,
        Duration accessTokenTtl,
        Duration refreshTokenTtl) {
}
