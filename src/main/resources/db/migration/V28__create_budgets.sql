CREATE TABLE budgets (
    id               UUID           PRIMARY KEY,
    service_order_id UUID           NOT NULL UNIQUE REFERENCES service_orders (id),
    total            NUMERIC(10, 2) NOT NULL,
    status           VARCHAR(20)    NOT NULL,
    created_at       TIMESTAMPTZ    NOT NULL,
    finished_at      TIMESTAMPTZ
);
