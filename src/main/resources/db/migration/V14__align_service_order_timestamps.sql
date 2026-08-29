ALTER TABLE service_orders RENAME COLUMN opened_at TO registered_at;
ALTER TABLE service_orders RENAME COLUMN diagnosis_started_at TO opened_at;
ALTER TABLE service_orders RENAME COLUMN completed_at TO finished_at;

ALTER TABLE service_orders
    DROP COLUMN IF EXISTS diagnosis_finished_at,
    DROP COLUMN IF EXISTS approved_at,
    DROP COLUMN IF EXISTS delivered_at;
