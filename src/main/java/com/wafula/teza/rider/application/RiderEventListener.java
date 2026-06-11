package com.wafula.teza.rider.application;

import com.wafula.teza.rider.domain.OnboardingStatus;
import com.wafula.teza.rider.domain.RiderProfile;
import com.wafula.teza.rider.domain.RiderProfileRepository;
import com.wafula.teza.rider.domain.VehicleType;
import com.wafula.teza.shared.domain.Role;
import com.wafula.teza.shared.event.UserRoleChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Listens to user identity changes and synchronizes rider profiles.
 */
@Component
public class RiderEventListener {

    private static final Logger log = LoggerFactory.getLogger(RiderEventListener.class);

    private final RiderProfileRepository riderProfileRepository;

    public RiderEventListener(RiderProfileRepository riderProfileRepository) {
        this.riderProfileRepository = riderProfileRepository;
    }

    @EventListener
    @Transactional
    public void onUserRoleChanged(UserRoleChangedEvent event) {
        if (event.newRole() == Role.RIDER) {
            if (!riderProfileRepository.existsByUserId(event.userId())) {
                log.info("Auto-initializing RiderProfile for user {} after elevation to RIDER", event.userId());
                RiderProfile profile = RiderProfile.builder()
                        .userId(event.userId())
                        .vehicleType(VehicleType.MOTORCYCLE) // default placeholder
                        .vehiclePlateNum("PENDING")          // default placeholder
                        .onboardingStatus(OnboardingStatus.PENDING)
                        .available(false)
                        .build();
                riderProfileRepository.save(profile);
            }
        }
    }
}
