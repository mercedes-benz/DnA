--
-- UPDATE Script start
--
UPDATE ab_user
SET email = replace(email, '@1', '@2')
WHERE email LIKE '%@1.com';
--
-- UPDATE Script end
--