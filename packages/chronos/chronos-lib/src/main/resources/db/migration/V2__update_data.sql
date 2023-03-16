--
-- UPDATE Script start
--
UPDATE forecast_nsql SET data = replace(data::text, '@1', '@2')::jsonb
where id ='DNA';
--
-- UPDATE Script end
--