CREATE TABLE if not exists fabric_catalog_metadata_nsql (
    id TEXT primary key,
    data jsonb NOT NULL
);