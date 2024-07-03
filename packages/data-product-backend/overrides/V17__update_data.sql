--
-- UPDATE Script start
--
UPDATE datacompliance_nsql SET data = replace(data::text, '@daimler.com', '@mercedes-benz.com')::jsonb;

UPDATE dataproduct_nsql SET data = replace(data::text, '@daimler.com', '@mercedes-benz.com')::jsonb;

UPDATE datatransfer_nsql SET data = replace(data::text, '@daimler.com', '@mercedes-benz.com')::jsonb;
--
-- UPDATE Script end
--