package com.wafula.teza.auth.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A persisted, revocable refresh token.
 *
 * <p>Only a SHA-256 {@code tokenHash} of the issued token is stored, so a database
 * leak does not expose usable tokens. Tokens are rotated on every refresh (the old
 * row is revoked) and revoked on logout.
 *
 * <p>References the owning user by {@code userId} rather than a JPA association: the
 * {@code User} entity belongs to the user module, and auth must not couple to it at
 * the persistence layer. The DB still enforces the FK on {@code user_id}.
 */
@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(nullable = false)
    @Builder.Default
    private boolean revoked = false;

    /** A token is usable only while it is neither revoked nor past its expiry. */
    public boolean isActive(Instant now) {
        return !revoked && expiresAt.isAfter(now);
    }
}
