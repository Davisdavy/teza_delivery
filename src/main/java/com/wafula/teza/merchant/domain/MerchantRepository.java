package com.wafula.teza.merchant.domain;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Persistence port for {@link Merchant}. Implemented by Spring Data JPA at runtime.
 */
public interface MerchantRepository extends JpaRepository<Merchant, UUID> {

    Optional<Merchant> findByUserId(UUID userId);

    boolean existsByUserId(UUID userId);
}
