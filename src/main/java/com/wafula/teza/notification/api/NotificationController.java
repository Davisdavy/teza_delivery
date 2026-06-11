package com.wafula.teza.notification.api;

import com.wafula.teza.notification.api.dto.NotificationResponse;
import com.wafula.teza.notification.application.NotificationService;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for managing a user's notifications.
 */
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public List<NotificationResponse> getNotifications(@AuthenticationPrincipal UUID currentUserId) {
        return notificationService.getNotificationsForUser(currentUserId).stream()
                .map(n -> new NotificationResponse(
                        n.getId(),
                        n.getUserId(),
                        n.getTitle(),
                        n.getMessage(),
                        n.getStatus(),
                        n.getCreatedAt()
                ))
                .toList();
    }

    @PutMapping("/{id}/read")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markAsRead(
            @PathVariable UUID id,
            @AuthenticationPrincipal UUID currentUserId) {
        notificationService.markAsRead(id, currentUserId);
    }

    @PutMapping("/read-all")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markAllAsRead(@AuthenticationPrincipal UUID currentUserId) {
        notificationService.markAllAsRead(currentUserId);
    }
}
