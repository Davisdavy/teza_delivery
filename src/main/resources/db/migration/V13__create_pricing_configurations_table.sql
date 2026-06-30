-- V13__create_pricing_configurations_table.sql
-- Create table for managing delivery pricing engine configurations.

CREATE TABLE pricing_configurations (
    id                     UUID PRIMARY KEY,
    base_fee               NUMERIC(10, 2) NOT NULL,
    price_per_kilometer    NUMERIC(10, 2) NOT NULL,
    price_per_minute       NUMERIC(10, 2) NOT NULL,
    minimum_delivery_fee   NUMERIC(10, 2) NOT NULL,
    maximum_delivery_fee   NUMERIC(10, 2) NOT NULL,
    surge_enabled          BOOLEAN NOT NULL DEFAULT FALSE,
    peak_hour_multiplier   NUMERIC(5, 2) NOT NULL,
    weekend_multiplier     NUMERIC(5, 2) NOT NULL,
    night_multiplier       NUMERIC(5, 2) NOT NULL,
    updated_by             UUID REFERENCES users (id) ON DELETE SET NULL,
    updated_at             TIMESTAMPTZ NOT NULL
);
