package com.wafula.teza.rider.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Real-time location coordinates for an active rider.
 * Uses a shared primary key with {@link RiderProfile} (1:1 relationship).
 */
@Entity
@Table(name = "rider_locations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RiderLocation {

    @Id
    @Column(name = "rider_profile_id")
    private UUID id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "rider_profile_id")
    private RiderProfile riderProfile;

    @Column(nullable = false)
    private double latitude;

    @Column(nullable = false)
    private double longitude;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
