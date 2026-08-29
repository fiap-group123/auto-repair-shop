ALTER TABLE services
    DROP COLUMN IF EXISTS description,
    DROP COLUMN IF EXISTS unit_price,
    DROP COLUMN IF EXISTS quantity;

ALTER TABLE services
    ADD CONSTRAINT uk_services_order_service UNIQUE (service_order_id, service_id);
