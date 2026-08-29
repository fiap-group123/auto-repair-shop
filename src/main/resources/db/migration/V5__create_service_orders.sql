CREATE TABLE service_orders (
    id           UUID         PRIMARY KEY,
    customer_id  UUID         NOT NULL REFERENCES customers (id),
    vehicle_id   UUID         NOT NULL REFERENCES vehicles (id),
    status       VARCHAR(30)  NOT NULL,
    opened_at    TIMESTAMPTZ  NOT NULL
);

CREATE INDEX idx_service_orders_customer_id ON service_orders (customer_id);
CREATE INDEX idx_service_orders_vehicle_id ON service_orders (vehicle_id);

CREATE UNIQUE INDEX uk_service_orders_open_vehicle
    ON service_orders (vehicle_id)
    WHERE status <> 'DELIVERED';
