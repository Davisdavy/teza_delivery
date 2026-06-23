package com.wafula.teza.notification.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data Repository for Managing Device Push Notification Tokens.
 */
@Repository
public interface NotificationTokenRepository extends JpaRepository<NotificationToken, UUID> {
    List<NotificationToken> findByUserId(UUID userId);
    List<NotificationToken> findByUserIdAndActive(UUID userId, boolean active);
    Optional<NotificationToken> findByToken(String token);
    void deleteByToken(String token);
}
