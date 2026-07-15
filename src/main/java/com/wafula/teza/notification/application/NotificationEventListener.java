package com.wafula.teza.notification.application;

import com.wafula.teza.delivery.domain.DeliveryStatus;
import com.wafula.teza.merchant.api.dto.MerchantResponse;
import com.wafula.teza.merchant.application.MerchantService;
import com.wafula.teza.rider.api.dto.RiderProfileResponse;
import com.wafula.teza.rider.application.RiderService;
import com.wafula.teza.shared.event.DeliveryOfferCreatedEvent;
import com.wafula.teza.shared.event.DeliveryStatusChangedEvent;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Event consumer listening to domain events and creating outbound notifications.
 */
@Component
public class NotificationEventListener {

    private static final Logger log = LoggerFactory.getLogger(NotificationEventListener.class);

    private final NotificationService notificationService;
    private final MerchantService merchantService;
    private final RiderService riderService;

    public NotificationEventListener(
            NotificationService notificationService,
            MerchantService merchantService,
            RiderService riderService) {
        this.notificationService = notificationService;
        this.merchantService = merchantService;
        this.riderService = riderService;
    }

    @EventListener
    public void onDeliveryStatusChanged(DeliveryStatusChangedEvent event) {
        UUID ownerUserId = resolveOwnerUserId(event);
        if (ownerUserId == null) {
            log.warn("Could not resolve owner userId for delivery status changed event: {}", event.deliveryId());
            return;
        }

        String title = null;
        String message = null;

        DeliveryStatus status = event.newStatus();
        switch (status) {
            case SEARCHING -> {
                title = "Searching for Riders";
                message = "We are matching your delivery request at " + event.pickupAddress() + " to nearby riders.";
            }
            case ASSIGNED -> {
                title = "Rider Assigned";
                message = "A rider has accepted your delivery order from " + event.pickupAddress() + " to " + event.dropoffAddress() + ".";
            }
            case ARRIVED -> {
                title = "Rider Arrived";
                message = "The rider has arrived at the pickup location: " + event.pickupAddress() + ".";
            }
            case PICKED_UP -> {
                title = "Delivery Picked Up";
                message = "Your package is now in transit from " + event.pickupAddress() + " to " + event.dropoffAddress() + ".";
            }
            case IN_TRANSIT -> {
                // Resolve merchant name for personalised OTP SMS
                String merchantName = "the merchant";
                if (event.merchantId() != null) {
                    try {
                        MerchantResponse merchant = merchantService.getProfileById(event.merchantId(), null, true);
                        merchantName = merchant.businessName();
                    } catch (Exception ex) {
                        log.warn("Could not resolve merchant name for OTP SMS, delivery {}", event.deliveryId());
                    }
                }
                String otp = event.verificationOtp() != null ? event.verificationOtp() : "------";
                title = "Delivery Verification Code";
                message = "Thank you for ordering from " + merchantName + ".\n\n"
                        + "Your delivery is on the way.\n\n"
                        + "Please provide this code to the rider upon arrival.\n\n"
                        + "Verification Code:\n" + otp + "\n\n"
                        + "This code confirms successful delivery.\n"
                        + "Do not share it before receiving your order.\n\n"
                        + "- Teza Logistics";
                log.info("[OTP SMS] Delivery {} → Code {} → Customer {}", event.deliveryId(), otp, ownerUserId);
            }
            case DELIVERED -> {
                title = "Delivered Successfully";
                message = "Your delivery to " + event.dropoffAddress() + " has been completed. Thank you!";
            }
            case CANCELLED -> {
                title = "Delivery Cancelled";
                message = "Your delivery order has been cancelled.";
            }
            default -> {
                // Draft or Pending, no direct notification necessary
            }
        }

        if (title != null && message != null) {
            notificationService.createNotification(ownerUserId, title, message);
        }

        // Notify assigned rider on relevant status changes
        if (event.riderId() != null) {
            try {
                RiderProfileResponse rider = riderService.getProfileById(event.riderId(), null, true);
                UUID riderUserId = rider.userId();

                if (status == DeliveryStatus.CANCELLED) {
                    notificationService.createNotification(
                            riderUserId,
                            "Delivery Cancelled",
                            "The delivery order you were assigned to has been cancelled."
                    );
                } else if (status == DeliveryStatus.ASSIGNED) {
                    notificationService.createNotification(
                            riderUserId,
                            "Delivery Assigned",
                            "You have been successfully assigned to a delivery at " + event.pickupAddress() + "."
                    );
                } else if (status == DeliveryStatus.IN_TRANSIT) {
                    notificationService.createNotification(
                            riderUserId,
                            "OTP Sent to Customer",
                            "A verification code has been sent to the customer. Ask for it upon arrival."
                    );
                }
            } catch (Exception ex) {
                log.error("Failed to notify rider for delivery status changed event: {}", event.deliveryId(), ex);
            }
        }
    }


    @EventListener
    public void onDeliveryOfferCreated(DeliveryOfferCreatedEvent event) {
        String title = "New Delivery Offer Available";
        String message = "You have received a new delivery offer. You have " + event.durationSeconds() + " seconds to respond.";
        
        java.util.Map<String, String> extraData = new java.util.HashMap<>();
        extraData.put("type", "OFFER");
        extraData.put("offerId", event.offerId().toString());
        extraData.put("deliveryId", event.deliveryId().toString());
        
        notificationService.createNotification(event.riderUserId(), title, message, extraData);
    }

    private UUID resolveOwnerUserId(DeliveryStatusChangedEvent event) {
        if (event.customerId() != null) {
            return event.customerId();
        }
        if (event.merchantId() != null) {
            try {
                MerchantResponse merchant = merchantService.getProfileById(event.merchantId(), null, true);
                return merchant.userId();
            } catch (Exception ex) {
                log.error("Failed to resolve merchant userId for merchantId: {}", event.merchantId(), ex);
            }
        }
        return null;
    }
}
