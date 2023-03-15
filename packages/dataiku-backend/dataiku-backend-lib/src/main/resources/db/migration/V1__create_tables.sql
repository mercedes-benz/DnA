CREATE TABLE IF NOT EXISTS dataiku_sql(
id text NOT NULL PRIMARY KEY,
project_name text NOT NULL,
description text ,
cloud_profile text,
created_by text,
created_on date
);

CREATE TABLE IF NOT EXISTS collaborator_sql(
id text NOT NULL PRIMARY KEY,
userid text NOT NULL,
dataiku_id text NOT NULL ,
givenname text,
surname text,
permission text,
CONSTRAINT fk_dataiku_collab
      FOREIGN KEY(dataiku_id) 
	  REFERENCES dataiku_sql(id)
);

CREATE TABLE IF NOT EXISTS userprivilege_sql(
id text NOT NULL PRIMARY KEY,
userId text NOT NULL,
profile text NOT NULL,
givenName text,
surName text
);