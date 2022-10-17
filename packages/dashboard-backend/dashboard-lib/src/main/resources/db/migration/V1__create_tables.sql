CREATE SEQUENCE IF NOT EXISTS lov_sequence
  START WITH 1000 INCREMENT BY 1;

CREATE TABLE IF NOT EXISTS department_sql
(
    id               BIGINT             DEFAULT nextval('lov_sequence') PRIMARY KEY,
    name             VARCHAR(255)       
);

CREATE TABLE IF NOT EXISTS frontend_technology_sql
(
    id               BIGINT             DEFAULT nextval('lov_sequence') PRIMARY KEY,
    name             VARCHAR(255)       
);

CREATE TABLE IF NOT EXISTS integrated_portal_sql
(
    id               BIGINT             DEFAULT nextval('lov_sequence') PRIMARY KEY,
    name             VARCHAR(255)       
);

CREATE TABLE IF NOT EXISTS kpi_name_sql
(
    id               BIGINT             DEFAULT nextval('lov_sequence') PRIMARY KEY,
    name             VARCHAR(255)       
);

CREATE TABLE IF NOT EXISTS reporting_cause_sql
(
    id               BIGINT             DEFAULT nextval('lov_sequence') PRIMARY KEY,
    name             VARCHAR(255)       
);

CREATE TABLE IF NOT EXISTS status_sql
(
    id               BIGINT             DEFAULT nextval('lov_sequence') PRIMARY KEY,
    name             VARCHAR(255)       
);

CREATE TABLE IF NOT EXISTS agile_releasetrain_sql
(
    id               BIGINT             DEFAULT nextval('lov_sequence') PRIMARY KEY,
    name             VARCHAR(255)     
);

CREATE TABLE IF NOT EXISTS connection_type_sql
(
    id               BIGINT             DEFAULT nextval('lov_sequence') PRIMARY KEY,
    name             VARCHAR(255)     
);

CREATE TABLE IF NOT EXISTS tag_sql
(
    id               BIGINT             DEFAULT nextval('lov_sequence') PRIMARY KEY,
    name             VARCHAR(255)       
);

CREATE TABLE IF NOT EXISTS level_sql
(
    id               BIGINT             DEFAULT nextval('lov_sequence') PRIMARY KEY,
    name             VARCHAR(255)       
);

CREATE TABLE IF NOT EXISTS legal_entity_sql
(
    id               BIGINT             DEFAULT nextval('lov_sequence') PRIMARY KEY,
    name             VARCHAR(255)       
);

CREATE TABLE IF NOT EXISTS common_function_sql
(
    id               BIGINT             DEFAULT nextval('lov_sequence') PRIMARY KEY,
    name             VARCHAR(255)       
);

CREATE TABLE IF NOT EXISTS datawarehouse_sql
(
    id               BIGINT             DEFAULT nextval('lov_sequence') PRIMARY KEY,
    name             VARCHAR(255)       
);

CREATE TABLE IF NOT EXISTS data_classification_sql
(
    id               BIGINT             DEFAULT nextval('lov_sequence') PRIMARY KEY,
    name             VARCHAR(255)       
);

CREATE TABLE IF NOT EXISTS customer_department_sql
(
    id               BIGINT             DEFAULT nextval('lov_sequence') PRIMARY KEY,
    name             VARCHAR(255)       
);

CREATE SEQUENCE IF NOT EXISTS report_seq
  START WITH 1 INCREMENT BY 1;

CREATE TABLE IF NOT EXISTS report_nsql (
    id text NOT NULL,
    data jsonb NOT NULL
);

CREATE TABLE IF NOT EXISTS userwidgetpref_nsql (
    id text NOT NULL,
    data jsonb NOT NULL
);

CREATE TABLE if not exists eventpushexception_nsql (
    id TEXT primary key,
    data jsonb NOT NULL
);