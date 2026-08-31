CREATE TABLE customer_invites (
    id           UUID         PRIMARY KEY,
    customer_id  UUID         NOT NULL REFERENCES customers (id),
    token_hash   VARCHAR(64)  NOT NULL UNIQUE,
    expires_at   TIMESTAMPTZ  NOT NULL,
    consumed_at  TIMESTAMPTZ  NULL,
    created_at   TIMESTAMPTZ  NOT NULL
);

CREATE INDEX idx_customer_invites_customer_id ON customer_invites (customer_id);
