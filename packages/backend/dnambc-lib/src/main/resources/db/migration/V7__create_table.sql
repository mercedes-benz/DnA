CREATE TABLE if not exists division_audit_nsql (
    id TEXT primary key,
    data jsonb NOT NULL
);