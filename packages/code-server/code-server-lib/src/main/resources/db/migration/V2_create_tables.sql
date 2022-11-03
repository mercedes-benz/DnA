DROP TABLE workspace_nsql;

CREATE TABLE workspace_nsql (
    id text NOT NULL PRIMARY KEY,
    data jsonb NOT NULL
);

CREATE SEQUENCE IF NOT EXISTS workspaceid_seq
  START WITH 1 INCREMENT BY 1;