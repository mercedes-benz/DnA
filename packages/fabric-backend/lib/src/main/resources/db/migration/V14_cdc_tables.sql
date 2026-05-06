ALTER TABLE fabric_catalog_metadata
ADD COLUMN IF NOT EXISTS published_cdc_tables JSON;

UPDATE fabric_catalog_metadata
SET published_cdc_tables = JSON_SET(
    IFNULL(published_cdc_tables, '{}'),
    '$.createdby',
    'ada'
);