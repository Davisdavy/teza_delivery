package com.wafula.teza.notification.api.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request payload to unregister a device push token.
 */
public record UnregisterTokenRequest(
    @NotBlank(message = "Token is required")
    String token
) {
}
