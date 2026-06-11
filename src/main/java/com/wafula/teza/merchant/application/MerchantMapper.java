package com.wafula.teza.merchant.application;

import com.wafula.teza.merchant.api.dto.MerchantResponse;
import com.wafula.teza.merchant.domain.Merchant;

public final class MerchantMapper {

    private MerchantMapper() {
    }

    public static MerchantResponse toResponse(Merchant merchant) {
        if (merchant == null) {
            return null;
        }
        return new MerchantResponse(
                merchant.getId(),
                merchant.getUserId(),
                merchant.getBusinessName(),
                merchant.getPhoneNumber(),
                merchant.getAddress(),
                merchant.getCreatedAt(),
                merchant.getUpdatedAt()
        );
    }
}
