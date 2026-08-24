CREATE TABLE customers (
    id            UUID         PRIMARY KEY,
    document_id   VARCHAR(14)  NOT NULL UNIQUE,
    name          VARCHAR(60)  NOT NULL,
    email         VARCHAR(60)  NOT NULL,
    phone         VARCHAR(11)  NOT NULL,
    active        BOOLEAN      NOT NULL,
    registered_at TIMESTAMPTZ  NOT NULL
);
