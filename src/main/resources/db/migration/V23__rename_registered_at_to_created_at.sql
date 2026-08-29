ALTER TABLE customers
    RENAME COLUMN registered_at TO created_at;

ALTER TABLE vehicles
    RENAME COLUMN registered_at TO created_at;

ALTER TABLE users
    RENAME COLUMN registered_at TO created_at;

ALTER TABLE services
    RENAME COLUMN registered_at TO created_at;

ALTER TABLE service_orders
    RENAME COLUMN registered_at TO created_at;
