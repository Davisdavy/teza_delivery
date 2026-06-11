package com.wafula.teza.delivery.domain;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Persistence port for {@link DeliveryStatusHistory}. Implemented by Spring Data JPA at runtime.
 */
public interface DeliveryStatusHistoryRepository extends JpaRepository<DeliveryStatusHistory, UUID> {

    List<DeliveryStatusHistory> findByDeliveryIdOrderByCreatedAtDesc(UUID deliveryId);
}
