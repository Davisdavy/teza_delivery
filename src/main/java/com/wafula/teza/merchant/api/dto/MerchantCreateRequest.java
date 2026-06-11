package com.wafula.teza.merchant.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MerchantCreateRequest(
        @NotBlank(message = "Business name is required")
        @Size(max = 100, message = "Business name cannot exceed 100 characters")
        String businessName,

        @Size(max = 32, message = "Phone number cannot exceed 32 characters")
        String phoneNumber,

        @Size(max = 255, message = "Address cannot exceed 255 characters")
        String address
) {
}
