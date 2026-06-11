package com.wafula.teza.rider.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Persistence port for {@link RiderProfile}. Implemented by Spring Data JPA at runtime.
 */
public interface RiderProfileRepository extends JpaRepository<RiderProfile, UUID> {

    Optional<RiderProfile> findByUserId(UUID userId);

    boolean existsByUserId(UUID userId);

    List<RiderProfile> findAllByOnboardingStatusAndAvailable(OnboardingStatus onboardingStatus, boolean available);
}
