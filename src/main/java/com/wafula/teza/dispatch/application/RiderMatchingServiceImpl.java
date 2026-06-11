package com.wafula.teza.dispatch.application;

import com.wafula.teza.dispatch.config.DispatchProperties;
import com.wafula.teza.dispatch.domain.CandidateRider;
import com.wafula.teza.dispatch.domain.DeliveryRequest;
import com.wafula.teza.dispatch.domain.RankedRider;
import com.wafula.teza.dispatch.domain.RiderScoringStrategy;
import java.util.List;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

@Service
@EnableConfigurationProperties(DispatchProperties.class)
public class RiderMatchingServiceImpl implements RiderMatchingService {

    private static final double EARTH_RADIUS_KM = 6371.0;

    private final DispatchProperties properties;
    private final RiderScoringStrategy scoringStrategy;

    public RiderMatchingServiceImpl(DispatchProperties properties, RiderScoringStrategy scoringStrategy) {
        this.properties = properties;
        this.scoringStrategy = scoringStrategy;
    }

    @Override
    public List<RankedRider> match(DeliveryRequest request, List<CandidateRider> candidates) {
        if (candidates == null || candidates.isEmpty()) {
            return List.of();
        }

        return candidates.stream()
                // 1. Filter online and available riders
                .filter(CandidateRider::online)
                .filter(CandidateRider::available)
                // 2. Map and calculate distance
                .map(rider -> {
                    double distance = calculateHaversineDistance(
                            request.pickupLatitude(), request.pickupLongitude(),
                            rider.latitude(), rider.longitude()
                    );
                    return new Object() {
                        final CandidateRider candidate = rider;
                        final double dist = distance;
                    };
                })
                // 3. Filter riders within the maximum search radius
                .filter(wrapper -> wrapper.dist <= properties.maxRadiusKm())
                // 4. Compute score and map to RankedRider
                .map(wrapper -> {
                    double score = scoringStrategy.computeScore(
                            wrapper.candidate,
                            wrapper.dist,
                            properties
                    );
                    return new RankedRider(wrapper.candidate.riderId(), wrapper.dist, score);
                })
                // 5. Sort descending by score
                .sorted((r1, r2) -> Double.compare(r2.score(), r1.score()))
                .toList();
    }

    /**
     * Calculates the great-circle distance between two points using the Haversine formula.
     */
    private double calculateHaversineDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2.0) * Math.sin(dLat / 2.0)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2.0) * Math.sin(dLon / 2.0);

        double c = 2.0 * Math.atan2(Math.sqrt(a), Math.sqrt(1.0 - a));
        return EARTH_RADIUS_KM * c;
    }
}
