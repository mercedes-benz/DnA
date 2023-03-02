--
-- UPDATE Script start
--
UPDATE ab_user
SET email = replace(email, '@1', '@2')
WHERE city LIKE like '%@1.com';
--
-- UPDATE Script end
--