package com.wafula.teza.dispatch.application;

import com.wafula.teza.dispatch.domain.CandidateRider;
import com.wafula.teza.dispatch.domain.DeliveryRequest;
import com.wafula.teza.dispatch.domain.RankedRider;
import java.util.List;

/**
 * Service orchestrating the matching of delivery requests to candidate riders.
 */
public interface RiderMatchingService {

    /**
     * Finds, scores, and ranks riders suitable for a delivery request.
     *
     * @param request    the delivery request parameters
     * @param candidates a list of potential riders
     * @return a sorted list of ranked riders (highest score first)
     */
    List<RankedRider> match(DeliveryRequest request, List<CandidateRider> candidates);
}
