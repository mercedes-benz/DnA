CREATE TABLE IF NOT EXIST collaborator_sql(
 id varchar(255) not null,
 dataiku_id varchar(255),
 givenname varchar(255),
 permission varchar(255),
 surname varchar(255),
 userid varchar(255) not null, 
 primary key (id));

CREATE TABLE IF NOT EXIST dataiku_sql(
 id varchar(255) not null,
 cloud_profile varchar(255),
 created_by varchar(255),
 created_on date,
 description varchar(255),
 project_name varchar(255) not null, 
 primary key (id));

CREATE TABLE IF NOT EXISTS userprivilege_sql(
 id text NOT NULL PRIMARY KEY,
 userId text NOT NULL,
 profile text NOT NULL,
 givenName text,
 surName text
);
