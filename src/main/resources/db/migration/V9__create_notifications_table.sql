-- V9__create_notifications_table.sql
-- Create notifications table for in-app messaging history

CREATE TABLE notifications (
    id          UUID         PRIMARY KEY,
    user_id     UUID         NOT NULL,
    title       VARCHAR(255) NOT NULL,
    message     TEXT         NOT NULL,
    status      VARCHAR(32)  NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL
);

CREATE INDEX idx_notifications_user_id ON notifications (user_id);
CREATE INDEX idx_notifications_created_at ON notifications (created_at DESC);
