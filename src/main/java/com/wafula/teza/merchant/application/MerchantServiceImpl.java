package com.wafula.teza.merchant.application;

import com.wafula.teza.merchant.api.dto.MerchantCreateRequest;
import com.wafula.teza.merchant.api.dto.MerchantResponse;
import com.wafula.teza.merchant.api.dto.MerchantUpdateRequest;
import com.wafula.teza.merchant.domain.Merchant;
import com.wafula.teza.merchant.domain.MerchantRepository;
import com.wafula.teza.shared.domain.Role;
import com.wafula.teza.shared.exception.ApiException;
import com.wafula.teza.user.application.UserAccount;
import com.wafula.teza.user.application.UserAccountService;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MerchantServiceImpl implements MerchantService {

    private final MerchantRepository merchantRepository;
    private final UserAccountService userAccountService;

    public MerchantServiceImpl(MerchantRepository merchantRepository, UserAccountService userAccountService) {
        this.merchantRepository = merchantRepository;
        this.userAccountService = userAccountService;
    }

    @Override
    @Transactional
    public MerchantResponse createProfile(UUID userId, MerchantCreateRequest request) {
        if (merchantRepository.existsByUserId(userId)) {
            throw new ApiException(HttpStatus.CONFLICT, "Merchant profile already exists for this user");
        }

        // Verify user exists and has MERCHANT role
        UserAccount user = userAccountService.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User account not found"));
        if (user.role() != Role.MERCHANT) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Only users with MERCHANT role can create a merchant profile");
        }

        Merchant merchant = Merchant.builder()
                .userId(userId)
                .businessName(request.businessName())
                .phoneNumber(request.phoneNumber())
                .address(request.address())
                .build();

        Merchant saved = merchantRepository.save(merchant);
        return MerchantMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public MerchantResponse getProfileByUserId(UUID userId) {
        Merchant merchant = merchantRepository.findByUserId(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Merchant profile not found"));
        return MerchantMapper.toResponse(merchant);
    }

    @Override
    @Transactional(readOnly = true)
    public MerchantResponse getProfileById(UUID profileId, UUID currentUserId, boolean isAdmin) {
        Merchant merchant = merchantRepository.findById(profileId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Merchant profile not found"));

        if (!isAdmin && !merchant.getUserId().equals(currentUserId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Access denied: you do not own this profile");
        }

        return MerchantMapper.toResponse(merchant);
    }

    @Override
    @Transactional
    public MerchantResponse updateProfile(UUID userId, MerchantUpdateRequest request) {
        Merchant merchant = merchantRepository.findByUserId(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Merchant profile not found"));

        if (request.businessName() != null) {
            merchant.setBusinessName(request.businessName());
        }
        if (request.phoneNumber() != null) {
            merchant.setPhoneNumber(request.phoneNumber());
        }
        if (request.address() != null) {
            merchant.setAddress(request.address());
        }

        Merchant updated = merchantRepository.save(merchant);
        return MerchantMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteProfile(UUID profileId, UUID currentUserId, boolean isAdmin) {
        Merchant merchant = merchantRepository.findById(profileId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Merchant profile not found"));

        if (!isAdmin && !merchant.getUserId().equals(currentUserId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Access denied: you cannot delete this profile");
        }

        merchantRepository.delete(merchant);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MerchantResponse> getAllProfiles() {
        return merchantRepository.findAll().stream()
                .map(MerchantMapper::toResponse)
                .toList();
    }
}
