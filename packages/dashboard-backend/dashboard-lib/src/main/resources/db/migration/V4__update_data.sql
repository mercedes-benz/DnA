--
-- UPDATE Script start
--
UPDATE report_nsql SET data = replace(data::text, '@1', '@2')::jsonb
where id ='DNA';
--
-- UPDATE Script end
--