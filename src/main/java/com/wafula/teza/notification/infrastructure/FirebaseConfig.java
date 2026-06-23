package com.wafula.teza.notification.infrastructure;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.auth.oauth2.GoogleCredentials;
import java.io.InputStream;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

/**
 * Configuration class to initialize the Firebase Admin SDK on application startup.
 */
@Configuration
public class FirebaseConfig {
    private static final Logger log = LoggerFactory.getLogger(FirebaseConfig.class);

    private final ResourceLoader resourceLoader;

    @Value("${teza.firebase.credentials-path:classpath:teza-8af25-firebase-adminsdk-fbsvc-08ea38fd48.json}")
    private String credentialsPath;

    public FirebaseConfig(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @PostConstruct
    public void initializeFirebase() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseOptions options = null;
                if (credentialsPath != null && !credentialsPath.isEmpty()) {
                    Resource resource = resourceLoader.getResource(credentialsPath);
                    if (resource.exists()) {
                        try (InputStream is = resource.getInputStream()) {
                            options = FirebaseOptions.builder()
                                    .setCredentials(GoogleCredentials.fromStream(is))
                                    .build();
                            FirebaseApp.initializeApp(options);
                            log.info("Firebase Application initialized successfully from credentials path: {}", credentialsPath);
                        }
                    } else {
                        log.warn("Firebase credentials resource not found at: {}", credentialsPath);
                    }
                }

                if (options == null) {
                    try {
                        options = FirebaseOptions.builder()
                                .setCredentials(GoogleCredentials.getApplicationDefault())
                                .build();
                        FirebaseApp.initializeApp(options);
                        log.info("Firebase Application initialized successfully using Google Application Default Credentials.");
                    } catch (Exception e) {
                        log.warn("Firebase credentials could not be loaded. Firebase Push Notifications will run in SIMULATION mode. Message: {}", e.getMessage());
                    }
                }
            } else {
                log.info("Firebase App already initialized.");
            }
        } catch (Exception e) {
            log.error("Failed to initialize Firebase: {}", e.getMessage(), e);
        }
    }
}
