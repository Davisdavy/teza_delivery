-- V5__make_delivery_merchant_nullable_and_add_customer.sql
-- Alter deliveries table to make merchant_id nullable and add customer_id referencing users.

-- 1. Make merchant_id column nullable in deliveries
ALTER TABLE deliveries ALTER COLUMN merchant_id DROP NOT NULL;

-- 2. Add customer_id column referencing users(id)
ALTER TABLE deliveries ADD COLUMN customer_id UUID REFERENCES users (id) ON DELETE SET NULL;

-- 3. Add index on customer_id for performance
CREATE INDEX idx_deliveries_customer_id ON deliveries (customer_id);
