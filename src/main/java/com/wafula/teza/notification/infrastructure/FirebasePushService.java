package com.wafula.teza.notification.infrastructure;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.AndroidNotification;
import com.google.firebase.messaging.ApnsConfig;
import com.google.firebase.messaging.Aps;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.Notification;
import com.wafula.teza.notification.domain.NotificationTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * Service to interface with Firebase Cloud Messaging to send push notifications.
 */
@Service
public class FirebasePushService {
    private static final Logger log = LoggerFactory.getLogger(FirebasePushService.class);

    private final NotificationTokenRepository tokenRepository;

    public FirebasePushService(NotificationTokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    /**
     * Sends a push notification to a device token with specific payload data and custom sound parameters.
     */
    @Transactional
    public void sendPushNotification(String targetToken, String title, String body, Map<String, String> data) {
        if (FirebaseApp.getApps().isEmpty()) {
            log.info("[FCM SIMULATION] Would send push to token {}: Title: \"{}\", Body: \"{}\", Data: {}", 
                    targetToken, title, body, data);
            return;
        }

        try {
            Message.Builder messageBuilder = Message.builder()
                    .setToken(targetToken)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .setAndroidConfig(AndroidConfig.builder()
                            .setNotification(AndroidNotification.builder()
                                    .setSound("offer_alert_bell")
                                    .setChannelId("offer_channel")
                                    .build())
                            .build())
                    .setApnsConfig(ApnsConfig.builder()
                            .setAps(Aps.builder()
                                    .setSound("offer_alert_bell.mp3")
                                    .build())
                            .build());

            if (data != null && !data.isEmpty()) {
                messageBuilder.putAllData(data);
            }

            if (data == null || !data.containsKey("click_action")) {
                messageBuilder.putData("click_action", "FLUTTER_NOTIFICATION_CLICK");
            }

            String response = FirebaseMessaging.getInstance().send(messageBuilder.build());
            log.info("Successfully sent FCM message: {}", response);
        } catch (FirebaseMessagingException e) {
            log.error("Failed to send FCM message to token {}: Code: {}, Message: {}", 
                    targetToken, e.getMessagingErrorCode(), e.getMessage());
            
            if (e.getMessagingErrorCode() == MessagingErrorCode.UNREGISTERED || 
                e.getMessagingErrorCode() == MessagingErrorCode.INVALID_ARGUMENT) {
                log.info("Automatically removing stale/invalid FCM token from DB: {}", targetToken);
                tokenRepository.deleteByToken(targetToken);
            }
        } catch (Exception e) {
            log.error("Unexpected error sending FCM message to token {}: {}", targetToken, e.getMessage(), e);
        }
    }
}
