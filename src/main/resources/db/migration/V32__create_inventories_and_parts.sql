CREATE TABLE inventories (
    id          UUID           PRIMARY KEY,
    name        VARCHAR(60)    NOT NULL UNIQUE,
    kind        VARCHAR(20)    NOT NULL,
    unit_price  NUMERIC(10, 2) NOT NULL,
    stock       INTEGER        NOT NULL,
    active      BOOLEAN        NOT NULL,
    created_at  TIMESTAMPTZ    NOT NULL
);

CREATE TABLE parts (
    id               UUID           PRIMARY KEY,
    service_order_id UUID           NOT NULL REFERENCES service_orders (id),
    inventory_id     UUID           NOT NULL REFERENCES inventories (id),
    quantity         INTEGER        NOT NULL,
    unit_price       NUMERIC(10, 2) NOT NULL,
    created_at       TIMESTAMPTZ    NOT NULL,
    UNIQUE (service_order_id, inventory_id)
);

CREATE INDEX idx_parts_service_order_id ON parts (service_order_id);
CREATE INDEX idx_parts_inventory_id ON parts (inventory_id);
