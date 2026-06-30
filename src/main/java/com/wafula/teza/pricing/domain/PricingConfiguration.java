package com.wafula.teza.pricing.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "pricing_configurations")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PricingConfiguration {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "base_fee", nullable = false)
    private BigDecimal baseFee;

    @Column(name = "price_per_kilometer", nullable = false)
    private BigDecimal pricePerKilometer;

    @Column(name = "price_per_minute", nullable = false)
    private BigDecimal pricePerMinute;

    @Column(name = "minimum_delivery_fee", nullable = false)
    private BigDecimal minimumDeliveryFee;

    @Column(name = "maximum_delivery_fee", nullable = false)
    private BigDecimal maximumDeliveryFee;

    @Column(name = "surge_enabled", nullable = false)
    private boolean surgeEnabled;

    @Column(name = "peak_hour_multiplier", nullable = false)
    private BigDecimal peakHourMultiplier;

    @Column(name = "weekend_multiplier", nullable = false)
    private BigDecimal weekendMultiplier;

    @Column(name = "night_multiplier", nullable = false)
    private BigDecimal nightMultiplier;

    @Column(name = "updated_by")
    private UUID updatedBy;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
