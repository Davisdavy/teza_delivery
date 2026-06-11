package com.wafula.teza.dispatch.domain;

import java.util.UUID;

/**
 * Represents a rider that has been matched, containing calculated distance and weighted score.
 */
public record RankedRider(
        UUID riderId,
        double distanceKm,
        double score
) {
}
