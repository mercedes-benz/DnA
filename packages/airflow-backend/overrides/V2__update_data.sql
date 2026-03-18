--
-- UPDATE Script start
--
UPDATE ab_user
SET email = replace(email, '@daimler.com', '@mercedes-benz.com')
WHERE email like '%@daimler.com';
--
-- UPDATE Script end
--
