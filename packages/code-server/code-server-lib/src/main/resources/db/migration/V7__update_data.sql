--
-- UPDATE Script start
--
UPDATE workspace_nsql SET data = replace(data::text, 'publishedSecuirtyConfig', 'publishedSecurityConfig')::jsonb;
--
-- UPDATE Script end
--