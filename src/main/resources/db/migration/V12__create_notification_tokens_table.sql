-- V12__create_notification_tokens_table.sql
-- Create notification_tokens table for storing FCM push notification tokens

CREATE TABLE notification_tokens (
    id            UUID         PRIMARY KEY,
    user_id       UUID         NOT NULL,
    token         VARCHAR(255) NOT NULL UNIQUE,
    device_id     VARCHAR(255),
    device_type   VARCHAR(64),
    active        BOOLEAN      DEFAULT TRUE NOT NULL,
    created_at    TIMESTAMPTZ  NOT NULL,
    last_seen_at  TIMESTAMPTZ,
    app_version   VARCHAR(50),
    updated_at    TIMESTAMPTZ  NOT NULL
);

CREATE INDEX idx_notification_tokens_user_id ON notification_tokens (user_id);
