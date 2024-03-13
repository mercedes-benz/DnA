DROP TABLE IF EXISTS recipe_nsql;

CREATE TABLE IF NOT EXISTS recipe_nsql (
        id text NOT NULL PRIMARY KEY,
        data jsonb NOT NULL
    );

