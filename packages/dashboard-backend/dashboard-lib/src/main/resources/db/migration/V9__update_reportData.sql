UPDATE report_nsql
SET data = 
    jsonb_set(
        jsonb_set(
            jsonb_set(
                -- Remove 'reportType' and 'integratedPortal' if they both exist
                CASE
                    WHEN jsonb_exists(data->'description', 'reportType')
                         AND jsonb_exists(data->'description', 'integratedPortal') THEN
                        data #- '{description,reportType}' #- '{description,integratedPortal}'
                    -- Remove only 'reportType' if it exists
                    WHEN jsonb_exists(data->'description', 'reportType') THEN
                        data #- '{description,reportType}'
                    -- Remove only 'integratedPortal' if it exists
                    WHEN jsonb_exists(data->'description', 'integratedPortal') THEN
                        data #- '{description,integratedPortal}'
                    -- No keys to remove
                    ELSE data
                END
                , '{dataWarehouses}'
                , COALESCE(
                    (
                        SELECT jsonb_agg(elem - 'dataClassification')
                        FROM jsonb_array_elements(data->'dataWarehouses') elem
                        WHERE jsonb_typeof(data->'dataWarehouses') = 'array' 
                            AND jsonb_typeof(elem) = 'object'
                    ),
                    '[]'::jsonb
                )
            )
            , '{singleDataSources}'
            , COALESCE(
                (
                    SELECT jsonb_agg(elem - 'dataClassification')
                    FROM jsonb_array_elements(data->'singleDataSources') elem
                    WHERE jsonb_typeof(data->'singleDataSources') = 'array'
                        AND jsonb_typeof(elem) = 'object'
                ),
                '[]'::jsonb
            )
        )
        , '{customer,internalCustomers}'
        , COALESCE(
            (
                SELECT jsonb_agg(elem - 'processOwner')
                FROM jsonb_array_elements(data->'customer'->'internalCustomers') elem
                WHERE jsonb_typeof(data->'customer'->'internalCustomers') = 'array'
                    AND jsonb_typeof(elem) = 'object'
            ),
            '[]'::jsonb
        )
    );



