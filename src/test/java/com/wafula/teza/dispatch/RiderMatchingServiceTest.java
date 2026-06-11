package com.wafula.teza.dispatch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.wafula.teza.dispatch.application.RiderMatchingService;
import com.wafula.teza.dispatch.application.RiderMatchingServiceImpl;
import com.wafula.teza.dispatch.config.DispatchProperties;
import com.wafula.teza.dispatch.domain.CandidateRider;
import com.wafula.teza.dispatch.domain.DefaultRiderScoringStrategy;
import com.wafula.teza.dispatch.domain.DeliveryRequest;
import com.wafula.teza.dispatch.domain.RankedRider;
import com.wafula.teza.dispatch.domain.RiderScoringStrategy;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RiderMatchingServiceTest {

    private RiderMatchingService matchingService;
    private DispatchProperties config;
    private RiderScoringStrategy defaultStrategy;

    @BeforeEach
    void setUp() {
        config = new DispatchProperties(10.0, 0.6, 0.4);
        defaultStrategy = new DefaultRiderScoringStrategy();
        matchingService = new RiderMatchingServiceImpl(config, defaultStrategy);
    }

    @Test
    void testMatchingFiltersAndRankings() {
        UUID closeRiderId = UUID.randomUUID();
        UUID farRiderId = UUID.randomUUID();
        UUID offlineRiderId = UUID.randomUUID();
        UUID unavailableRiderId = UUID.randomUUID();
        UUID outOfRangeRiderId = UUID.randomUUID();

        // Pickup coordinates (Nairobi Central)
        double pickupLat = -1.2833;
        double pickupLon = 36.8167;
        DeliveryRequest request = new DeliveryRequest(UUID.randomUUID(), pickupLat, pickupLon);

        List<CandidateRider> candidates = List.of(
                // 1. Close, online, available, rating 4.0 (~1.2 km away)
                new CandidateRider(closeRiderId, -1.2800, 36.8100, true, true, 4.0),
                // 2. Far but in range, online, available, rating 5.0 (~5.3 km away)
                new CandidateRider(farRiderId, -1.2500, 36.8500, true, true, 5.0),
                // 3. Close but offline
                new CandidateRider(offlineRiderId, -1.2800, 36.8100, false, true, 4.5),
                // 4. Close but unavailable
                new CandidateRider(unavailableRiderId, -1.2800, 36.8100, true, false, 4.5),
                // 5. Online & available but out of range (> 10 km)
                new CandidateRider(outOfRangeRiderId, -1.1500, 36.8200, true, true, 4.8)
        );

        List<RankedRider> result = matchingService.match(request, candidates);

        // Assertions
        // Out of range, offline, and unavailable riders should be filtered out
        assertEquals(2, result.size(), "Only close and far available/online riders should remain");

        // The close rider is much closer and has a reasonable rating, so should rank first
        RankedRider first = result.get(0);
        assertEquals(closeRiderId, first.riderId(), "Close rider should be ranked first");
        assertTrue(first.distanceKm() < 2.0, "Close rider distance should be small");

        RankedRider second = result.get(1);
        assertEquals(farRiderId, second.riderId(), "Far rider should be ranked second");
        assertTrue(second.distanceKm() > 4.0, "Far rider distance should be larger");
    }

    @Test
    void testPluggableStrategyReplacement() {
        // Define a custom strategy where ONLY rating matters (distance weight is ignored)
        RiderScoringStrategy customStrategy = (rider, distance, cfg) -> rider.rating();
        RiderMatchingService customMatchingService = new RiderMatchingServiceImpl(config, customStrategy);

        UUID lowRatingCloseId = UUID.randomUUID();
        UUID highRatingFarId = UUID.randomUUID();

        DeliveryRequest request = new DeliveryRequest(UUID.randomUUID(), -1.2833, 36.8167);

        List<CandidateRider> candidates = List.of(
                // Close but low rating
                new CandidateRider(lowRatingCloseId, -1.2800, 36.8100, true, true, 2.0),
                // Further but high rating
                new CandidateRider(highRatingFarId, -1.2500, 36.8500, true, true, 5.0)
        );

        List<RankedRider> result = customMatchingService.match(request, candidates);

        assertEquals(2, result.size());
        // With rating-only strategy, the high rating far rider must be ranked first
        assertEquals(highRatingFarId, result.get(0).riderId(), "High rating far rider should be ranked first");
    }
}
