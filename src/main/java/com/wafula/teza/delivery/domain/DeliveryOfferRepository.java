package com.wafula.teza.delivery.domain;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Persistence port for {@link DeliveryOffer}. Implemented by Spring Data JPA at runtime.
 */
public interface DeliveryOfferRepository extends JpaRepository<DeliveryOffer, UUID> {

    List<DeliveryOffer> findByDeliveryId(UUID deliveryId);

    List<DeliveryOffer> findByRiderId(UUID riderId);

    List<DeliveryOffer> findByRiderIdAndStatus(UUID riderId, OfferStatus status);
}
