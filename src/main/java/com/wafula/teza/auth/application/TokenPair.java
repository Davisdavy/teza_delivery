package com.wafula.teza.auth.application;

/**
 * The result of a successful authentication: a short-lived access token plus the
 * refresh token used to obtain the next one.
 *
 * @param accessToken           bearer token for protected requests.
 * @param refreshToken          token exchanged at {@code /api/auth/refresh}.
 * @param accessTokenExpiresIn  access-token lifetime in seconds.
 */
public record TokenPair(String accessToken, String refreshToken, long accessTokenExpiresIn) {
}
