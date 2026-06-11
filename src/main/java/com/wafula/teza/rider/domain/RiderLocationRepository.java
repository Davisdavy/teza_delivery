package com.wafula.teza.rider.domain;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Persistence port for {@link RiderLocation}. Implemented by Spring Data JPA at runtime.
 */
public interface RiderLocationRepository extends JpaRepository<RiderLocation, UUID> {
}
