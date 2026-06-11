package com.wafula.teza.merchant.application;

import com.wafula.teza.merchant.api.dto.MerchantCreateRequest;
import com.wafula.teza.merchant.api.dto.MerchantResponse;
import com.wafula.teza.merchant.api.dto.MerchantUpdateRequest;
import java.util.UUID;

public interface MerchantService {

    MerchantResponse createProfile(UUID userId, MerchantCreateRequest request);

    MerchantResponse getProfileByUserId(UUID userId);

    MerchantResponse getProfileById(UUID profileId, UUID currentUserId, boolean isAdmin);

    MerchantResponse updateProfile(UUID userId, MerchantUpdateRequest request);

    void deleteProfile(UUID profileId, UUID currentUserId, boolean isAdmin);
}
