--
-- UPDATE Script start
--
UPDATE workspace_nsql SET data = replace(data::text, '@1', '@2')::jsonb
where id ='DNA';
--
-- UPDATE Script end
--