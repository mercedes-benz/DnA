CREATE INDEX IF NOT EXISTS idx_fabric_catalog_metadata_service_name
ON fabric_catalog_metadata_nsql ((jsonb_extract_path_text(data, 'metadata', 'serviceName')));

UPDATE fabric_workspace_nsql
SET data = jsonb_set(
    data,
    '{lakehouseId}',
    to_jsonb(id),
    true
)
WHERE data->>'lakehouseId' IS NULL;
