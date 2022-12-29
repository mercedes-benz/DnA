
CREATE TABLE if not exists dataproduct_nsql (
    id TEXT primary key,
    data jsonb NOT NULL
);

CREATE SEQUENCE IF NOT EXISTS dataproduct_seq
  START WITH 1 INCREMENT BY 1;

  
CREATE TABLE if not exists carlafunction_nsql (
    id TEXT primary key,
    data jsonb NOT NULL
);

CREATE TABLE if not exists agilereleasetrain_nsql (
    id TEXT primary key,
    data jsonb NOT NULL
);

CREATE TABLE if not exists datacatalog_nsql (
    id TEXT primary key,
    data jsonb NOT NULL
);