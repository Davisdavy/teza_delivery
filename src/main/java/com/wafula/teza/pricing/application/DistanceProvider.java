package com.wafula.teza.pricing.application;

import com.wafula.teza.pricing.domain.DistanceResult;
import com.wafula.teza.pricing.domain.Location;

public interface DistanceProvider {
    DistanceResult calculate(Location pickup, Location dropoff);
}
