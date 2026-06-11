-- V7__add_delivery_timestamp_metrics.sql
-- Add analytics and SLA metrics timestamps to deliveries table

ALTER TABLE deliveries ADD COLUMN accepted_at TIMESTAMPTZ;
ALTER TABLE deliveries ADD COLUMN picked_up_at TIMESTAMPTZ;
ALTER TABLE deliveries ADD COLUMN delivered_at TIMESTAMPTZ;
ALTER TABLE deliveries ADD COLUMN cancelled_at TIMESTAMPTZ;
