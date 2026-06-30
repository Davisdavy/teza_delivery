package com.wafula.teza.pricing.api;

import com.wafula.teza.pricing.api.dto.EstimateRequest;
import com.wafula.teza.pricing.api.dto.PricingConfigurationResponse;
import com.wafula.teza.pricing.api.dto.PricingConfigurationUpdateRequest;
import com.wafula.teza.pricing.api.dto.PricingEstimateResponse;
import com.wafula.teza.pricing.application.PricingService;
import com.wafula.teza.pricing.application.PricingService.PricingConfigurationUpdateParameters;
import com.wafula.teza.pricing.domain.Location;
import com.wafula.teza.pricing.domain.PricingBreakdown;
import com.wafula.teza.pricing.domain.PricingConfiguration;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pricing")
public class PricingController {

    private final PricingService pricingService;

    public PricingController(PricingService pricingService) {
        this.pricingService = pricingService;
    }

    @PostMapping("/estimate")
    public PricingEstimateResponse estimateFee(@Valid @RequestBody EstimateRequest request) {
        Location pickup = new Location(request.pickupLatitude(), request.pickupLongitude());
        Location dropoff = new Location(request.dropoffLatitude(), request.dropoffLongitude());
        PricingBreakdown breakdown = pricingService.calculateFee(pickup, dropoff);

        return new PricingEstimateResponse(
                breakdown.baseFee(),
                breakdown.distanceFee(),
                breakdown.timeFee(),
                breakdown.multiplier(),
                breakdown.finalFee()
        );
    }

    @GetMapping("/config")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SUPPORT_ADMIN')")
    public PricingConfigurationResponse getActiveConfiguration() {
        PricingConfiguration config = pricingService.getActiveConfiguration();
        return mapToResponse(config);
    }

    @PutMapping("/config")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public PricingConfigurationResponse updateConfiguration(
            @AuthenticationPrincipal UUID currentUserId,
            @Valid @RequestBody PricingConfigurationUpdateRequest request) {
        
        PricingConfigurationUpdateParameters params = new PricingConfigurationUpdateParameters(
                request.baseFee(),
                request.pricePerKilometer(),
                request.pricePerMinute(),
                request.minimumDeliveryFee(),
                request.maximumDeliveryFee(),
                request.surgeEnabled(),
                request.peakHourMultiplier(),
                request.weekendMultiplier(),
                request.nightMultiplier()
        );

        PricingConfiguration updated = pricingService.updateConfiguration(currentUserId, params);
        return mapToResponse(updated);
    }

    private PricingConfigurationResponse mapToResponse(PricingConfiguration config) {
        return new PricingConfigurationResponse(
                config.getId(),
                config.getBaseFee(),
                config.getPricePerKilometer(),
                config.getPricePerMinute(),
                config.getMinimumDeliveryFee(),
                config.getMaximumDeliveryFee(),
                config.isSurgeEnabled(),
                config.getPeakHourMultiplier(),
                config.getWeekendMultiplier(),
                config.getNightMultiplier(),
                config.getUpdatedBy(),
                config.getUpdatedAt()
        );
    }
}
