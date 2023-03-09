--
-- UPDATE Script start
--
UPDATE datacompliance_nsql SET data = replace(data::text, '@1', '@2')::jsonb
where id ='DNA';

UPDATE dataproduct_nsql SET data = replace(data::text, '@1', '@2')::jsonb
where id ='DNA';

UPDATE datatransfer_nsql SET data = replace(data::text, '@1', '@2')::jsonb
where id ='DNA';
--
-- UPDATE Script end
--