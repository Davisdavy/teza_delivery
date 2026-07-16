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

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MerchantServiceImpl.class);

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
    @Transactional
    public MerchantResponse getProfileByUserId(UUID userId) {
        Merchant merchant = merchantRepository.findByUserId(userId)
                .orElseGet(() -> {
                    UserAccount user = userAccountService.findById(userId).orElse(null);
                    if (user != null && user.role() == Role.MERCHANT) {
                        log.warn("Self-healing: Auto-initializing missing Merchant profile for user {}", userId);
                        Merchant newMerchant = Merchant.builder()
                                .userId(userId)
                                .businessName("Business Name Pending")
                                .phoneNumber("0000000000")
                                .address("Address Pending")
                                .build();
                        return merchantRepository.save(newMerchant);
                    }
                    throw new ApiException(HttpStatus.NOT_FOUND, "Merchant profile not found");
                });
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
    @Transactional
    public List<MerchantResponse> getAllProfiles() {
        try {
            List<UserAccount> merchantUsers = userAccountService.findAll().stream()
                    .filter(u -> u.role() == Role.MERCHANT)
                    .toList();
            for (UserAccount user : merchantUsers) {
                if (!merchantRepository.existsByUserId(user.id())) {
                    log.warn("Self-healing: Auto-initializing missing Merchant profile for user {} during bulk listing", user.id());
                    Merchant newMerchant = Merchant.builder()
                            .userId(user.id())
                            .businessName("Business Name Pending")
                            .phoneNumber("0000000000")
                            .address("Address Pending")
                            .build();
                    merchantRepository.save(newMerchant);
                }
            }
        } catch (Exception e) {
            log.error("Failed to perform self-healing for merchants: {}", e.getMessage());
        }

        return merchantRepository.findAll().stream()
                .map(MerchantMapper::toResponse)
                .toList();
    }
}
