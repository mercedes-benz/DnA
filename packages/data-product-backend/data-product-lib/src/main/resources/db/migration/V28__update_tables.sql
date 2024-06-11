
--  datatransfer table update


UPDATE datatransfer_nsql dt
SET data = jsonb_set(
    dt.data, 
    '{providerInformation,contactInformation,leanIXDetails}', 
    COALESCE(
        (SELECT pn.data::jsonb 
         FROM planningit_nsql pn
         WHERE pn.id = dt.data->'providerInformation'->'contactInformation'->>'appId'), 
        'null'::jsonb
    ),
    true
)
WHERE dt.data->'providerInformation'->'contactInformation'->>'appId'is not NULL;
 
UPDATE datatransfer_nsql dt
SET data = jsonb_set(
    dt.data, 
    '{consumerInformation,contactInformation,leanIXDetails}', 
    COALESCE(
        (SELECT pn.data::jsonb 
         FROM planningit_nsql pn
         WHERE pn.id = dt.data->'consumerInformation'->'contactInformation'->>'appId'), 
        'null'::jsonb
    ),
    true
)
WHERE dt.data->'consumerInformation'->'contactInformation'->>'appId'is not NULL;

--  dataproduct table update

UPDATE dataproduct_nsql dt
SET data = jsonb_set(
    dt.data, 
    '{contactInformation,leanIXDetails}', 
    COALESCE(
        (SELECT pn.data::jsonb 
         FROM planningit_nsql pn
         WHERE pn.id = dt.data->'contactInformation'->>'appId'), 
        'null'::jsonb
    ),
    true
)
WHERE dt.data->'contactInformation'->>'appId' IS NOT NULL