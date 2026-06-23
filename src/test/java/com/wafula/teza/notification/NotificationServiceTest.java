package com.wafula.teza.notification;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.wafula.teza.delivery.domain.DeliveryStatus;
import com.wafula.teza.merchant.api.dto.MerchantResponse;
import com.wafula.teza.merchant.application.MerchantService;
import com.wafula.teza.notification.application.NotificationEventListener;
import com.wafula.teza.notification.application.NotificationService;
import com.wafula.teza.notification.application.NotificationServiceImpl;
import com.wafula.teza.notification.domain.Notification;
import com.wafula.teza.notification.domain.NotificationRepository;
import com.wafula.teza.notification.domain.NotificationStatus;
import com.wafula.teza.notification.domain.NotificationToken;
import com.wafula.teza.notification.domain.NotificationTokenRepository;
import com.wafula.teza.notification.infrastructure.FirebasePushService;
import com.wafula.teza.rider.api.dto.RiderProfileResponse;
import com.wafula.teza.rider.application.RiderService;
import com.wafula.teza.shared.event.DeliveryOfferCreatedEvent;
import com.wafula.teza.shared.event.DeliveryStatusChangedEvent;
import com.wafula.teza.shared.exception.ApiException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationTokenRepository notificationTokenRepository;

    @Mock
    private FirebasePushService firebasePushService;

    @Mock
    private MerchantService merchantService;

    @Mock
    private RiderService riderService;

    private NotificationService notificationService;
    private NotificationEventListener eventListener;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationServiceImpl(
                notificationRepository,
                notificationTokenRepository,
                firebasePushService
        );
        eventListener = new NotificationEventListener(notificationService, merchantService, riderService);
    }

    @Test
    void testCreateNotification() {
        UUID userId = UUID.randomUUID();
        String title = "Test Title";
        String message = "Test Message";

        Notification notification = Notification.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .title(title)
                .message(message)
                .status(NotificationStatus.UNREAD)
                .createdAt(Instant.now())
                .build();

        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        Notification result = notificationService.createNotification(userId, title, message);

        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals(title, result.getTitle());
        assertEquals(message, result.getMessage());
        assertEquals(NotificationStatus.UNREAD, result.getStatus());
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void testMarkAsReadSuccess() {
        UUID notificationId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Notification notification = Notification.builder()
                .id(notificationId)
                .userId(userId)
                .title("T")
                .message("M")
                .status(NotificationStatus.UNREAD)
                .build();

        when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

        notificationService.markAsRead(notificationId, userId);

        assertEquals(NotificationStatus.READ, notification.getStatus());
        verify(notificationRepository).save(notification);
    }

    @Test
    void testMarkAsReadThrowsForbiddenForNonOwner() {
        UUID notificationId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        UUID attackerId = UUID.randomUUID();

        Notification notification = Notification.builder()
                .id(notificationId)
                .userId(ownerId)
                .title("T")
                .message("M")
                .status(NotificationStatus.UNREAD)
                .build();

        when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(notification));

        ApiException exception = assertThrows(ApiException.class, () ->
            notificationService.markAsRead(notificationId, attackerId)
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    void testOnDeliveryStatusChangedCustomer() {
        UUID customerId = UUID.randomUUID();
        UUID deliveryId = UUID.randomUUID();

        DeliveryStatusChangedEvent event = new DeliveryStatusChangedEvent(
                deliveryId,
                DeliveryStatus.PENDING,
                DeliveryStatus.SEARCHING,
                customerId,
                null,
                null,
                "Pickup 1",
                "Dropoff 1"
        );

        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

        eventListener.onDeliveryStatusChanged(event);

        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void testOnDeliveryStatusChangedMerchant() {
        UUID merchantId = UUID.randomUUID();
        UUID merchantUserId = UUID.randomUUID();
        UUID deliveryId = UUID.randomUUID();

        DeliveryStatusChangedEvent event = new DeliveryStatusChangedEvent(
                deliveryId,
                DeliveryStatus.PENDING,
                DeliveryStatus.SEARCHING,
                null,
                merchantId,
                null,
                "Pickup 1",
                "Dropoff 1"
        );

        MerchantResponse merchantResponse = new MerchantResponse(
                merchantId, merchantUserId, "Merchant Biz", "123", "St", Instant.now(), Instant.now()
        );

        when(merchantService.getProfileById(merchantId, null, true)).thenReturn(merchantResponse);
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

        eventListener.onDeliveryStatusChanged(event);

        verify(merchantService).getProfileById(merchantId, null, true);
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void testOnDeliveryStatusChangedNotifiesAssignedRider() {
        UUID customerId = UUID.randomUUID();
        UUID riderId = UUID.randomUUID();
        UUID riderUserId = UUID.randomUUID();
        UUID deliveryId = UUID.randomUUID();

        DeliveryStatusChangedEvent event = new DeliveryStatusChangedEvent(
                deliveryId,
                DeliveryStatus.SEARCHING,
                DeliveryStatus.ASSIGNED,
                customerId,
                null,
                riderId,
                "Pickup 1",
                "Dropoff 1"
        );

        RiderProfileResponse riderResponse = new RiderProfileResponse(
                riderId, riderUserId, null, null, true, null, Instant.now(), Instant.now(), 0L
        );

        when(riderService.getProfileById(riderId, null, true)).thenReturn(riderResponse);
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

        eventListener.onDeliveryStatusChanged(event);

        // Should notify both the owner customer and the assigned rider (2 times)
        verify(notificationRepository, times(2)).save(any(Notification.class));
        verify(riderService).getProfileById(riderId, null, true);
    }

    @Test
    void testOnDeliveryOfferCreated() {
        UUID riderUserId = UUID.randomUUID();
        UUID offerId = UUID.randomUUID();
        UUID deliveryId = UUID.randomUUID();

        DeliveryOfferCreatedEvent event = new DeliveryOfferCreatedEvent(
                offerId,
                deliveryId,
                UUID.randomUUID(),
                riderUserId,
                30
        );

        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

        eventListener.onDeliveryOfferCreated(event);

        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void testRegisterDeviceTokenNew() {
        UUID userId = UUID.randomUUID();
        String token = "fcm-token-123";
        String deviceId = "device-id-123";
        String deviceType = "android";
        String appVersion = "1.0.0";

        when(notificationTokenRepository.findByToken(token)).thenReturn(Optional.empty());

        notificationService.registerDeviceToken(userId, token, deviceId, deviceType, appVersion);

        verify(notificationTokenRepository).save(any(NotificationToken.class));
    }

    @Test
    void testRegisterDeviceTokenExisting() {
        UUID userId = UUID.randomUUID();
        String token = "fcm-token-123";
        String deviceId = "device-id-123";
        String deviceType = "android";
        String appVersion = "1.0.0";

        NotificationToken existingToken = NotificationToken.builder()
                .userId(UUID.randomUUID())
                .token(token)
                .build();

        when(notificationTokenRepository.findByToken(token)).thenReturn(Optional.of(existingToken));

        notificationService.registerDeviceToken(userId, token, deviceId, deviceType, appVersion);

        verify(notificationTokenRepository).save(existingToken);
        assertEquals(userId, existingToken.getUserId());
        assertEquals(deviceId, existingToken.getDeviceId());
    }

    @Test
    void testUnregisterDeviceToken() {
        UUID userId = UUID.randomUUID();
        String token = "fcm-token-123";

        NotificationToken existingToken = NotificationToken.builder()
                .userId(userId)
                .token(token)
                .active(true)
                .build();

        when(notificationTokenRepository.findByToken(token)).thenReturn(Optional.of(existingToken));

        notificationService.unregisterDeviceToken(userId, token);

        verify(notificationTokenRepository).save(existingToken);
        assertEquals(false, existingToken.isActive());
    }
}
