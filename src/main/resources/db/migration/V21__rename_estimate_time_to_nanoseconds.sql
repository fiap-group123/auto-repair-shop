ALTER TABLE services
    RENAME COLUMN estimated_time_seconds TO estimated_time_nanoseconds;

ALTER TABLE service_orders
    RENAME COLUMN estimate_time_seconds TO estimated_time_nanoseconds;
