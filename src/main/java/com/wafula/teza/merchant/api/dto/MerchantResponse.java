package com.wafula.teza.merchant.api.dto;

import java.time.Instant;
import java.util.UUID;

public record MerchantResponse(
        UUID id,
        UUID userId,
        String businessName,
        String phoneNumber,
        String address,
        Instant createdAt,
        Instant updatedAt
) {
}
