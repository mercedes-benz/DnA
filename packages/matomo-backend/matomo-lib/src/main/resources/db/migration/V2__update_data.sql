--
-- UPDATE Script start
--
UPDATE matomo_nsql SET data = replace(data::text, '@1', '@2')::jsonb
where id ='DNA';
--
-- UPDATE Script end
--