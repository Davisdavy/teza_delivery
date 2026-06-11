package com.wafula.teza.auth.infrastructure;

import com.wafula.teza.auth.config.JwtProperties;
import com.wafula.teza.shared.domain.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

/**
 * Issues and validates signed JWTs (HMAC-SHA256).
 *
 * <p>Access and refresh tokens share the signing key and issuer but differ in
 * lifetime and a {@code typ} claim, so a refresh token can never be replayed as
 * an access token. This class is pure JWT mechanics — it performs no persistence
 * or revocation checks (those live in the application service).
 */
@Service
public class JwtService {

    static final String CLAIM_TYPE = "typ";
    static final String CLAIM_ROLE = "role";
    static final String CLAIM_EMAIL = "email";
    static final String TYPE_ACCESS = "access";
    static final String TYPE_REFRESH = "refresh";

    private final JwtProperties properties;
    private final SecretKey signingKey;

    public JwtService(JwtProperties properties) {
        this.properties = properties;
        this.signingKey = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
    }

    /** Short-lived token carrying identity and role; the bearer for protected requests. */
    public String generateAccessToken(UUID userId, String email, Role role) {
        return buildToken(userId, TYPE_ACCESS, properties.accessTokenTtl().toMillis())
                .claim(CLAIM_ROLE, role.name())
                .claim(CLAIM_EMAIL, email)
                .compact();
    }

    /** Long-lived token exchanged for a fresh access token; persisted by hash for revocation. */
    public String generateRefreshToken(UUID userId) {
        return buildToken(userId, TYPE_REFRESH, properties.refreshTokenTtl().toMillis()).compact();
    }

    private io.jsonwebtoken.JwtBuilder buildToken(UUID userId, String type, long ttlMillis) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(userId.toString())
                .issuer(properties.issuer())
                .claim(CLAIM_TYPE, type)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(ttlMillis)))
                .signWith(signingKey);
    }

    /**
     * Verifies the signature, issuer and expiry and returns the claims.
     *
     * @throws JwtException if the token is malformed, expired, or fails verification.
     */
    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .requireIssuer(properties.issuer())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public UUID extractUserId(Claims claims) {
        return UUID.fromString(claims.getSubject());
    }

    public Role extractRole(Claims claims) {
        return Role.valueOf(claims.get(CLAIM_ROLE, String.class));
    }

    public boolean isAccessToken(Claims claims) {
        return TYPE_ACCESS.equals(claims.get(CLAIM_TYPE, String.class));
    }

    public boolean isRefreshToken(Claims claims) {
        return TYPE_REFRESH.equals(claims.get(CLAIM_TYPE, String.class));
    }
}
