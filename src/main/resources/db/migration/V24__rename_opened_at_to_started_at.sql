ALTER TABLE services
    RENAME COLUMN opened_at TO started_at;

ALTER TABLE service_orders
    RENAME COLUMN opened_at TO started_at;
