package com.wafula.teza.auth.application;

import com.wafula.teza.shared.domain.Role;

/**
 * Use-case entry point for authentication. This is the auth module's public
 * application surface; other modules and the web layer depend on this interface,
 * never on the implementation or infrastructure details.
 */
public interface AuthService {

    /**
     * Register a self-service account.
     *
     * @param role must be {@link Role#CUSTOMER} or {@link Role#MERCHANT}; any other
     *             value is rejected.
     */
    TokenPair register(String email, String rawPassword, Role role);

    /** Authenticate with email + password and issue a fresh token pair. */
    TokenPair login(String email, String rawPassword);

    /** Exchange a valid, unrevoked refresh token for a new pair, rotating the old one. */
    TokenPair refresh(String refreshToken);

    /** Revoke the supplied refresh token so it can no longer be exchanged. */
    void logout(String refreshToken);

    String generateResetToken(String email);

    void resetPasswordWithToken(String token, String newPassword);
}
