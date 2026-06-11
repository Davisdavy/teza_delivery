package com.wafula.teza.delivery.api.dto;

import jakarta.validation.constraints.NotNull;

public record OfferResponseRequest(
        @NotNull(message = "Accepted boolean is required")
        Boolean accepted
) {
}
