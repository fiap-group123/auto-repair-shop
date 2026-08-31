ALTER TABLE services
    RENAME COLUMN estimated_time_nanoseconds TO estimated_time_seconds;

ALTER TABLE service_orders
    RENAME COLUMN estimated_time_nanoseconds TO estimated_time_seconds;
