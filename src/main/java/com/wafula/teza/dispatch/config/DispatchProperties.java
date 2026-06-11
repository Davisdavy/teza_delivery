package com.wafula.teza.dispatch.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Settings for the rider matching and dispatch engine.
 */
@ConfigurationProperties(prefix = "teza.dispatch")
public record DispatchProperties(
        @DefaultValue("10.0") double maxRadiusKm,
        @DefaultValue("0.6") double distanceWeight,
        @DefaultValue("0.4") double ratingWeight
) {
}
