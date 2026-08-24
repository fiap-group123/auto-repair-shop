CREATE TABLE vehicles (
    id            UUID         PRIMARY KEY,
    owner_id      UUID         NOT NULL REFERENCES customers (id),
    plate         VARCHAR(7)   NOT NULL UNIQUE,
    brand         VARCHAR(40)  NOT NULL,
    model         VARCHAR(40)  NOT NULL,
    year          INTEGER      NOT NULL,
    registered_at TIMESTAMPTZ  NOT NULL
);

CREATE INDEX idx_vehicles_owner_id ON vehicles (owner_id);
