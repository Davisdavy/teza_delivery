-- V14__add_verification_otp_to_deliveries.sql
-- Add verification OTP field to deliveries table for Proof of Delivery.
ALTER TABLE deliveries ADD COLUMN verification_otp VARCHAR(6);
