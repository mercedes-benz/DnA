--
-- DDL Script start
--

CREATE TABLE IF NOT EXISTS classification_nsql
(
    id               TEXT             PRIMARY KEY,
    data             jsonb            not null       
);

CREATE TABLE IF NOT EXISTS storage_nsql
(
    id               TEXT             PRIMARY KEY,
    data             jsonb            not null       
);

CREATE TABLE IF NOT EXISTS eventpushexception_nsql
(
    id               TEXT             PRIMARY KEY,
    data             jsonb            not null       
);

--
-- DDL Script end
--