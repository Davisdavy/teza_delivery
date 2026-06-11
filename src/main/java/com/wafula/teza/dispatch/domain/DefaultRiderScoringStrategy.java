package com.wafula.teza.dispatch.domain;

import com.wafula.teza.dispatch.config.DispatchProperties;
import org.springframework.stereotype.Component;

/**
 * Default scoring strategy combining distance proximity and normalized ratings.
 * Computes: score = (1 / (1 + distance)) * distanceWeight + (rating / 5.0) * ratingWeight
 */
@Component
public class DefaultRiderScoringStrategy implements RiderScoringStrategy {

    @Override
    public double computeScore(CandidateRider rider, double distanceKm, DispatchProperties config) {
        // Compute proximity score: decreases as distance increases. Max value is 1.0 at distance = 0.
        double proximityScore = 1.0 / (1.0 + distanceKm);

        // Normalize rating: assumes ratings are in range 0.0 to 5.0.
        double normalizedRating = rider.rating() / 5.0;

        return (proximityScore * config.distanceWeight()) + (normalizedRating * config.ratingWeight());
    }
}
