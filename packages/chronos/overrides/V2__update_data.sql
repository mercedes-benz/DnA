--
-- UPDATE Script start
--
UPDATE forecast_nsql SET data = replace(data::text, '@daimler.com', '@mercedes-benz.com')::jsonb;
--
-- UPDATE Script end
--