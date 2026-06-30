package com.wafula.teza.pricing.api.dto;

import java.math.BigDecimal;

public record PricingEstimateResponse(
        BigDecimal baseFee,
        BigDecimal distanceFee,
        BigDecimal timeFee,
        BigDecimal multiplier,
        BigDecimal finalFee
) {}
