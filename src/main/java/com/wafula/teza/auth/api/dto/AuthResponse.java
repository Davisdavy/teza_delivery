package com.wafula.teza.auth.api.dto;

import com.wafula.teza.auth.application.TokenPair;

/**
 * Token response returned by register / login / refresh.
 *
 * @param tokenType always {@code "Bearer"}.
 * @param expiresIn access-token lifetime in seconds.
 */
public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn) {

    public static AuthResponse from(TokenPair tokens) {
        return new AuthResponse(
                tokens.accessToken(),
                tokens.refreshToken(),
                "Bearer",
                tokens.accessTokenExpiresIn());
    }
}
