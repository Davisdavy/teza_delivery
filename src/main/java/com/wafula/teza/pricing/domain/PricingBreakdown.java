package com.wafula.teza.pricing.domain;

import java.math.BigDecimal;

public record PricingBreakdown(
        BigDecimal baseFee,
        BigDecimal distanceFee,
        BigDecimal timeFee,
        BigDecimal multiplier,
        BigDecimal finalFee
) {
}
