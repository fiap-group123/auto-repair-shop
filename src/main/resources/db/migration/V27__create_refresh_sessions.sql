CREATE TABLE refresh_sessions (
    id          UUID        PRIMARY KEY,
    user_id     UUID        NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    family_id   UUID        NOT NULL,
    token_hash  VARCHAR(64) NOT NULL UNIQUE,
    expires_at  TIMESTAMPTZ NOT NULL,
    revoked_at  TIMESTAMPTZ,
    replaced_by UUID,
    created_at  TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_refresh_sessions_family ON refresh_sessions (family_id);
CREATE INDEX idx_refresh_sessions_user ON refresh_sessions (user_id);
