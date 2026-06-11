package com.wafula.teza.delivery.application;

import com.wafula.teza.delivery.api.dto.DeliveryOfferResponse;
import com.wafula.teza.delivery.api.dto.DeliveryResponse;
import com.wafula.teza.delivery.api.dto.DeliveryStatusHistoryResponse;
import com.wafula.teza.delivery.domain.Delivery;
import com.wafula.teza.delivery.domain.DeliveryOffer;
import com.wafula.teza.delivery.domain.DeliveryStatusHistory;

public final class DeliveryMapper {

    private DeliveryMapper() {
    }

    public static DeliveryResponse toResponse(Delivery delivery) {
        if (delivery == null) {
            return null;
        }
        return new DeliveryResponse(
                delivery.getId(),
                delivery.getMerchantId(),
                delivery.getCustomerId(),
                delivery.getRiderId(),
                delivery.getPickupAddress(),
                delivery.getPickupLatitude(),
                delivery.getPickupLongitude(),
                delivery.getDropoffAddress(),
                delivery.getDropoffLatitude(),
                delivery.getDropoffLongitude(),
                delivery.getStatus(),
                delivery.getDeliveryFee(),
                delivery.getAcceptedAt(),
                delivery.getPickedUpAt(),
                delivery.getDeliveredAt(),
                delivery.getCancelledAt(),
                delivery.getCreatedAt(),
                delivery.getUpdatedAt()
        );
    }

    public static DeliveryOfferResponse toOfferResponse(DeliveryOffer offer) {
        if (offer == null) {
            return null;
        }
        return new DeliveryOfferResponse(
                offer.getId(),
                offer.getDelivery().getId(),
                offer.getRiderId(),
                offer.getStatus(),
                offer.getExpiresAt(),
                offer.getCreatedAt(),
                offer.getUpdatedAt()
        );
    }

    public static DeliveryStatusHistoryResponse toHistoryResponse(DeliveryStatusHistory history) {
        if (history == null) {
            return null;
        }
        return new DeliveryStatusHistoryResponse(
                history.getId(),
                history.getDelivery().getId(),
                history.getStatus(),
                history.getChangedByUserId(),
                history.getReason(),
                history.getCreatedAt()
        );
    }
}
