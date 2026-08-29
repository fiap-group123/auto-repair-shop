ALTER TABLE offered_services RENAME TO services;

ALTER INDEX IF EXISTS uk_offered_services_order_name RENAME TO uk_services_order_name;
ALTER INDEX IF EXISTS idx_offered_services_service_order_id RENAME TO idx_services_service_order_id;
