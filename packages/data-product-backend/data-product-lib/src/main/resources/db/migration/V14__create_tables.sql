CREATE TABLE if not exists platform_nsql (
    id TEXT primary key,
    data jsonb NOT NULL
);

CREATE TABLE if not exists front_end_tools_nsql (
    id TEXT primary key,
    data jsonb NOT NULL
);
