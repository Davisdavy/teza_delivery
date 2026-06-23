package com.wafula.teza.delivery.application;

import com.wafula.teza.delivery.api.dto.DeliveryCreateRequest;
import com.wafula.teza.delivery.api.dto.DeliveryOfferResponse;
import com.wafula.teza.delivery.api.dto.DeliveryResponse;
import com.wafula.teza.delivery.api.dto.DeliveryStatusHistoryResponse;
import com.wafula.teza.delivery.api.dto.DeliveryStatusUpdateRequest;
import com.wafula.teza.delivery.api.dto.DeliveryUpdateRequest;
import com.wafula.teza.delivery.api.dto.OfferCreateRequest;
import com.wafula.teza.delivery.api.dto.OfferResponseRequest;
import com.wafula.teza.delivery.api.dto.RiderStatsResponse;
import com.wafula.teza.dispatch.domain.RankedRider;
import java.util.List;
import java.util.UUID;

import com.wafula.teza.shared.api.dto.PagedResponse;

public interface DeliveryService {


    RiderStatsResponse getRiderStats(UUID riderUserId);

    DeliveryResponse createDelivery(UUID userId, DeliveryCreateRequest request);

    DeliveryResponse getDeliveryById(UUID deliveryId, UUID currentUserId, boolean isAdmin);

    List<DeliveryResponse> getDeliveriesForMerchant(UUID merchantUserId);

    List<DeliveryResponse> getDeliveriesForCustomer(UUID customerUserId);

    List<DeliveryResponse> getDeliveriesForRider(UUID riderUserId);

    List<DeliveryOfferResponse> getOffersForRider(UUID riderUserId);

    DeliveryResponse updateDelivery(UUID deliveryId, UUID userId, DeliveryUpdateRequest request);

    DeliveryResponse updateDeliveryStatus(UUID deliveryId, UUID currentUserId, boolean isAdmin, DeliveryStatusUpdateRequest request);

    void deleteDelivery(UUID deliveryId, UUID userId, boolean isAdmin);

    DeliveryOfferResponse createOffer(UUID deliveryId, OfferCreateRequest request);

    DeliveryOfferResponse respondToOffer(UUID offerId, UUID riderUserId, OfferResponseRequest request);

    DeliveryOfferResponse getOfferById(UUID offerId, UUID currentUserId);

    List<DeliveryOfferResponse> getOffersForDelivery(UUID deliveryId, UUID currentUserId, boolean isAdmin);

    List<DeliveryStatusHistoryResponse> getStatusHistory(UUID deliveryId, UUID currentUserId, boolean isAdmin);

    List<RankedRider> findMatchingRiders(UUID deliveryId, UUID currentUserId, boolean isAdmin);

    List<DeliveryResponse> getAllDeliveries();

    PagedResponse<DeliveryResponse> getAllDeliveries(int page, int size);
}
