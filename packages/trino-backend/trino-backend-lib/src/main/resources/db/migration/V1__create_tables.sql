
CREATE TABLE IF NOT EXISTS trino_connectors_nsql (
    id text NOT NULL PRIMARY KEY,
    data jsonb NOT NULL
);

CREATE TABLE IF NOT EXISTS trino_datalake_nsql (
    id text NOT NULL PRIMARY KEY,
    data jsonb NOT NULL
);


CREATE TABLE IF NOT EXISTS trino_access_nsql (
    id text NOT NULL PRIMARY KEY,
    data jsonb NOT NULL
);
