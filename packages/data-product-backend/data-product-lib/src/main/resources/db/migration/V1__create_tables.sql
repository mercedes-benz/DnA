CREATE TABLE if not exists datacompliance_nsql (
    id TEXT primary key,
    data jsonb NOT NULL
);

CREATE TABLE if not exists entityid_nsql (
    id TEXT primary key,
    data jsonb NOT NULL
);