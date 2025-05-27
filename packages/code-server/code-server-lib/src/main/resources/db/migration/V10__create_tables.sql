CREATE TABLE IF NOT EXISTS user_wsgroup_nsql (
    id text NOT NULL PRIMARY KEY,
    data jsonb NOT NULL
);

UPDATE workspace_nsql
	SET data = data || '{"activeInGroup": false}'::jsonb;