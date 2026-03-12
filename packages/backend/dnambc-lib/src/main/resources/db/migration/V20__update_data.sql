--
-- UPDATE Script start
--
UPDATE userinfo_nsql
SET data = jsonb_set(data, '{isDeleted}', 'false'::jsonb, true)
WHERE jsonb_typeof(data) = 'object';
--
-- UPDATE Script end
--