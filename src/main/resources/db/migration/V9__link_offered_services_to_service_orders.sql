ALTER TABLE offered_services
    ADD COLUMN service_order_id        UUID         NOT NULL REFERENCES service_orders (id),
    ADD COLUMN status                  VARCHAR(30)  NOT NULL DEFAULT 'WAITING',
    ADD COLUMN opened_at               TIMESTAMPTZ  NULL,
    ADD COLUMN finished_at             TIMESTAMPTZ  NULL,
    ADD COLUMN estimated_time_seconds  BIGINT       NULL;

ALTER TABLE offered_services DROP CONSTRAINT IF EXISTS offered_services_name_key;

CREATE UNIQUE INDEX uk_offered_services_order_name
    ON offered_services (service_order_id, name);

CREATE INDEX idx_offered_services_service_order_id
    ON offered_services (service_order_id);
