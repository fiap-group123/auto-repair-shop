CREATE TABLE offered_services (
    id            UUID          PRIMARY KEY,
    name          VARCHAR(60)   NOT NULL UNIQUE,
    price         NUMERIC(10,2) NOT NULL,
    active        BOOLEAN       NOT NULL,
    registered_at TIMESTAMPTZ   NOT NULL
);
