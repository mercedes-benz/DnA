--
-- INC Script start
--

INSERT INTO userrole_nsql(id, data) VALUES ('1', '{"name": "User"}') 
ON CONFLICT (id) DO UPDATE SET data = '{"name": "User"}';

INSERT INTO userrole_nsql(id, data) VALUES ('3', '{"name": "Admin"}') 
ON CONFLICT (id) DO UPDATE SET data = '{"name": "Admin"}';

INSERT INTO userrole_nsql(id, data) VALUES ('4', '{"name": "ReportAdmin"}') 
ON CONFLICT (id) DO UPDATE SET data = '{"name": "ReportAdmin"}';

--
-- INC Script end
--