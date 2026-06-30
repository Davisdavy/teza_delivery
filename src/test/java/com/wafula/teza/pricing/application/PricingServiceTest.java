package com.wafula.teza.pricing.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.wafula.teza.pricing.domain.DistanceResult;
import com.wafula.teza.pricing.domain.Location;
import com.wafula.teza.pricing.domain.PricingBreakdown;
import com.wafula.teza.pricing.domain.PricingConfiguration;
import com.wafula.teza.pricing.domain.PricingConfigurationRepository;
import com.wafula.teza.shared.exception.ApiException;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class PricingServiceTest {

    @Mock
    private PricingConfigurationRepository repository;

    @Mock
    private DistanceProvider distanceProvider;

    private PricingService pricingService;

    private PricingConfiguration config;

    @BeforeEach
    void setUp() {
        config = PricingConfiguration.builder()
                .id(UUID.randomUUID())
                .baseFee(BigDecimal.valueOf(50.00))
                .pricePerKilometer(BigDecimal.valueOf(30.00))
                .pricePerMinute(BigDecimal.valueOf(2.00))
                .minimumDeliveryFee(BigDecimal.valueOf(100.00))
                .maximumDeliveryFee(BigDecimal.valueOf(1000.00))
                .surgeEnabled(true)
                .peakHourMultiplier(BigDecimal.valueOf(1.50))
                .weekendMultiplier(BigDecimal.valueOf(1.20))
                .nightMultiplier(BigDecimal.valueOf(1.30))
                .updatedAt(Instant.now())
                .build();
    }

    private void initializeServiceWithTime(String isoInstant) {
        Clock fixedClock = Clock.fixed(Instant.parse(isoInstant), ZoneId.of("Africa/Nairobi"));
        pricingService = new PricingServiceImpl(repository, distanceProvider, fixedClock);
    }

    @Test
    void testCalculateFeeWeekdayOffPeak() {
        // Tuesday at 11:00 AM Nairobi Time
        initializeServiceWithTime("2026-06-30T08:00:00Z"); // 08:00 UTC = 11:00 AM Nairobi
        
        Location pickup = new Location(-1.2833, 36.8167);
        Location dropoff = new Location(-1.2933, 36.8267);

        when(repository.findFirstByOrderByUpdatedAtDesc()).thenReturn(Optional.of(config));
        when(distanceProvider.calculate(pickup, dropoff)).thenReturn(new DistanceResult(2.5, 10.0));

        PricingBreakdown result = pricingService.calculateFee(pickup, dropoff);

        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(50).setScale(0), result.baseFee());
        // 2.5 km * 30.00 = 75.00
        assertEquals(BigDecimal.valueOf(75).setScale(0), result.distanceFee());
        // 10.0 mins * 2.00 = 20.00
        assertEquals(BigDecimal.valueOf(20).setScale(0), result.timeFee());
        // No multipliers apply
        assertEquals(BigDecimal.valueOf(1.00).setScale(2), result.multiplier());
        // 50 + 75 + 20 = 145 (rounded to nearest multiple of 10 = 150)
        assertEquals(BigDecimal.valueOf(150).setScale(0), result.finalFee());
    }

    @Test
    void testCalculateFeeWeekdayPeakHour() {
        // Tuesday at 8:00 AM Nairobi Time (Peak Hour is 7-9 AM)
        initializeServiceWithTime("2026-06-30T05:00:00Z"); // 05:00 UTC = 08:00 AM Nairobi

        Location pickup = new Location(-1.2833, 36.8167);
        Location dropoff = new Location(-1.2933, 36.8267);

        when(repository.findFirstByOrderByUpdatedAtDesc()).thenReturn(Optional.of(config));
        when(distanceProvider.calculate(pickup, dropoff)).thenReturn(new DistanceResult(2.5, 10.0));

        PricingBreakdown result = pricingService.calculateFee(pickup, dropoff);

        assertNotNull(result);
        // 145.00 * 1.50 = 217.50 (rounded to nearest multiple of 10 = 220)
        assertEquals(BigDecimal.valueOf(1.50).setScale(2), result.multiplier());
        assertEquals(BigDecimal.valueOf(220).setScale(0), result.finalFee());
    }

    @Test
    void testCalculateFeeEnforcesMinimumFee() {
        // Tuesday at 11:00 AM Nairobi Time
        initializeServiceWithTime("2026-06-30T08:00:00Z"); 

        Location pickup = new Location(-1.2833, 36.8167);
        Location dropoff = new Location(-1.2843, 36.8177); // Very short distance

        when(repository.findFirstByOrderByUpdatedAtDesc()).thenReturn(Optional.of(config));
        when(distanceProvider.calculate(pickup, dropoff)).thenReturn(new DistanceResult(0.2, 1.0));

        PricingBreakdown result = pricingService.calculateFee(pickup, dropoff);

        assertNotNull(result);
        // 50 + (0.2 * 30) + (1 * 2) = 50 + 6 + 2 = 58.00
        // Config minimum is 100.00
        assertEquals(BigDecimal.valueOf(100).setScale(0), result.finalFee());
    }

    @Test
    void testCalculateFeeEnforcesMaximumFee() {
        // Tuesday at 11:00 AM Nairobi Time
        initializeServiceWithTime("2026-06-30T08:00:00Z"); 

        Location pickup = new Location(-1.2833, 36.8167);
        Location dropoff = new Location(-1.2843, 36.8177);

        when(repository.findFirstByOrderByUpdatedAtDesc()).thenReturn(Optional.of(config));
        // Large distance
        when(distanceProvider.calculate(pickup, dropoff)).thenReturn(new DistanceResult(50.0, 100.0));

        PricingBreakdown result = pricingService.calculateFee(pickup, dropoff);

        assertNotNull(result);
        // 50 + (50 * 30) + (100 * 2) = 50 + 1500 + 200 = 1750.00
        // Config maximum is 1000.00
        assertEquals(BigDecimal.valueOf(1000).setScale(0), result.finalFee());
    }

    @Test
    void testGetActiveConfigurationThrowsWhenNotFound() {
        initializeServiceWithTime("2026-06-30T08:00:00Z");
        when(repository.findFirstByOrderByUpdatedAtDesc()).thenReturn(Optional.empty());

        ApiException exception = assertThrows(ApiException.class, () -> pricingService.getActiveConfiguration());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatus());
        assertEquals("No active pricing configuration found.", exception.getMessage());
    }

    @Test
    void testUpdateConfiguration() {
        initializeServiceWithTime("2026-06-30T08:00:00Z");
        
        PricingService.PricingConfigurationUpdateParameters params = new PricingService.PricingConfigurationUpdateParameters(
                BigDecimal.valueOf(60.00),
                BigDecimal.valueOf(35.00),
                BigDecimal.valueOf(2.50),
                BigDecimal.valueOf(120.00),
                BigDecimal.valueOf(1200.00),
                true,
                BigDecimal.valueOf(1.60),
                BigDecimal.valueOf(1.25),
                BigDecimal.valueOf(1.35)
        );

        UUID adminId = UUID.randomUUID();
        when(repository.save(any(PricingConfiguration.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PricingConfiguration updated = pricingService.updateConfiguration(adminId, params);

        assertNotNull(updated);
        assertEquals(BigDecimal.valueOf(60.00), updated.getBaseFee());
        assertEquals(BigDecimal.valueOf(35.00), updated.getPricePerKilometer());
        assertEquals(adminId, updated.getUpdatedBy());
    }
}
