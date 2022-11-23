CREATE TABLE if not exists datacompliance_audit_nsql (
    id TEXT primary key,
    data jsonb NOT NULL
);