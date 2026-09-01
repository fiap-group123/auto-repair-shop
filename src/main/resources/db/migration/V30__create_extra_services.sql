CREATE TABLE extra_services (
    id               UUID           PRIMARY KEY,
    service_order_id UUID           NOT NULL REFERENCES service_orders (id),
    name             VARCHAR(60)    NOT NULL,
    price            NUMERIC(10, 2) NOT NULL,
    status           VARCHAR(20)    NOT NULL,
    created_at       TIMESTAMPTZ    NOT NULL,
    UNIQUE (service_order_id, name)
);

CREATE INDEX idx_extra_services_service_order_id ON extra_services (service_order_id);
