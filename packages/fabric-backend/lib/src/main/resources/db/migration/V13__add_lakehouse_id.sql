UPDATE fabric_workspace_nsql
SET data = jsonb_set(
    data,
    '{lakehouseId}',
    to_jsonb(id),
    true
)
WHERE data->>'lakehouseId' IS NULL;