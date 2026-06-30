package com.wafula.teza.pricing.application;

import com.wafula.teza.pricing.domain.DistanceResult;
import com.wafula.teza.pricing.domain.Location;
import com.wafula.teza.pricing.domain.PricingBreakdown;
import com.wafula.teza.pricing.domain.PricingConfiguration;
import com.wafula.teza.pricing.domain.PricingConfigurationRepository;
import com.wafula.teza.shared.exception.ApiException;
import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PricingServiceImpl implements PricingService {

    private static final Logger log = LoggerFactory.getLogger(PricingServiceImpl.class);
    private static final ZoneId DEFAULT_ZONE = ZoneId.of("Africa/Nairobi");

    private final PricingConfigurationRepository repository;
    private final DistanceProvider distanceProvider;
    private final Clock clock;

    public PricingServiceImpl(
            PricingConfigurationRepository repository,
            DistanceProvider distanceProvider,
            @Autowired(required = false) Clock clock) {
        this.repository = repository;
        this.distanceProvider = distanceProvider;
        this.clock = clock != null ? clock : Clock.system(DEFAULT_ZONE);
    }

    @PostConstruct
    @Transactional
    public void initDefaultConfig() {
        if (repository.count() == 0) {
            log.info("No pricing configurations found. Seeding default pricing configuration.");
            PricingConfiguration defaultConfig = PricingConfiguration.builder()
                    .baseFee(BigDecimal.valueOf(50.00))
                    .pricePerKilometer(BigDecimal.valueOf(30.00))
                    .pricePerMinute(BigDecimal.valueOf(2.00))
                    .minimumDeliveryFee(BigDecimal.valueOf(100.00))
                    .maximumDeliveryFee(BigDecimal.valueOf(1000.00))
                    .surgeEnabled(true)
                    .peakHourMultiplier(BigDecimal.valueOf(1.50))
                    .weekendMultiplier(BigDecimal.valueOf(1.20))
                    .nightMultiplier(BigDecimal.valueOf(1.30))
                    .updatedBy(null)
                    .updatedAt(Instant.now())
                    .build();
            repository.save(defaultConfig);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PricingConfiguration getActiveConfiguration() {
        return repository.findFirstByOrderByUpdatedAtDesc()
                .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "No active pricing configuration found."));
    }

    @Override
    @Transactional
    public PricingConfiguration updateConfiguration(UUID adminId, PricingConfigurationUpdateParameters params) {
        PricingConfiguration config = PricingConfiguration.builder()
                .baseFee(params.baseFee())
                .pricePerKilometer(params.pricePerKilometer())
                .pricePerMinute(params.pricePerMinute())
                .minimumDeliveryFee(params.minimumDeliveryFee())
                .maximumDeliveryFee(params.maximumDeliveryFee())
                .surgeEnabled(params.surgeEnabled())
                .peakHourMultiplier(params.peakHourMultiplier())
                .weekendMultiplier(params.weekendMultiplier())
                .nightMultiplier(params.nightMultiplier())
                .updatedBy(adminId)
                .updatedAt(Instant.now(clock))
                .build();
        return repository.save(config);
    }

    @Override
    @Transactional(readOnly = true)
    public PricingBreakdown calculateFee(Location pickup, Location dropoff) {
        PricingConfiguration config = getActiveConfiguration();
        DistanceResult route = distanceProvider.calculate(pickup, dropoff);

        BigDecimal baseFee = config.getBaseFee();
        BigDecimal distanceFee = BigDecimal.valueOf(route.distanceKm()).multiply(config.getPricePerKilometer());
        BigDecimal timeFee = BigDecimal.valueOf(route.durationMinutes()).multiply(config.getPricePerMinute());

        BigDecimal multiplier = BigDecimal.ONE;

        if (config.isSurgeEnabled()) {
            ZonedDateTime now = ZonedDateTime.now(clock);
            if (isPeakHour(now)) {
                multiplier = multiplier.multiply(config.getPeakHourMultiplier());
            }
            if (isWeekend(now)) {
                multiplier = multiplier.multiply(config.getWeekendMultiplier());
            }
            if (isNight(now)) {
                multiplier = multiplier.multiply(config.getNightMultiplier());
            }
        }

        BigDecimal subtotal = baseFee.add(distanceFee).add(timeFee);
        BigDecimal calculatedFee = subtotal.multiply(multiplier);

        BigDecimal finalFee = calculatedFee;
        // Round to the nearest multiple of 10 (Kenya Shillings, ending in zero)
        finalFee = finalFee.divide(BigDecimal.TEN, 0, RoundingMode.HALF_UP).multiply(BigDecimal.TEN);

        if (finalFee.compareTo(config.getMinimumDeliveryFee()) < 0) {
            finalFee = config.getMinimumDeliveryFee();
        } else if (finalFee.compareTo(config.getMaximumDeliveryFee()) > 0) {
            finalFee = config.getMaximumDeliveryFee();
        }

        // Standardize scales to 0 decimal places for financial calculations (no cents)
        return new PricingBreakdown(
                baseFee.setScale(0, RoundingMode.HALF_UP),
                distanceFee.setScale(0, RoundingMode.HALF_UP),
                timeFee.setScale(0, RoundingMode.HALF_UP),
                multiplier.setScale(2, RoundingMode.HALF_UP),
                finalFee.setScale(0, RoundingMode.HALF_UP)
        );
    }

    private boolean isPeakHour(ZonedDateTime now) {
        DayOfWeek day = now.getDayOfWeek();
        boolean isWeekday = day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY;
        if (!isWeekday) {
            return false;
        }
        int hour = now.getHour();
        // Peak hours: 7 AM - 9 AM, 4 PM - 7 PM (16:00 - 19:00)
        return (hour >= 7 && hour < 9) || (hour >= 16 && hour < 19);
    }

    private boolean isWeekend(ZonedDateTime now) {
        DayOfWeek day = now.getDayOfWeek();
        return day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
    }

    private boolean isNight(ZonedDateTime now) {
        int hour = now.getHour();
        // Night hours: 10 PM (22:00) to 5 AM (05:00)
        return hour >= 22 || hour < 5;
    }
}
