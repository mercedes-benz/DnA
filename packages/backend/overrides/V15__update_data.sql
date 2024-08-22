--
-- UPDATE Script start
--
UPDATE userinfo_nsql SET data = replace(data::text, '@daimler.com', '@mercedes-benz.com')::jsonb
WHERE (jsonb_extract_path_text(data,'email')) like '%@daimler.com';

UPDATE solution_nsql SET data = replace(data::text, '@daimler.com', '@mercedes-benz.com')::jsonb;


--
-- UPDATE Script end
--