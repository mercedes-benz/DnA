CREATE TABLE IF NOT EXISTS level_sql
(
    id               BIGINT             DEFAULT nextval('dashboard_sequence') PRIMARY KEY,
    name             VARCHAR(255)       
);

CREATE TABLE IF NOT EXISTS legal_entity_sql
(
    id               BIGINT             DEFAULT nextval('dashboard_sequence') PRIMARY KEY,
    name             VARCHAR(255)       
);

CREATE TABLE IF NOT EXISTS common_function_sql
(
    id               BIGINT             DEFAULT nextval('dashboard_sequence') PRIMARY KEY,
    name             VARCHAR(255)       
);

CREATE TABLE IF NOT EXISTS datawarehouse_sql
(
    id               BIGINT             DEFAULT nextval('dashboard_sequence') PRIMARY KEY,
    name             VARCHAR(255)       
);

CREATE TABLE IF NOT EXISTS data_classification_sql
(
    id               BIGINT             DEFAULT nextval('dashboard_sequence') PRIMARY KEY,
    name             VARCHAR(255)       
);