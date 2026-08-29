CREATE TABLE service_order_items (
    service_order_id   UUID          NOT NULL REFERENCES service_orders (id),
    offered_service_id UUID          NOT NULL REFERENCES offered_services (id),
    description        VARCHAR(60)   NOT NULL,
    unit_price         NUMERIC(10,2) NOT NULL,
    quantity           INTEGER       NOT NULL
);

CREATE INDEX idx_service_order_items_order_id ON service_order_items (service_order_id);
