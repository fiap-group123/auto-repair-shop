ALTER TABLE extra_services
    ALTER COLUMN status TYPE VARCHAR(30);

ALTER TABLE extra_services
    ADD COLUMN started_at TIMESTAMPTZ,
    ADD COLUMN finished_at TIMESTAMPTZ,
    ADD COLUMN estimated_time_seconds BIGINT;
