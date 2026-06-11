-- V6__rename_rider_profile_is_active_to_is_available.sql
-- Rename rider_profiles.is_active to is_available to prevent name clashes with onboarding status.

ALTER TABLE rider_profiles RENAME COLUMN is_active TO is_available;
