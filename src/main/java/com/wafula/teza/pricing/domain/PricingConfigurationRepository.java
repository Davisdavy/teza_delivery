package com.wafula.teza.pricing.domain;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PricingConfigurationRepository extends JpaRepository<PricingConfiguration, UUID> {
    Optional<PricingConfiguration> findFirstByOrderByUpdatedAtDesc();
}
