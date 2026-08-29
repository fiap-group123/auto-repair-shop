ALTER TABLE service_orders
    DROP COLUMN estimate_time;

ALTER TABLE service_orders
    ADD COLUMN estimate_time_seconds BIGINT NULL;
