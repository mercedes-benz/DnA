-- Migrate publishedCdcTables from string array format to object array format.
-- Existing rows may have publishedCdcTables stored as ["tableName1", "tableName2", ...]
-- which is incompatible with the new List<CdcTableDetail> Java type.
-- This migration converts each string entry to a minimal CdcTableDetail object
-- with just the productName field populated, or removes the field if empty.

UPDATE fabric_catalog_metadata_nsql
SET data = jsonb_set(
    data,
    '{publishedCdcTables}',
    (
        SELECT COALESCE(
            jsonb_agg(
                jsonb_build_object('productName', elem)
            ),
            '[]'::jsonb
        )
        FROM jsonb_array_elements_text(data->'publishedCdcTables') AS elem
    )
)
WHERE data ? 'publishedCdcTables'
  AND jsonb_typeof(data->'publishedCdcTables') = 'array'
  AND jsonb_array_length(data->'publishedCdcTables') > 0
  AND jsonb_typeof(data->'publishedCdcTables'->0) = 'string';
