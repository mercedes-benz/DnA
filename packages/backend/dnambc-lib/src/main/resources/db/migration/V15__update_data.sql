--
-- UPDATE Script start
--
UPDATE userinfo_nsql SET data = replace(data::text, '@1', '@2')::jsonb
where id ='DNA';

UPDATE solution_nsql SET data = replace(data::text, '@1', '@2')::jsonb
where id ='DNA';

--
-- UPDATE Script end
--