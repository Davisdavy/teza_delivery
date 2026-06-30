package com.wafula.teza.pricing.infrastructure;

import com.wafula.teza.pricing.application.DistanceProvider;
import com.wafula.teza.pricing.domain.DistanceResult;
import com.wafula.teza.pricing.domain.Location;
import java.util.function.BiFunction;

public class MockDistanceProvider implements DistanceProvider {

    private BiFunction<Location, Location, DistanceResult> behavior = (pickup, dropoff) -> {
        // Default behavior: calculate Haversine distance
        double earthRadius = 6371; // km
        double dLat = Math.toRadians(dropoff.latitude() - pickup.latitude());
        double dLng = Math.toRadians(dropoff.longitude() - pickup.longitude());
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(pickup.latitude())) * Math.cos(Math.toRadians(dropoff.latitude())) *
                   Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distanceKm = earthRadius * c;
        double durationMinutes = distanceKm * 2.0; // 30 km/h average
        return new DistanceResult(distanceKm, durationMinutes);
    };

    @Override
    public DistanceResult calculate(Location pickup, Location dropoff) {
        return behavior.apply(pickup, dropoff);
    }

    public void setBehavior(BiFunction<Location, Location, DistanceResult> behavior) {
        this.behavior = behavior;
    }

    public void setFixedResult(double distanceKm, double durationMinutes) {
        this.behavior = (pickup, dropoff) -> new DistanceResult(distanceKm, durationMinutes);
    }
}
