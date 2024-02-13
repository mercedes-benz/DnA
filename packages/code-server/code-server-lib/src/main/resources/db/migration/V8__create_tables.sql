DROP TABLE IF EXISTS software_nsql;

CREATE TABLE IF NOT EXISTS software_nsql (
        id text NOT NULL PRIMARY KEY,
        data jsonb NOT NULL
    );

-- insert into division_nsql (id, data)
-- values('1','{"name": "division1", "subdivisions": [{"id": "1", "name": "subdivision1"}, {"id": "2", "name": "subdivision2"}]}'),
-- 	  ('2','{"name": "division2", "subdivisions": [{"id": "1", "name": "subdivision1"}, {"id": "2", "name": "subdivision2"}]}');

-- insert into software_nsql(id,data)
-- values('1','{"name": "Quarkus", "version": "Quarkus-2.0"}'),
--       ('2','{"name": "Quarkus1", "version": "Quarkus-2.0"}');

-- INSERT INTO software (id, data) 
-- VALUES 
--   (1, '{"name": "Gradle", "versions": ["1.3v", "1.6v"]}'),
--   (2, '{"name": "Node", "versions": ["1.7v"]}');
