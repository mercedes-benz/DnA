--
-- UPDATE Script start
--
update workspace_nsql set data = data #- '{password}';
--
-- UPDATE Script end
--