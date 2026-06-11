package com.wafula.teza.auth.application;

import com.wafula.teza.auth.config.JwtProperties;
import com.wafula.teza.auth.domain.RefreshToken;
import com.wafula.teza.auth.domain.RefreshTokenRepository;
import com.wafula.teza.auth.infrastructure.JwtService;
import com.wafula.teza.shared.domain.Role;
import com.wafula.teza.shared.exception.ApiException;
import com.wafula.teza.user.application.UserAccount;
import com.wafula.teza.user.application.UserAccountService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Default {@link AuthService}. Coordinates the password encoder, the
 * {@link AuthenticationManager}, JWT issuance, and refresh-token persistence,
 * delegating all identity storage to the user module's {@link UserAccountService}.
 */
@Service
public class AuthServiceImpl implements AuthService {

    /** Roles a user is allowed to obtain through public self-registration. */
    private static final Set<Role> SELF_SERVICE_ROLES = Set.of(Role.CUSTOMER, Role.MERCHANT);

    private final UserAccountService userAccountService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;

    public AuthServiceImpl(UserAccountService userAccountService,
            RefreshTokenRepository refreshTokenRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            JwtProperties jwtProperties) {
        this.userAccountService = userAccountService;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.jwtProperties = jwtProperties;
    }

    @Override
    @Transactional
    public TokenPair register(String email, String rawPassword, Role role) {
        if (!SELF_SERVICE_ROLES.contains(role)) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Role " + role + " cannot be self-registered");
        }
        // create(...) throws 409 if the email is already taken.
        UserAccount account = userAccountService.create(email, passwordEncoder.encode(rawPassword), role);
        return issueTokens(account);
    }

    @Override
    @Transactional
    public TokenPair login(String email, String rawPassword) {
        // Throws BadCredentialsException (-> 401) when authentication fails.
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, rawPassword));
        UserAccount account = userAccountService.findByEmail(email)
                .orElseThrow(() -> unauthorized("Invalid credentials"));
        return issueTokens(account);
    }

    @Override
    @Transactional
    public TokenPair refresh(String refreshToken) {
        Claims claims = parseRefreshToken(refreshToken);
        RefreshToken stored = refreshTokenRepository.findByTokenHash(hash(refreshToken))
                .orElseThrow(() -> unauthorized("Unknown refresh token"));
        if (!stored.isActive(Instant.now())) {
            throw unauthorized("Refresh token expired or revoked");
        }
        if (!stored.getUserId().equals(jwtService.extractUserId(claims))) {
            throw unauthorized("Refresh token does not match its subject");
        }
        stored.setRevoked(true); // rotation: the presented token is single-use
        UserAccount account = userAccountService.findById(stored.getUserId())
                .orElseThrow(() -> unauthorized("Account no longer exists"));
        return issueTokens(account);
    }

    @Override
    @Transactional
    public void logout(String refreshToken) {
        // Best-effort and idempotent: revoke if we recognise it, otherwise do nothing.
        refreshTokenRepository.findByTokenHash(hash(refreshToken))
                .ifPresent(token -> token.setRevoked(true));
    }

    @Override
    @Transactional
    public String generateResetToken(String email) {
        return userAccountService.generateResetToken(email);
    }

    @Override
    @Transactional
    public void resetPasswordWithToken(String token, String newPassword) {
        userAccountService.resetPasswordWithToken(token, newPassword);
    }

    private TokenPair issueTokens(UserAccount account) {
        String accessToken = jwtService.generateAccessToken(account.id(), account.email(), account.role());
        String refreshToken = jwtService.generateRefreshToken(account.id());
        refreshTokenRepository.save(RefreshToken.builder()
                .tokenHash(hash(refreshToken))
                .userId(account.id())
                .expiresAt(Instant.now().plus(jwtProperties.refreshTokenTtl()))
                .revoked(false)
                .build());
        return new TokenPair(accessToken, refreshToken, jwtProperties.accessTokenTtl().toSeconds());
    }

    private Claims parseRefreshToken(String refreshToken) {
        try {
            Claims claims = jwtService.parse(refreshToken);
            if (!jwtService.isRefreshToken(claims)) {
                throw unauthorized("Not a refresh token");
            }
            return claims;
        } catch (JwtException | IllegalArgumentException ex) {
            throw unauthorized("Invalid refresh token");
        }
    }

    private ApiException unauthorized(String message) {
        return new ApiException(HttpStatus.UNAUTHORIZED, message);
    }

    /** SHA-256 hex digest — what we persist, so the raw token never touches the DB. */
    private String hash(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (java.security.NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 unavailable", ex);
        }
    }
}
