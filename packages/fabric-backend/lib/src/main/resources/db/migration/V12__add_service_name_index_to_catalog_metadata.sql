CREATE INDEX IF NOT EXISTS idx_fabric_catalog_metadata_service_name
ON fabric_catalog_metadata_nsql ((jsonb_extract_path_text(data, 'metadata', 'serviceName')));
