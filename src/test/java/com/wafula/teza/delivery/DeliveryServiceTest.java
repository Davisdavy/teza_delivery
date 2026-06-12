package com.wafula.teza.delivery;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.wafula.teza.delivery.api.dto.DeliveryCreateRequest;
import com.wafula.teza.delivery.api.dto.DeliveryResponse;
import com.wafula.teza.delivery.api.dto.DeliveryUpdateRequest;
import com.wafula.teza.delivery.application.DeliveryService;
import com.wafula.teza.delivery.application.DeliveryServiceImpl;
import com.wafula.teza.delivery.domain.Delivery;
import com.wafula.teza.delivery.domain.DeliveryOfferRepository;
import com.wafula.teza.delivery.domain.DeliveryRepository;
import com.wafula.teza.delivery.domain.DeliveryStatus;
import com.wafula.teza.delivery.domain.DeliveryStatusHistoryRepository;
import com.wafula.teza.delivery.domain.DeliveryOffer;
import com.wafula.teza.delivery.domain.OfferStatus;
import com.wafula.teza.delivery.api.dto.OfferResponseRequest;
import com.wafula.teza.shared.event.DeliveryOfferCreatedEvent;
import com.wafula.teza.merchant.api.dto.MerchantResponse;
import com.wafula.teza.merchant.application.MerchantService;
import com.wafula.teza.rider.application.RiderService;
import com.wafula.teza.dispatch.application.RiderMatchingService;
import com.wafula.teza.dispatch.domain.RankedRider;
import com.wafula.teza.dispatch.domain.CandidateRider;
import com.wafula.teza.dispatch.domain.DeliveryRequest;
import com.wafula.teza.rider.api.dto.RiderProfileResponse;
import com.wafula.teza.shared.domain.Role;
import com.wafula.teza.shared.exception.ApiException;
import com.wafula.teza.user.application.UserAccount;
import com.wafula.teza.user.application.UserAccountService;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class DeliveryServiceTest {

    @Mock
    private DeliveryRepository deliveryRepository;

    @Mock
    private DeliveryOfferRepository deliveryOfferRepository;

    @Mock
    private DeliveryStatusHistoryRepository deliveryStatusHistoryRepository;

    @Mock
    private MerchantService merchantService;

    @Mock
    private RiderService riderService;

    @Mock
    private UserAccountService userAccountService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private RiderMatchingService riderMatchingService;

    private DeliveryService deliveryService;

    @BeforeEach
    void setUp() {
        deliveryService = new DeliveryServiceImpl(
                deliveryRepository,
                deliveryOfferRepository,
                deliveryStatusHistoryRepository,
                merchantService,
                riderService,
                userAccountService,
                eventPublisher,
                riderMatchingService
        );
    }

    @Test
    void testCreateDeliveryAsCustomer() {
        UUID userId = UUID.randomUUID();
        UserAccount customerAccount = new UserAccount(userId, "customer@test.com", "hash", Role.CUSTOMER, true);
        
        DeliveryCreateRequest request = new DeliveryCreateRequest(
                "Pickup 123", -1.2833, 36.8167,
                "Dropoff 456", -1.2933, 36.8267,
                BigDecimal.valueOf(150.00)
        );

        when(userAccountService.findById(userId)).thenReturn(Optional.of(customerAccount));
        
        Delivery savedDelivery = Delivery.builder()
                .id(UUID.randomUUID())
                .customerId(userId)
                .merchantId(null)
                .pickupAddress(request.pickupAddress())
                .pickupLatitude(request.pickupLatitude())
                .pickupLongitude(request.pickupLongitude())
                .dropoffAddress(request.dropoffAddress())
                .dropoffLatitude(request.dropoffLatitude())
                .dropoffLongitude(request.dropoffLongitude())
                .status(DeliveryStatus.PENDING)
                .deliveryFee(request.deliveryFee())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(deliveryRepository.save(any(Delivery.class))).thenReturn(savedDelivery);

        DeliveryResponse response = deliveryService.createDelivery(userId, request);

        assertNotNull(response);
        assertEquals(userId, response.customerId());
        assertNull(response.merchantId());
        assertEquals(DeliveryStatus.PENDING, response.status());
        verify(deliveryRepository).save(any(Delivery.class));
    }

    @Test
    void testCreateDeliveryAsMerchant() {
        UUID userId = UUID.randomUUID();
        UUID merchantId = UUID.randomUUID();
        UserAccount merchantAccount = new UserAccount(userId, "merchant@test.com", "hash", Role.MERCHANT, true);
        
        MerchantResponse merchantResponse = new MerchantResponse(
                merchantId, userId, "Test Business", "123456", "Biz St", Instant.now(), Instant.now()
        );

        DeliveryCreateRequest request = new DeliveryCreateRequest(
                "Pickup 123", -1.2833, 36.8167,
                "Dropoff 456", -1.2933, 36.8267,
                BigDecimal.valueOf(150.00)
        );

        when(userAccountService.findById(userId)).thenReturn(Optional.of(merchantAccount));
        when(merchantService.getProfileByUserId(userId)).thenReturn(merchantResponse);
        
        Delivery savedDelivery = Delivery.builder()
                .id(UUID.randomUUID())
                .customerId(null)
                .merchantId(merchantId)
                .pickupAddress(request.pickupAddress())
                .pickupLatitude(request.pickupLatitude())
                .pickupLongitude(request.pickupLongitude())
                .dropoffAddress(request.dropoffAddress())
                .dropoffLatitude(request.dropoffLatitude())
                .dropoffLongitude(request.dropoffLongitude())
                .status(DeliveryStatus.PENDING)
                .deliveryFee(request.deliveryFee())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(deliveryRepository.save(any(Delivery.class))).thenReturn(savedDelivery);

        DeliveryResponse response = deliveryService.createDelivery(userId, request);

        assertNotNull(response);
        assertEquals(merchantId, response.merchantId());
        assertNull(response.customerId());
        assertEquals(DeliveryStatus.PENDING, response.status());
        verify(deliveryRepository).save(any(Delivery.class));
    }

    @Test
    void testCreateDeliveryAsAdminOrRiderThrowsForbidden() {
        UUID adminId = UUID.randomUUID();
        UserAccount adminAccount = new UserAccount(adminId, "admin@test.com", "hash", Role.SUPER_ADMIN, true);
        
        DeliveryCreateRequest request = new DeliveryCreateRequest(
                "Pickup 123", -1.2833, 36.8167,
                "Dropoff 456", -1.2933, 36.8267,
                BigDecimal.valueOf(150.00)
        );

        when(userAccountService.findById(adminId)).thenReturn(Optional.of(adminAccount));

        ApiException exception = assertThrows(ApiException.class, () -> 
            deliveryService.createDelivery(adminId, request)
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
        assertEquals("Only customers and merchants can place deliveries", exception.getMessage());
    }

    @Test
    void testGetDeliveryByIdSuccessForOwnerCustomer() {
        UUID customerId = UUID.randomUUID();
        UUID deliveryId = UUID.randomUUID();

        Delivery delivery = Delivery.builder()
                .id(deliveryId)
                .customerId(customerId)
                .pickupAddress("A")
                .dropoffAddress("B")
                .status(DeliveryStatus.PENDING)
                .deliveryFee(BigDecimal.TEN)
                .build();

        when(deliveryRepository.findById(deliveryId)).thenReturn(Optional.of(delivery));

        DeliveryResponse response = deliveryService.getDeliveryById(deliveryId, customerId, false);

        assertNotNull(response);
        assertEquals(customerId, response.customerId());
    }

    @Test
    void testGetDeliveryByIdThrowsForbiddenForNonOwnerCustomer() {
        UUID ownerCustomerId = UUID.randomUUID();
        UUID nonOwnerCustomerId = UUID.randomUUID();
        UUID deliveryId = UUID.randomUUID();

        Delivery delivery = Delivery.builder()
                .id(deliveryId)
                .customerId(ownerCustomerId)
                .pickupAddress("A")
                .dropoffAddress("B")
                .status(DeliveryStatus.PENDING)
                .deliveryFee(BigDecimal.TEN)
                .build();

        when(deliveryRepository.findById(deliveryId)).thenReturn(Optional.of(delivery));
        // Mock profile lookup to raise exception for merchants/riders, confirming they aren't linked
        when(merchantService.getProfileByUserId(nonOwnerCustomerId)).thenThrow(new ApiException(HttpStatus.NOT_FOUND, "Not found"));
        when(riderService.getProfileByUserId(nonOwnerCustomerId)).thenThrow(new ApiException(HttpStatus.NOT_FOUND, "Not found"));

        ApiException exception = assertThrows(ApiException.class, () -> 
            deliveryService.getDeliveryById(deliveryId, nonOwnerCustomerId, false)
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
    }

    @Test
    void testGetDeliveriesForCustomer() {
        UUID customerId = UUID.randomUUID();
        Delivery delivery = Delivery.builder()
                .id(UUID.randomUUID())
                .customerId(customerId)
                .pickupAddress("A")
                .dropoffAddress("B")
                .status(DeliveryStatus.PENDING)
                .deliveryFee(BigDecimal.TEN)
                .build();

        when(deliveryRepository.findByCustomerId(customerId)).thenReturn(List.of(delivery));

        List<DeliveryResponse> result = deliveryService.getDeliveriesForCustomer(customerId);

        assertEquals(1, result.size());
        assertEquals(customerId, result.get(0).customerId());
    }

    @Test
    void testUpdateDeliverySuccessForCustomerOwner() {
        UUID customerId = UUID.randomUUID();
        UUID deliveryId = UUID.randomUUID();
        UserAccount customerAccount = new UserAccount(customerId, "customer@test.com", "hash", Role.CUSTOMER, true);

        Delivery delivery = Delivery.builder()
                .id(deliveryId)
                .customerId(customerId)
                .pickupAddress("Old Address")
                .dropoffAddress("B")
                .status(DeliveryStatus.PENDING)
                .deliveryFee(BigDecimal.TEN)
                .build();

        DeliveryUpdateRequest updateRequest = new DeliveryUpdateRequest(
                "New Address", null, null, null, null, null, null
        );

        when(deliveryRepository.findById(deliveryId)).thenReturn(Optional.of(delivery));
        when(userAccountService.findById(customerId)).thenReturn(Optional.of(customerAccount));
        when(deliveryRepository.save(any(Delivery.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DeliveryResponse response = deliveryService.updateDelivery(deliveryId, customerId, updateRequest);

        assertNotNull(response);
        assertEquals("New Address", response.pickupAddress());
    }

    @Test
    void testUpdateDeliveryThrowsForbiddenForNonOwner() {
        UUID ownerId = UUID.randomUUID();
        UUID attackerId = UUID.randomUUID();
        UUID deliveryId = UUID.randomUUID();
        UserAccount attackerAccount = new UserAccount(attackerId, "attacker@test.com", "hash", Role.CUSTOMER, true);

        Delivery delivery = Delivery.builder()
                .id(deliveryId)
                .customerId(ownerId)
                .pickupAddress("Old Address")
                .dropoffAddress("B")
                .status(DeliveryStatus.PENDING)
                .deliveryFee(BigDecimal.TEN)
                .build();

        DeliveryUpdateRequest updateRequest = new DeliveryUpdateRequest(
                "New Address", null, null, null, null, null, null
        );

        when(deliveryRepository.findById(deliveryId)).thenReturn(Optional.of(delivery));
        when(userAccountService.findById(attackerId)).thenReturn(Optional.of(attackerAccount));

        ApiException exception = assertThrows(ApiException.class, () ->
            deliveryService.updateDelivery(deliveryId, attackerId, updateRequest)
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
        verify(deliveryRepository, never()).save(any(Delivery.class));
    }

    @Test
    void testUpdateDeliveryStatusPopulatesTimestamps() {
        UUID deliveryId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();

        Delivery delivery = Delivery.builder()
                .id(deliveryId)
                .customerId(ownerId)
                .pickupAddress("Old Address")
                .dropoffAddress("B")
                .status(DeliveryStatus.PENDING)
                .deliveryFee(BigDecimal.TEN)
                .build();

        com.wafula.teza.delivery.api.dto.DeliveryStatusUpdateRequest request = 
                new com.wafula.teza.delivery.api.dto.DeliveryStatusUpdateRequest(DeliveryStatus.CANCELLED, "Customer cancelled");

        when(deliveryRepository.findById(deliveryId)).thenReturn(Optional.of(delivery));
        UserAccount ownerAccount = new UserAccount(ownerId, "customer@test.com", "hash", Role.CUSTOMER, true);
        when(userAccountService.findById(ownerId)).thenReturn(Optional.of(ownerAccount));
        when(deliveryRepository.save(any(Delivery.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DeliveryResponse response = deliveryService.updateDeliveryStatus(deliveryId, ownerId, false, request);

        assertNotNull(response);
        assertEquals(DeliveryStatus.CANCELLED, response.status());
        assertNotNull(response.cancelledAt());
        assertNull(response.acceptedAt());
        assertNull(response.pickedUpAt());
        assertNull(response.deliveredAt());
    }

    @Test
    void testFindMatchingRidersSuccess() {
        UUID deliveryId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        Delivery delivery = Delivery.builder()
                .id(deliveryId)
                .customerId(customerId)
                .pickupLatitude(-1.2833)
                .pickupLongitude(36.8167)
                .status(DeliveryStatus.PENDING)
                .build();

        UUID riderProfileId = UUID.randomUUID();
        RiderProfileResponse riderProfile = new RiderProfileResponse(
                riderProfileId,
                UUID.randomUUID(),
                com.wafula.teza.rider.domain.VehicleType.MOTORCYCLE,
                "PLATE123",
                true,
                com.wafula.teza.rider.domain.OnboardingStatus.APPROVED,
                Instant.now(),
                Instant.now()
        );

        com.wafula.teza.rider.api.dto.RiderLocationResponse riderLocation = new com.wafula.teza.rider.api.dto.RiderLocationResponse(
                riderProfileId,
                -1.2800,
                36.8100,
                Instant.now()
        );

        RankedRider rankedRider = new RankedRider(
                riderProfileId,
                1.2,
                0.85
        );

        UserAccount customerAccount = new UserAccount(customerId, "customer@test.com", "hash", Role.CUSTOMER, true);
        when(userAccountService.findById(customerId)).thenReturn(Optional.of(customerAccount));
        when(deliveryRepository.findById(deliveryId)).thenReturn(Optional.of(delivery));
        when(riderService.findAvailableRiders()).thenReturn(List.of(riderProfile));
        when(riderService.getLocationByProfileId(riderProfileId)).thenReturn(riderLocation);

        DeliveryRequest deliveryRequest = new DeliveryRequest(
                deliveryId, -1.2833, 36.8167
        );
        CandidateRider candidate = new CandidateRider(
                riderProfileId, -1.2800, 36.8100, true, true, 5.0
        );

        when(riderMatchingService.match(deliveryRequest, List.of(candidate))).thenReturn(List.of(rankedRider));

        List<RankedRider> result = deliveryService.findMatchingRiders(deliveryId, customerId, false);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(riderProfileId, result.get(0).riderId());
        assertEquals(1.2, result.get(0).distanceKm());
        assertEquals(0.85, result.get(0).score());
    }

    @Test
    void testFindMatchingRidersForbidden() {
        UUID deliveryId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();
        Delivery delivery = Delivery.builder()
                .id(deliveryId)
                .customerId(customerId)
                .pickupLatitude(-1.2833)
                .pickupLongitude(36.8167)
                .status(DeliveryStatus.PENDING)
                .build();

        UserAccount otherAccount = new UserAccount(otherUserId, "other@test.com", "hash", Role.CUSTOMER, true);
        when(userAccountService.findById(otherUserId)).thenReturn(Optional.of(otherAccount));
        when(deliveryRepository.findById(deliveryId)).thenReturn(Optional.of(delivery));

        assertThrows(ApiException.class, () -> {
            deliveryService.findMatchingRiders(deliveryId, otherUserId, false);
        });
    }

    @Test
    void testUpdateDeliveryStatusToSearchingTriggersFirstOffer() {
        UUID deliveryId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        Delivery delivery = Delivery.builder()
                .id(deliveryId)
                .customerId(customerId)
                .pickupLatitude(-1.2833)
                .pickupLongitude(36.8167)
                .status(DeliveryStatus.PENDING)
                .build();

        UUID riderProfileId = UUID.randomUUID();
        RiderProfileResponse riderProfile = new RiderProfileResponse(
                riderProfileId,
                UUID.randomUUID(),
                com.wafula.teza.rider.domain.VehicleType.MOTORCYCLE,
                "PLATE123",
                true,
                com.wafula.teza.rider.domain.OnboardingStatus.APPROVED,
                Instant.now(),
                Instant.now()
        );

        com.wafula.teza.rider.api.dto.RiderLocationResponse riderLocation = new com.wafula.teza.rider.api.dto.RiderLocationResponse(
                riderProfileId,
                -1.2800,
                36.8100,
                Instant.now()
        );

        RankedRider rankedRider = new RankedRider(riderProfileId, 1.2, 0.85);

        when(deliveryRepository.findById(deliveryId)).thenReturn(Optional.of(delivery));
        when(deliveryRepository.save(any(Delivery.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(deliveryOfferRepository.save(any(DeliveryOffer.class))).thenAnswer(invocation -> {
            DeliveryOffer savedOffer = invocation.getArgument(0);
            savedOffer.setId(UUID.randomUUID());
            return savedOffer;
        });

        when(riderService.findAvailableRiders()).thenReturn(List.of(riderProfile));
        when(riderService.getLocationByProfileId(riderProfileId)).thenReturn(riderLocation);
        when(riderService.getProfileById(riderProfileId, null, true)).thenReturn(riderProfile);

        DeliveryRequest deliveryRequest = new DeliveryRequest(deliveryId, -1.2833, 36.8167);
        CandidateRider candidate = new CandidateRider(riderProfileId, -1.2800, 36.8100, true, true, 5.0);
        when(riderMatchingService.match(deliveryRequest, List.of(candidate))).thenReturn(List.of(rankedRider));

        com.wafula.teza.delivery.api.dto.DeliveryStatusUpdateRequest request = 
                new com.wafula.teza.delivery.api.dto.DeliveryStatusUpdateRequest(DeliveryStatus.SEARCHING, "Start search");

        deliveryService.updateDeliveryStatus(deliveryId, customerId, true, request);

        // Verify offer was created and event published
        verify(deliveryOfferRepository).save(any(DeliveryOffer.class));
        verify(eventPublisher).publishEvent(any(DeliveryOfferCreatedEvent.class));
    }

    @Test
    void testDeclineOfferTriggersNextBestOffer() {
        UUID deliveryId = UUID.randomUUID();
        Delivery delivery = Delivery.builder()
                .id(deliveryId)
                .customerId(UUID.randomUUID())
                .pickupLatitude(-1.2833)
                .pickupLongitude(36.8167)
                .status(DeliveryStatus.SEARCHING)
                .build();

        UUID firstRiderProfileId = UUID.randomUUID();
        UUID secondRiderProfileId = UUID.randomUUID();

        DeliveryOffer activeOffer = DeliveryOffer.builder()
                .id(UUID.randomUUID())
                .delivery(delivery)
                .riderId(firstRiderProfileId)
                .status(OfferStatus.PENDING)
                .expiresAt(Instant.now().plusSeconds(60))
                .build();

        when(deliveryOfferRepository.findById(activeOffer.getId())).thenReturn(Optional.of(activeOffer));
        when(deliveryOfferRepository.save(any(DeliveryOffer.class))).thenAnswer(invocation -> {
            DeliveryOffer savedOffer = invocation.getArgument(0);
            if (savedOffer.getId() == null) {
                savedOffer.setId(UUID.randomUUID());
            }
            return savedOffer;
        });

        RiderProfileResponse firstRiderProfile = new RiderProfileResponse(
                firstRiderProfileId, UUID.randomUUID(), com.wafula.teza.rider.domain.VehicleType.MOTORCYCLE,
                "PLATE1", true, com.wafula.teza.rider.domain.OnboardingStatus.APPROVED, Instant.now(), Instant.now()
        );
        RiderProfileResponse secondRiderProfile = new RiderProfileResponse(
                secondRiderProfileId, UUID.randomUUID(), com.wafula.teza.rider.domain.VehicleType.MOTORCYCLE,
                "PLATE2", true, com.wafula.teza.rider.domain.OnboardingStatus.APPROVED, Instant.now(), Instant.now()
        );

        when(riderService.getProfileByUserId(firstRiderProfile.userId())).thenReturn(firstRiderProfile);

        // Failover matching setup: excluding firstRiderProfileId
        when(deliveryOfferRepository.findByDeliveryId(deliveryId)).thenReturn(List.of(activeOffer));
        when(riderService.findAvailableRiders()).thenReturn(List.of(firstRiderProfile, secondRiderProfile));

        // Let second rider have location, first rider excluded by offered filter
        com.wafula.teza.rider.api.dto.RiderLocationResponse secondLocation = new com.wafula.teza.rider.api.dto.RiderLocationResponse(
                secondRiderProfileId, -1.2900, 36.8200, Instant.now()
        );
        when(riderService.getLocationByProfileId(secondRiderProfileId)).thenReturn(secondLocation);
        when(riderService.getProfileById(secondRiderProfileId, null, true)).thenReturn(secondRiderProfile);

        DeliveryRequest deliveryRequest = new DeliveryRequest(deliveryId, -1.2833, 36.8167);
        CandidateRider candidate2 = new CandidateRider(secondRiderProfileId, -1.2900, 36.8200, true, true, 5.0);
        RankedRider rankedRider2 = new RankedRider(secondRiderProfileId, 2.0, 0.7);

        when(riderMatchingService.match(deliveryRequest, List.of(candidate2))).thenReturn(List.of(rankedRider2));

        OfferResponseRequest request = new OfferResponseRequest(false); // declined!
        deliveryService.respondToOffer(activeOffer.getId(), firstRiderProfile.userId(), request);

        // Verify decline status was saved and new offer created
        assertEquals(OfferStatus.DECLINED, activeOffer.getStatus());
        verify(deliveryOfferRepository, org.mockito.Mockito.times(2)).save(any(DeliveryOffer.class));
        verify(eventPublisher).publishEvent(any(DeliveryOfferCreatedEvent.class));
    }

    @Test
    void testUpdateDeliveryStatusUpdatesRiderAvailability() {
        UUID deliveryId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        UUID riderId = UUID.randomUUID();
        UUID riderUserId = UUID.randomUUID();

        Delivery delivery = Delivery.builder()
                .id(deliveryId)
                .customerId(customerId)
                .riderId(riderId)
                .pickupAddress("A")
                .dropoffAddress("B")
                .status(DeliveryStatus.IN_TRANSIT)
                .deliveryFee(BigDecimal.TEN)
                .build();

        com.wafula.teza.delivery.api.dto.DeliveryStatusUpdateRequest request =
                new com.wafula.teza.delivery.api.dto.DeliveryStatusUpdateRequest(DeliveryStatus.DELIVERED, "Delivered successfully");

        RiderProfileResponse riderProfile = new RiderProfileResponse(
                riderId,
                riderUserId,
                com.wafula.teza.rider.domain.VehicleType.MOTORCYCLE,
                "PLATE123",
                true,
                com.wafula.teza.rider.domain.OnboardingStatus.APPROVED,
                Instant.now(),
                Instant.now()
        );

        when(deliveryRepository.findById(deliveryId)).thenReturn(Optional.of(delivery));
        when(riderService.getProfileByUserId(riderUserId)).thenReturn(riderProfile);
        when(riderService.getProfileById(riderId, null, true)).thenReturn(riderProfile);
        when(deliveryRepository.save(any(Delivery.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DeliveryResponse response = deliveryService.updateDeliveryStatus(deliveryId, riderUserId, false, request);

        assertNotNull(response);
        assertEquals(DeliveryStatus.DELIVERED, response.status());
        verify(riderService).updateProfile(riderUserId, new com.wafula.teza.rider.api.dto.RiderProfileUpdateRequest(null, null, null, true));
    }
}
