package com.wafula.teza.dispatch.domain;

import com.wafula.teza.dispatch.config.DispatchProperties;

/**
 * Strategy interface for pluggable rider scoring.
 */
public interface RiderScoringStrategy {

    /**
     * Compute a scoring value for a candidate rider. Higher value indicates a better fit.
     *
     * @param rider      the candidate rider details
     * @param distanceKm the computed distance in kilometers
     * @param config     the dispatch scoring weights
     * @return the computed score value
     */
    double computeScore(CandidateRider rider, double distanceKm, DispatchProperties config);
}
