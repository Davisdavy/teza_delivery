package com.wafula.teza.pricing.application;

import com.wafula.teza.pricing.domain.Location;
import com.wafula.teza.pricing.domain.PricingBreakdown;
import com.wafula.teza.pricing.domain.PricingConfiguration;
import java.math.BigDecimal;
import java.util.UUID;

public interface PricingService {

    PricingBreakdown calculateFee(Location pickup, Location dropoff);

    PricingConfiguration getActiveConfiguration();

    PricingConfiguration updateConfiguration(UUID adminId, PricingConfigurationUpdateParameters params);

    record PricingConfigurationUpdateParameters(
            BigDecimal baseFee,
            BigDecimal pricePerKilometer,
            BigDecimal pricePerMinute,
            BigDecimal minimumDeliveryFee,
            BigDecimal maximumDeliveryFee,
            boolean surgeEnabled,
            BigDecimal peakHourMultiplier,
            BigDecimal weekendMultiplier,
            BigDecimal nightMultiplier
    ) {}
}
