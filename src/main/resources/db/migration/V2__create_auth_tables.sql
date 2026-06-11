-- Auth module schema: revocable refresh tokens. Depends on V1's users table.

CREATE TABLE refresh_tokens (
    id         UUID         PRIMARY KEY,
    token_hash VARCHAR(64)  NOT NULL UNIQUE,
    user_id    UUID         NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    expires_at TIMESTAMPTZ  NOT NULL,
    revoked    BOOLEAN      NOT NULL DEFAULT FALSE
);

-- Refresh validation looks tokens up by hash; logout/rotation revoke by user.
CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens (user_id);
