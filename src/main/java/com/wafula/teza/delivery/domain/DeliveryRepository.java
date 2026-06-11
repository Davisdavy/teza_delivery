package com.wafula.teza.delivery.domain;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Persistence port for {@link Delivery}. Implemented by Spring Data JPA at runtime.
 */
public interface DeliveryRepository extends JpaRepository<Delivery, UUID> {

    List<Delivery> findByMerchantId(UUID merchantId);

    List<Delivery> findByCustomerId(UUID customerId);

    List<Delivery> findByRiderId(UUID riderId);

    List<Delivery> findByStatus(DeliveryStatus status);
}
