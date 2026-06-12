package com.wafula.teza.delivery.api;

import com.wafula.teza.delivery.api.dto.DeliveryCreateRequest;
import com.wafula.teza.delivery.api.dto.DeliveryOfferResponse;
import com.wafula.teza.delivery.api.dto.DeliveryResponse;
import com.wafula.teza.delivery.api.dto.DeliveryStatusHistoryResponse;
import com.wafula.teza.delivery.api.dto.DeliveryStatusUpdateRequest;
import com.wafula.teza.delivery.api.dto.DeliveryUpdateRequest;
import com.wafula.teza.delivery.api.dto.OfferCreateRequest;
import com.wafula.teza.delivery.api.dto.OfferResponseRequest;
import com.wafula.teza.delivery.application.DeliveryService;
import com.wafula.teza.dispatch.domain.RankedRider;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for managing deliveries, status updates, status history logs,
 * and matching offers.
 */
@RestController
@RequestMapping("/api/delivery")
public class DeliveryController {

    private final DeliveryService deliveryService;

    public DeliveryController(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DeliveryResponse createDelivery(
            @AuthenticationPrincipal UUID currentUserId,
            @Valid @RequestBody DeliveryCreateRequest request) {
        return deliveryService.createDelivery(currentUserId, request);
    }

    @GetMapping("/{id}")
    public DeliveryResponse getDeliveryById(
            @PathVariable UUID id,
            @AuthenticationPrincipal UUID currentUserId,
            Authentication authentication) {
        boolean isAdmin = hasAdminRole(authentication);
        return deliveryService.getDeliveryById(id, currentUserId, isAdmin);
    }

    @GetMapping("/merchant")
    public List<DeliveryResponse> getDeliveriesForMerchant(@AuthenticationPrincipal UUID currentUserId) {
        return deliveryService.getDeliveriesForMerchant(currentUserId);
    }

    @GetMapping("/customer")
    public List<DeliveryResponse> getDeliveriesForCustomer(@AuthenticationPrincipal UUID currentUserId) {
        return deliveryService.getDeliveriesForCustomer(currentUserId);
    }

    @GetMapping("/rider")
    public List<DeliveryResponse> getDeliveriesForRider(@AuthenticationPrincipal UUID currentUserId) {
        return deliveryService.getDeliveriesForRider(currentUserId);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SUPPORT_ADMIN')")
    public List<DeliveryResponse> getAllDeliveries() {
        return deliveryService.getAllDeliveries();
    }

    @PutMapping("/{id}")
    public DeliveryResponse updateDelivery(
            @PathVariable UUID id,
            @AuthenticationPrincipal UUID currentUserId,
            @Valid @RequestBody DeliveryUpdateRequest request) {
        return deliveryService.updateDelivery(id, currentUserId, request);
    }

    @PutMapping("/{id}/status")
    public DeliveryResponse updateDeliveryStatus(
            @PathVariable UUID id,
            @AuthenticationPrincipal UUID currentUserId,
            Authentication authentication,
            @Valid @RequestBody DeliveryStatusUpdateRequest request) {
        boolean isAdmin = hasAdminRole(authentication);
        return deliveryService.updateDeliveryStatus(id, currentUserId, isAdmin, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDelivery(
            @PathVariable UUID id,
            @AuthenticationPrincipal UUID currentUserId,
            Authentication authentication) {
        boolean isAdmin = hasSuperAdminRole(authentication);
        deliveryService.deleteDelivery(id, currentUserId, isAdmin);
    }

    @PostMapping("/{id}/offers")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SUPPORT_ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public DeliveryOfferResponse createOffer(
            @PathVariable UUID id,
            @Valid @RequestBody OfferCreateRequest request) {
        return deliveryService.createOffer(id, request);
    }

    @PutMapping("/offers/{offerId}/respond")
    public DeliveryOfferResponse respondToOffer(
            @PathVariable UUID offerId,
            @AuthenticationPrincipal UUID currentUserId,
            @Valid @RequestBody OfferResponseRequest request) {
        return deliveryService.respondToOffer(offerId, currentUserId, request);
    }

    @GetMapping("/{id}/offers")
    public List<DeliveryOfferResponse> getOffersForDelivery(
            @PathVariable UUID id,
            @AuthenticationPrincipal UUID currentUserId,
            Authentication authentication) {
        boolean isAdmin = hasAdminRole(authentication);
        return deliveryService.getOffersForDelivery(id, currentUserId, isAdmin);
    }

    @GetMapping("/{id}/history")
    public List<DeliveryStatusHistoryResponse> getStatusHistory(
            @PathVariable UUID id,
            @AuthenticationPrincipal UUID currentUserId,
            Authentication authentication) {
        boolean isAdmin = hasAdminRole(authentication);
        return deliveryService.getStatusHistory(id, currentUserId, isAdmin);
    }

    @GetMapping("/{id}/matching-riders")
    public List<RankedRider> getMatchingRiders(
            @PathVariable UUID id,
            @AuthenticationPrincipal UUID currentUserId,
            Authentication authentication) {
        boolean isAdmin = hasAdminRole(authentication);
        return deliveryService.findMatchingRiders(id, currentUserId, isAdmin);
    }

    private boolean hasAdminRole(Authentication authentication) {
        if (authentication == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority ->
                    grantedAuthority.getAuthority().equals("ROLE_SUPER_ADMIN") ||
                    grantedAuthority.getAuthority().equals("ROLE_SUPPORT_ADMIN"));
    }

    private boolean hasSuperAdminRole(Authentication authentication) {
        if (authentication == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_SUPER_ADMIN"));
    }
}
