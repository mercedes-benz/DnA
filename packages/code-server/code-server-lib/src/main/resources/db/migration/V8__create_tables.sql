DROP TABLE IF EXISTS software_nsql;

CREATE TABLE IF NOT EXISTS software_nsql (
        id text NOT NULL PRIMARY KEY,
        data jsonb NOT NULL
    );
