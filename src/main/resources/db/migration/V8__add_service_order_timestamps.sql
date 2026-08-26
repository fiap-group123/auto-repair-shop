ALTER TABLE service_orders
    ADD COLUMN diagnosis_started_at  TIMESTAMPTZ NULL,
    ADD COLUMN diagnosis_finished_at TIMESTAMPTZ NULL,
    ADD COLUMN approved_at           TIMESTAMPTZ NULL,
    ADD COLUMN completed_at          TIMESTAMPTZ NULL,
    ADD COLUMN delivered_at          TIMESTAMPTZ NULL;
