--
-- INSERT Script start
--
insert into trino_access_nsql(id,data)
values
	  ('id','{"catalogs": [{"allow": "all","catalog": ".*","user": ".*"}],"schemas": [{"catalog":".*", "owner": true, "schema": ".*", "user":".*"}], "tables" : [{"catalog":".*", "schema":".*", "table" : ".*", "user":".*"}]}');
--
-- INSERT Script end
--