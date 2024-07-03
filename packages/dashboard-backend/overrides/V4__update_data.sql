--
-- UPDATE Script start
--
UPDATE report_nsql SET data = replace(data::text, '@daimler.com', '@mercedes-benz.com')::jsonb;
--
-- UPDATE Script end
--