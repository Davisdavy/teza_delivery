package com.wafula.teza.delivery.api.dto;

public record RiderStatsResponse(
        long completedDeliveries,
        long cancelledDeliveries,
        long declinedOffers,
        long expiredOffers
) {
}
