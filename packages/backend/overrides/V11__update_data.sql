--
-- UPDATE Script start
--
UPDATE customerjourneyphase_nsql SET data = replace(data::text, '|', '<')::jsonb;

UPDATE marketingcommunicationchannel_nsql SET data = replace(data :: text, 'E-Commerce','Online Shop') :: jsonb;

--
-- UPDATE Script end
--
