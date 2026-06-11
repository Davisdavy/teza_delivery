package com.wafula.teza.dispatch.domain;

import java.util.UUID;

/**
 * Represents a rider candidate available for matching, containing physical position
 * and scoring parameters.
 */
public record CandidateRider(
        UUID riderId,
        double latitude,
        double longitude,
        boolean online,
        boolean available,
        double rating
) {
}
