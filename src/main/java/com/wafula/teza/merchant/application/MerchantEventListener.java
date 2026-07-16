package com.wafula.teza.merchant.application;

import com.wafula.teza.merchant.domain.Merchant;
import com.wafula.teza.merchant.domain.MerchantRepository;
import com.wafula.teza.shared.domain.Role;
import com.wafula.teza.shared.event.UserRoleChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Listens to user identity changes and synchronizes merchant profiles.
 */
@Component
public class MerchantEventListener {

    private static final Logger log = LoggerFactory.getLogger(MerchantEventListener.class);

    private final MerchantRepository merchantRepository;

    public MerchantEventListener(MerchantRepository merchantRepository) {
        this.merchantRepository = merchantRepository;
    }

    @EventListener
    @Transactional
    public void onUserRoleChanged(UserRoleChangedEvent event) {
        if (event.newRole() == Role.MERCHANT) {
            if (!merchantRepository.existsByUserId(event.userId())) {
                log.info("Auto-initializing Merchant profile for user {} after elevation/creation as MERCHANT", event.userId());
                Merchant merchant = Merchant.builder()
                        .userId(event.userId())
                        .businessName("Business Name Pending")
                        .phoneNumber("0000000000") // default placeholder
                        .address("Address Pending")  // default placeholder
                        .build();
                merchantRepository.save(merchant);
            }
        }
    }
}
