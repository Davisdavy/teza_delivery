-- V3__create_delivery_platform_tables.sql
-- Create schema for rider, merchant, and delivery modules with relationships and indexes.

-- 1. Rider Module: rider_profiles
CREATE TABLE rider_profiles (
    id                 UUID         PRIMARY KEY,
    user_id            UUID         NOT NULL UNIQUE REFERENCES users (id) ON DELETE CASCADE,
    vehicle_type       VARCHAR(32)  NOT NULL,
    vehicle_plate_num  VARCHAR(32),
    is_active          BOOLEAN      NOT NULL DEFAULT FALSE,
    onboarding_status  VARCHAR(32)  NOT NULL,
    created_at         TIMESTAMPTZ  NOT NULL,
    updated_at         TIMESTAMPTZ  NOT NULL
);

CREATE INDEX idx_rider_profiles_user_id ON rider_profiles (user_id);

-- 2. Rider Module: rider_locations
CREATE TABLE rider_locations (
    rider_profile_id   UUID         PRIMARY KEY REFERENCES rider_profiles (id) ON DELETE CASCADE,
    latitude           DOUBLE PRECISION NOT NULL,
    longitude          DOUBLE PRECISION NOT NULL,
    updated_at         TIMESTAMPTZ  NOT NULL
);

-- 3. Merchant Module: merchants
CREATE TABLE merchants (
    id                 UUID         PRIMARY KEY,
    user_id            UUID         NOT NULL UNIQUE REFERENCES users (id) ON DELETE CASCADE,
    business_name      VARCHAR(100) NOT NULL,
    phone_number       VARCHAR(32),
    address            VARCHAR(255),
    created_at         TIMESTAMPTZ  NOT NULL,
    updated_at         TIMESTAMPTZ  NOT NULL
);

CREATE INDEX idx_merchants_user_id ON merchants (user_id);

-- 4. Delivery Module: deliveries
CREATE TABLE deliveries (
    id                 UUID         PRIMARY KEY,
    merchant_id        UUID         NOT NULL REFERENCES merchants (id) ON DELETE CASCADE,
    rider_id           UUID         REFERENCES rider_profiles (id) ON DELETE SET NULL,
    pickup_address     VARCHAR(255) NOT NULL,
    pickup_latitude    DOUBLE PRECISION NOT NULL,
    pickup_longitude   DOUBLE PRECISION NOT NULL,
    dropoff_address    VARCHAR(255) NOT NULL,
    dropoff_latitude   DOUBLE PRECISION NOT NULL,
    dropoff_longitude  DOUBLE PRECISION NOT NULL,
    status             VARCHAR(32)  NOT NULL,
    delivery_fee       NUMERIC(10, 2) NOT NULL,
    created_at         TIMESTAMPTZ  NOT NULL,
    updated_at         TIMESTAMPTZ  NOT NULL
);

CREATE INDEX idx_deliveries_merchant_id ON deliveries (merchant_id);
CREATE INDEX idx_deliveries_rider_id ON deliveries (rider_id);
CREATE INDEX idx_deliveries_status ON deliveries (status);

-- 5. Delivery Module: delivery_offers
CREATE TABLE delivery_offers (
    id                 UUID         PRIMARY KEY,
    delivery_id        UUID         NOT NULL REFERENCES deliveries (id) ON DELETE CASCADE,
    rider_id           UUID         NOT NULL REFERENCES rider_profiles (id) ON DELETE CASCADE,
    status             VARCHAR(32)  NOT NULL,
    expires_at         TIMESTAMPTZ  NOT NULL,
    created_at         TIMESTAMPTZ  NOT NULL,
    updated_at         TIMESTAMPTZ  NOT NULL
);

CREATE INDEX idx_delivery_offers_delivery_id ON delivery_offers (delivery_id);
CREATE INDEX idx_delivery_offers_rider_id ON delivery_offers (rider_id);
CREATE INDEX idx_delivery_offers_status ON delivery_offers (status);

-- 6. Delivery Module: delivery_status_history
CREATE TABLE delivery_status_history (
    id                 UUID         PRIMARY KEY,
    delivery_id        UUID         NOT NULL REFERENCES deliveries (id) ON DELETE CASCADE,
    status             VARCHAR(32)  NOT NULL,
    changed_by_user_id UUID         NOT NULL REFERENCES users (id),
    reason             VARCHAR(255),
    created_at         TIMESTAMPTZ  NOT NULL
);

CREATE INDEX idx_delivery_status_history_delivery_id ON delivery_status_history (delivery_id);
