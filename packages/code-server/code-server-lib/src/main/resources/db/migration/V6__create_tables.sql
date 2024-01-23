DROP TABLE recipe_nsql;

CREATE TABLE
    recipe_nsql (
        id text NOT NULL PRIMARY KEY,
        data jsonb NOT NULL
    );

