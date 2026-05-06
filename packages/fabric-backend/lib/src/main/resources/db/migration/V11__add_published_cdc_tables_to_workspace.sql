ALTER TABLE fabric_workspace_nsql
ADD COLUMN IF NOT EXISTS published_cdc_tables jsonb;
