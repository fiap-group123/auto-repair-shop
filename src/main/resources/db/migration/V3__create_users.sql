CREATE TABLE users (
    id             UUID         PRIMARY KEY,
    email          VARCHAR(60)  NOT NULL UNIQUE,
    password_hash  VARCHAR(100) NOT NULL,
    role           VARCHAR(20)  NOT NULL,
    active         BOOLEAN      NOT NULL,
    customer_id    UUID         NULL,
    registered_at  TIMESTAMPTZ  NOT NULL
);
