package com.wafula.teza.delivery.application;

import com.wafula.teza.delivery.api.dto.DeliveryCreateRequest;
import com.wafula.teza.delivery.api.dto.DeliveryOfferResponse;
import com.wafula.teza.delivery.api.dto.DeliveryResponse;
import com.wafula.teza.delivery.api.dto.DeliveryStatusHistoryResponse;
import com.wafula.teza.delivery.api.dto.DeliveryStatusUpdateRequest;
import com.wafula.teza.delivery.api.dto.DeliveryUpdateRequest;
import com.wafula.teza.delivery.api.dto.OfferCreateRequest;
import com.wafula.teza.delivery.api.dto.OfferResponseRequest;
import com.wafula.teza.delivery.domain.Delivery;
import com.wafula.teza.delivery.domain.DeliveryOffer;
import com.wafula.teza.delivery.domain.DeliveryOfferRepository;
import com.wafula.teza.delivery.domain.DeliveryRepository;
import com.wafula.teza.delivery.domain.DeliveryStatus;
import com.wafula.teza.delivery.domain.DeliveryStatusHistory;
import com.wafula.teza.delivery.domain.DeliveryStatusHistoryRepository;
import com.wafula.teza.delivery.domain.OfferStatus;
import com.wafula.teza.merchant.api.dto.MerchantResponse;
import com.wafula.teza.merchant.application.MerchantService;
import com.wafula.teza.rider.api.dto.RiderProfileResponse;
import com.wafula.teza.rider.application.RiderService;
import com.wafula.teza.shared.domain.Role;
import com.wafula.teza.shared.exception.ApiException;
import com.wafula.teza.user.application.UserAccount;
import com.wafula.teza.user.application.UserAccountService;
import java.time.Instant;
import com.wafula.teza.dispatch.application.RiderMatchingService;
import com.wafula.teza.dispatch.domain.CandidateRider;
import com.wafula.teza.dispatch.domain.DeliveryRequest;
import com.wafula.teza.dispatch.domain.RankedRider;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.ApplicationEventPublisher;
import com.wafula.teza.shared.event.DeliveryStatusChangedEvent;
import com.wafula.teza.shared.event.DeliveryOfferCreatedEvent;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeliveryServiceImpl implements DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final DeliveryOfferRepository deliveryOfferRepository;
    private final DeliveryStatusHistoryRepository deliveryStatusHistoryRepository;
    private final MerchantService merchantService;
    private final RiderService riderService;
    private final UserAccountService userAccountService;
    private final ApplicationEventPublisher eventPublisher;
    private final RiderMatchingService riderMatchingService;
    private final Map<UUID, List<UUID>> matchingRidersCache = new ConcurrentHashMap<>();

    public DeliveryServiceImpl(
            DeliveryRepository deliveryRepository,
            DeliveryOfferRepository deliveryOfferRepository,
            DeliveryStatusHistoryRepository deliveryStatusHistoryRepository,
            MerchantService merchantService,
            RiderService riderService,
            UserAccountService userAccountService,
            ApplicationEventPublisher eventPublisher,
            RiderMatchingService riderMatchingService) {
        this.deliveryRepository = deliveryRepository;
        this.deliveryOfferRepository = deliveryOfferRepository;
        this.deliveryStatusHistoryRepository = deliveryStatusHistoryRepository;
        this.merchantService = merchantService;
        this.riderService = riderService;
        this.userAccountService = userAccountService;
        this.eventPublisher = eventPublisher;
        this.riderMatchingService = riderMatchingService;
    }

    @Override
    @Transactional
    public DeliveryResponse createDelivery(UUID userId, DeliveryCreateRequest request) {
        UserAccount user = userAccountService.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User account not found"));

        UUID merchantId = null;
        UUID customerId = null;

        if (user.role() == Role.MERCHANT) {
            MerchantResponse merchant = merchantService.getProfileByUserId(userId);
            merchantId = merchant.id();
        } else if (user.role() == Role.CUSTOMER || user.role() == Role.SUPER_ADMIN || user.role() == Role.SUPPORT_ADMIN) {
            customerId = userId;
        } else {
            throw new ApiException(HttpStatus.FORBIDDEN, "Only customers, merchants, and admins can place deliveries");
        }

        Delivery delivery = Delivery.builder()
                .merchantId(merchantId)
                .customerId(customerId)
                .pickupAddress(request.pickupAddress())
                .pickupLatitude(request.pickupLatitude())
                .pickupLongitude(request.pickupLongitude())
                .dropoffAddress(request.dropoffAddress())
                .dropoffLatitude(request.dropoffLatitude())
                .dropoffLongitude(request.dropoffLongitude())
                .status(DeliveryStatus.PENDING)
                .deliveryFee(request.deliveryFee())
                .build();

        Delivery saved = deliveryRepository.save(delivery);
        logStatusChange(saved, DeliveryStatus.PENDING, userId, "Delivery created as pending");

        eventPublisher.publishEvent(new DeliveryStatusChangedEvent(
                saved.getId(),
                null,
                DeliveryStatus.PENDING,
                saved.getCustomerId(),
                saved.getMerchantId(),
                saved.getRiderId(),
                saved.getPickupAddress(),
                saved.getDropoffAddress()
        ));

        return DeliveryMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public DeliveryResponse getDeliveryById(UUID deliveryId, UUID currentUserId, boolean isAdmin) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Delivery not found"));

        if (!isAdmin) {
            verifyDeliveryAccess(delivery, currentUserId);
        }

        return DeliveryMapper.toResponse(delivery);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeliveryResponse> getDeliveriesForMerchant(UUID merchantUserId) {
        MerchantResponse merchant = getMerchantProfile(merchantUserId);
        return deliveryRepository.findByMerchantId(merchant.id()).stream()
                .map(DeliveryMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeliveryResponse> getDeliveriesForCustomer(UUID customerUserId) {
        return deliveryRepository.findByCustomerId(customerUserId).stream()
                .map(DeliveryMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeliveryResponse> getDeliveriesForRider(UUID riderUserId) {
        RiderProfileResponse rider = getRiderProfile(riderUserId);
        return deliveryRepository.findByRiderId(rider.id()).stream()
                .map(DeliveryMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeliveryOfferResponse> getOffersForRider(UUID riderUserId) {
        RiderProfileResponse rider = getRiderProfile(riderUserId);
        return deliveryOfferRepository.findByRiderIdAndStatus(rider.id(), OfferStatus.PENDING).stream()
                .map(DeliveryMapper::toOfferResponse)
                .toList();
    }

    @Override
    @Transactional
    public DeliveryResponse updateDelivery(UUID deliveryId, UUID userId, DeliveryUpdateRequest request) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Delivery not found"));

        if (!isDeliveryOwner(delivery, userId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Access denied: you do not own this delivery");
        }

        if (delivery.getStatus() != DeliveryStatus.PENDING && delivery.getStatus() != DeliveryStatus.SEARCHING) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Cannot update delivery details in status: " + delivery.getStatus());
        }

        if (request.pickupAddress() != null) {
            delivery.setPickupAddress(request.pickupAddress());
        }
        if (request.pickupLatitude() != null) {
            delivery.setPickupLatitude(request.pickupLatitude());
        }
        if (request.pickupLongitude() != null) {
            delivery.setPickupLongitude(request.pickupLongitude());
        }
        if (request.dropoffAddress() != null) {
            delivery.setDropoffAddress(request.dropoffAddress());
        }
        if (request.dropoffLatitude() != null) {
            delivery.setDropoffLatitude(request.dropoffLatitude());
        }
        if (request.dropoffLongitude() != null) {
            delivery.setDropoffLongitude(request.dropoffLongitude());
        }
        if (request.deliveryFee() != null) {
            delivery.setDeliveryFee(request.deliveryFee());
        }

        Delivery updated = deliveryRepository.save(delivery);
        return DeliveryMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public DeliveryResponse updateDeliveryStatus(UUID deliveryId, UUID currentUserId, boolean isAdmin, DeliveryStatusUpdateRequest request) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Delivery not found"));

        DeliveryStatus oldStatus = delivery.getStatus();
        DeliveryStatus newStatus = request.status();

        if (oldStatus == newStatus) {
            return DeliveryMapper.toResponse(delivery);
        }

        // Validate state transitions & authority
        if (isAdmin) {
            // Admin can override
        } else {
            validateTransition(delivery, oldStatus, newStatus, currentUserId);
        }

        updateDeliveryStatusAndTimestamps(delivery, newStatus);
        Delivery updated = deliveryRepository.save(delivery);

        String reason = request.reason() != null ? request.reason() : "Status changed to " + newStatus;
        logStatusChange(updated, newStatus, currentUserId, reason);

        if (newStatus == DeliveryStatus.SEARCHING) {
            triggerNextOffer(updated);
        }

        return DeliveryMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteDelivery(UUID deliveryId, UUID userId, boolean isAdmin) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Delivery not found"));

        if (!isAdmin && !isDeliveryOwner(delivery, userId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Access denied: you do not own this delivery");
        }

        if (delivery.getStatus() != DeliveryStatus.PENDING && delivery.getStatus() != DeliveryStatus.SEARCHING) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Only pending or searching deliveries can be deleted");
        }

        deliveryRepository.delete(delivery);
    }

    @Override
    @Transactional
    public DeliveryOfferResponse createOffer(UUID deliveryId, OfferCreateRequest request) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Delivery not found"));

        if (delivery.getStatus() != DeliveryStatus.PENDING && delivery.getStatus() != DeliveryStatus.SEARCHING) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Offers can only be created for PENDING or SEARCHING deliveries");
        }

        // Verify rider profile exists
        RiderProfileResponse riderProfile;
        try {
            riderProfile = riderService.getProfileById(request.riderId(), null, true);
        } catch (ApiException ex) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Rider profile not found");
        }

        DeliveryOffer offer = DeliveryOffer.builder()
                .delivery(delivery)
                .riderId(request.riderId())
                .status(OfferStatus.PENDING)
                .expiresAt(Instant.now().plusSeconds(request.durationSeconds()))
                .build();

        DeliveryOffer saved = deliveryOfferRepository.save(offer);

        eventPublisher.publishEvent(new DeliveryOfferCreatedEvent(
                saved.getId(),
                delivery.getId(),
                request.riderId(),
                riderProfile.userId(),
                request.durationSeconds()
        ));

        return DeliveryMapper.toOfferResponse(saved);
    }

    @Override
    @Transactional
    public DeliveryOfferResponse respondToOffer(UUID offerId, UUID riderUserId, OfferResponseRequest request) {
        DeliveryOffer offer = deliveryOfferRepository.findById(offerId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Offer not found"));

        if (offer.getStatus() != OfferStatus.PENDING) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Offer is already resolved: " + offer.getStatus());
        }

        if (Instant.now().isAfter(offer.getExpiresAt())) {
            offer.setStatus(OfferStatus.EXPIRED);
            deliveryOfferRepository.save(offer);
            throw new ApiException(HttpStatus.BAD_REQUEST, "Offer has expired");
        }

        RiderProfileResponse riderProfile = getRiderProfile(riderUserId);
        if (!offer.getRiderId().equals(riderProfile.id())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Access denied: this offer is not for you");
        }

        if (request.accepted()) {
            offer.setStatus(OfferStatus.ACCEPTED);
            Delivery delivery = offer.getDelivery();

            if (delivery.getStatus() != DeliveryStatus.PENDING && delivery.getStatus() != DeliveryStatus.SEARCHING) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Delivery is no longer pending assignment");
            }

            delivery.setRiderId(riderProfile.id());
            updateDeliveryStatusAndTimestamps(delivery, DeliveryStatus.ASSIGNED);
            deliveryRepository.save(delivery);

            logStatusChange(delivery, DeliveryStatus.ASSIGNED, riderUserId, "Delivery accepted by rider");

            // Mark other offers for this delivery as EXPIRED
            List<DeliveryOffer> otherOffers = deliveryOfferRepository.findByDeliveryId(delivery.getId());
            for (DeliveryOffer other : otherOffers) {
                if (!other.getId().equals(offer.getId()) && other.getStatus() == OfferStatus.PENDING) {
                    other.setStatus(OfferStatus.EXPIRED);
                    deliveryOfferRepository.save(other);
                }
            }
        } else {
            offer.setStatus(OfferStatus.DECLINED);
            Delivery delivery = offer.getDelivery();
            if (delivery.getStatus() == DeliveryStatus.SEARCHING) {
                triggerNextOffer(delivery);
            }
        }

        DeliveryOffer updated = deliveryOfferRepository.save(offer);
        return DeliveryMapper.toOfferResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeliveryOfferResponse> getOffersForDelivery(UUID deliveryId, UUID currentUserId, boolean isAdmin) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Delivery not found"));

        if (!isAdmin) {
            verifyDeliveryAccess(delivery, currentUserId);
        }

        return deliveryOfferRepository.findByDeliveryId(deliveryId).stream()
                .map(DeliveryMapper::toOfferResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeliveryStatusHistoryResponse> getStatusHistory(UUID deliveryId, UUID currentUserId, boolean isAdmin) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Delivery not found"));

        if (!isAdmin) {
            verifyDeliveryAccess(delivery, currentUserId);
        }

        return deliveryStatusHistoryRepository.findByDeliveryIdOrderByCreatedAtDesc(deliveryId).stream()
                .map(DeliveryMapper::toHistoryResponse)
                .toList();
    }

    // Helper methods
    private MerchantResponse getMerchantProfile(UUID userId) {
        try {
            return merchantService.getProfileByUserId(userId);
        } catch (ApiException ex) {
            throw new ApiException(HttpStatus.FORBIDDEN, "User does not have a Merchant profile");
        }
    }

    private RiderProfileResponse getRiderProfile(UUID userId) {
        try {
            return riderService.getProfileByUserId(userId);
        } catch (ApiException ex) {
            throw new ApiException(HttpStatus.FORBIDDEN, "User does not have a Rider profile");
        }
    }

    private boolean isDeliveryOwner(Delivery delivery, UUID userId) {
        try {
            UserAccount user = userAccountService.findById(userId).orElse(null);
            if (user == null) {
                return false;
            }
            if (user.role() == Role.MERCHANT) {
                MerchantResponse merchant = merchantService.getProfileByUserId(userId);
                return delivery.getMerchantId() != null && delivery.getMerchantId().equals(merchant.id());
            } else if (user.role() == Role.CUSTOMER) {
                return delivery.getCustomerId() != null && delivery.getCustomerId().equals(userId);
            }
        } catch (ApiException ignored) {}
        return false;
    }

    private void verifyDeliveryAccess(Delivery delivery, UUID userId) {
        boolean hasAccess = false;
        if (delivery.getCustomerId() != null && delivery.getCustomerId().equals(userId)) {
            hasAccess = true;
        }

        if (!hasAccess) {
            try {
                MerchantResponse merchant = merchantService.getProfileByUserId(userId);
                if (delivery.getMerchantId() != null && delivery.getMerchantId().equals(merchant.id())) {
                    hasAccess = true;
                }
            } catch (ApiException ignored) {}
        }

        if (!hasAccess) {
            try {
                RiderProfileResponse rider = riderService.getProfileByUserId(userId);
                if (delivery.getRiderId() != null && delivery.getRiderId().equals(rider.id())) {
                    hasAccess = true;
                }
                if (!hasAccess) {
                    List<DeliveryOffer> offers = deliveryOfferRepository.findByRiderIdAndStatus(rider.id(), OfferStatus.PENDING);
                    boolean hasPendingOffer = offers.stream()
                            .anyMatch(o -> o.getDelivery().getId().equals(delivery.getId()));
                    if (hasPendingOffer) {
                        hasAccess = true;
                    }
                }
            } catch (ApiException ignored) {}
        }

        if (!hasAccess) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Access denied: you are not linked to this delivery");
        }
    }

    private void updateDeliveryStatusAndTimestamps(Delivery delivery, DeliveryStatus newStatus) {
        DeliveryStatus oldStatus = delivery.getStatus();
        delivery.setStatus(newStatus);
        Instant now = Instant.now();
        if (newStatus == DeliveryStatus.ASSIGNED) {
            delivery.setAcceptedAt(now);
            if (delivery.getRiderId() != null) {
                try {
                    RiderProfileResponse riderProfile = riderService.getProfileById(delivery.getRiderId(), null, true);
                    riderService.updateProfile(riderProfile.userId(), new com.wafula.teza.rider.api.dto.RiderProfileUpdateRequest(null, null, null, false));
                } catch (ApiException ignored) {}
            }
        } else if (newStatus == DeliveryStatus.PICKED_UP) {
            delivery.setPickedUpAt(now);
        } else if (newStatus == DeliveryStatus.DELIVERED) {
            delivery.setDeliveredAt(now);
            if (delivery.getRiderId() != null) {
                try {
                    RiderProfileResponse riderProfile = riderService.getProfileById(delivery.getRiderId(), null, true);
                    riderService.updateProfile(riderProfile.userId(), new com.wafula.teza.rider.api.dto.RiderProfileUpdateRequest(null, null, null, true));
                } catch (ApiException ignored) {}
            }
        } else if (newStatus == DeliveryStatus.CANCELLED) {
            delivery.setCancelledAt(now);
            if (delivery.getRiderId() != null) {
                try {
                    RiderProfileResponse riderProfile = riderService.getProfileById(delivery.getRiderId(), null, true);
                    riderService.updateProfile(riderProfile.userId(), new com.wafula.teza.rider.api.dto.RiderProfileUpdateRequest(null, null, null, true));
                } catch (ApiException ignored) {}
            }
        }

        eventPublisher.publishEvent(new DeliveryStatusChangedEvent(
                delivery.getId(),
                oldStatus,
                newStatus,
                delivery.getCustomerId(),
                delivery.getMerchantId(),
                delivery.getRiderId(),
                delivery.getPickupAddress(),
                delivery.getDropoffAddress()
        ));
    }

    private void logStatusChange(Delivery delivery, DeliveryStatus status, UUID userId, String reason) {
        DeliveryStatusHistory log = DeliveryStatusHistory.builder()
                .delivery(delivery)
                .status(status)
                .changedByUserId(userId)
                .reason(reason)
                .build();
        deliveryStatusHistoryRepository.save(log);
    }

    private void validateTransition(Delivery delivery, DeliveryStatus oldStatus, DeliveryStatus newStatus, UUID userId) {
        if (newStatus == DeliveryStatus.CANCELLED) {
            if (!isDeliveryOwner(delivery, userId)) {
                throw new ApiException(HttpStatus.FORBIDDEN, "Only the owner can cancel the delivery");
            }
            if (oldStatus == DeliveryStatus.DELIVERED || oldStatus == DeliveryStatus.CANCELLED) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Cannot cancel a delivery that is " + oldStatus);
            }
            return;
        }

        if (oldStatus == DeliveryStatus.PENDING && newStatus == DeliveryStatus.SEARCHING) {
            if (!isDeliveryOwner(delivery, userId)) {
                throw new ApiException(HttpStatus.FORBIDDEN, "Only the owner can submit this delivery to search");
            }
            return;
        }

        if (oldStatus == DeliveryStatus.ASSIGNED && newStatus == DeliveryStatus.ARRIVED) {
            RiderProfileResponse rider = getRiderProfile(userId);
            if (delivery.getRiderId() == null || !delivery.getRiderId().equals(rider.id())) {
                throw new ApiException(HttpStatus.FORBIDDEN, "Only the assigned rider can mark arrival");
            }
            return;
        }

        if (oldStatus == DeliveryStatus.ARRIVED && newStatus == DeliveryStatus.PICKED_UP) {
            RiderProfileResponse rider = getRiderProfile(userId);
            if (delivery.getRiderId() == null || !delivery.getRiderId().equals(rider.id())) {
                throw new ApiException(HttpStatus.FORBIDDEN, "Only the assigned rider can pick up this delivery");
            }
            return;
        }

        if (oldStatus == DeliveryStatus.PICKED_UP && newStatus == DeliveryStatus.IN_TRANSIT) {
            RiderProfileResponse rider = getRiderProfile(userId);
            if (delivery.getRiderId() == null || !delivery.getRiderId().equals(rider.id())) {
                throw new ApiException(HttpStatus.FORBIDDEN, "Only the assigned rider can mark delivery in transit");
            }
            return;
        }

        if (oldStatus == DeliveryStatus.IN_TRANSIT && newStatus == DeliveryStatus.DELIVERED) {
            RiderProfileResponse rider = getRiderProfile(userId);
            if (delivery.getRiderId() == null || !delivery.getRiderId().equals(rider.id())) {
                throw new ApiException(HttpStatus.FORBIDDEN, "Only the assigned rider can mark this delivery as delivered");
            }
            return;
        }

        throw new ApiException(HttpStatus.BAD_REQUEST, String.format("Unauthorized or invalid status transition from %s to %s", oldStatus, newStatus));
    }

    @Override
    @Transactional(readOnly = true)
    public List<RankedRider> findMatchingRiders(UUID deliveryId, UUID currentUserId, boolean isAdmin) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Delivery not found"));

        if (!isAdmin && !isDeliveryOwner(delivery, currentUserId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Access denied: you do not own this delivery");
        }

        DeliveryRequest deliveryRequest = new DeliveryRequest(
                delivery.getId(),
                delivery.getPickupLatitude(),
                delivery.getPickupLongitude()
        );

        List<CandidateRider> candidates = riderService.findAvailableRiders().stream()
                .map(profile -> {
                    try {
                        var location = riderService.getLocationByProfileId(profile.id());
                        return new CandidateRider(
                                profile.id(),
                                location.latitude(),
                                location.longitude(),
                                true, // online
                                true, // available
                                5.0  // rating
                        );
                    } catch (ApiException e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toList();

        return riderMatchingService.match(deliveryRequest, candidates);
    }

    private void triggerNextOffer(Delivery delivery) {
        List<UUID> candidates = matchingRidersCache.get(delivery.getId());
        if (candidates == null || candidates.isEmpty()) {
            List<UUID> offeredRiderIds = deliveryOfferRepository.findByDeliveryId(delivery.getId()).stream()
                    .map(DeliveryOffer::getRiderId)
                    .toList();

            DeliveryRequest deliveryRequest = new DeliveryRequest(
                    delivery.getId(),
                    delivery.getPickupLatitude(),
                    delivery.getPickupLongitude()
            );

            List<CandidateRider> candidateRiders = riderService.findAvailableRiders().stream()
                    .filter(profile -> !offeredRiderIds.contains(profile.id()))
                    .map(profile -> {
                        try {
                            var location = riderService.getLocationByProfileId(profile.id());
                            return new CandidateRider(
                                    profile.id(),
                                    location.latitude(),
                                    location.longitude(),
                                    true,
                                    true,
                                    5.0
                            );
                        } catch (ApiException e) {
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .toList();

            List<RankedRider> ranked = riderMatchingService.match(deliveryRequest, candidateRiders);
            candidates = new ArrayList<>(ranked.stream().map(RankedRider::riderId).toList());
            matchingRidersCache.put(delivery.getId(), candidates);
        }

        if (!candidates.isEmpty()) {
            UUID nextRiderId = candidates.remove(0);

            DeliveryOffer offer = DeliveryOffer.builder()
                    .delivery(delivery)
                    .riderId(nextRiderId)
                    .status(OfferStatus.PENDING)
                    .expiresAt(Instant.now().plusSeconds(45))
                    .build();

            DeliveryOffer saved = deliveryOfferRepository.save(offer);

            RiderProfileResponse riderProfile = riderService.getProfileById(nextRiderId, null, true);

            eventPublisher.publishEvent(new DeliveryOfferCreatedEvent(
                    saved.getId(),
                    delivery.getId(),
                    nextRiderId,
                    riderProfile.userId(),
                    45
            ));
        } else {
            matchingRidersCache.remove(delivery.getId());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeliveryResponse> getAllDeliveries() {
        return deliveryRepository.findAll().stream()
                .map(DeliveryMapper::toResponse)
                .toList();
    }
}
