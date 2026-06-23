package com.wafula.teza.notification.api.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request payload for registering a push notification token.
 */
public record RegisterTokenRequest(
    @NotBlank(message = "Token is required")
    String token,
    String deviceId,
    String deviceType,
    String appVersion
) {
}
