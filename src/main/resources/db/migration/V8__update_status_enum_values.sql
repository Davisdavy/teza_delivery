-- V8__update_status_enum_values.sql
-- Update existing status values in tables to match the new centralized enum values

UPDATE deliveries SET status = 'PENDING' WHERE status = 'DRAFT';
UPDATE rider_profiles SET onboarding_status = 'PENDING' WHERE onboarding_status = 'ONBOARDING';
UPDATE rider_profiles SET onboarding_status = 'APPROVED' WHERE onboarding_status = 'ACTIVE';
UPDATE rider_profiles SET onboarding_status = 'REJECTED' WHERE onboarding_status = 'INACTIVE';
UPDATE delivery_offers SET status = 'DECLINED' WHERE status = 'REJECTED';
