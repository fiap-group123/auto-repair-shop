ALTER TABLE service_order_items RENAME TO services;

ALTER TABLE services RENAME COLUMN offered_service_id TO service_id;

ALTER INDEX idx_service_order_items_order_id RENAME TO idx_services_order_id;
