
CREATE SEQUENCE IF NOT EXISTS dashboard_sequence
  START WITH 1000 INCREMENT BY 1;
  

CREATE TABLE IF NOT EXISTS common_function_sql
(
    id               BIGINT             DEFAULT nextval('dashboard_sequence') PRIMARY KEY,
    name             VARCHAR(255)       
);

CREATE TABLE IF NOT EXISTS data_source_sql
(
    id               BIGINT             DEFAULT nextval('dashboard_sequence') PRIMARY KEY,
    name             VARCHAR(255)       
);

CREATE TABLE IF NOT EXISTS department_sql
(
    id               BIGINT             DEFAULT nextval('dashboard_sequence') PRIMARY KEY,
    name             VARCHAR(255)       
);

CREATE TABLE IF NOT EXISTS frontend_technology_sql
(
    id               BIGINT             DEFAULT nextval('dashboard_sequence') PRIMARY KEY,
    name             VARCHAR(255)       
);

CREATE TABLE IF NOT EXISTS hierarchy_sql
(
    id               BIGINT             DEFAULT nextval('dashboard_sequence') PRIMARY KEY,
    name             VARCHAR(255)       
);

CREATE TABLE IF NOT EXISTS integrated_portal_sql
(
    id               BIGINT             DEFAULT nextval('dashboard_sequence') PRIMARY KEY,
    name             VARCHAR(255)       
);

CREATE TABLE IF NOT EXISTS kpi_name_sql
(
    id               BIGINT             DEFAULT nextval('dashboard_sequence') PRIMARY KEY,
    name             VARCHAR(255)       
);

CREATE TABLE IF NOT EXISTS product_phase_sql
(
    id               BIGINT             DEFAULT nextval('dashboard_sequence') PRIMARY KEY,
    name             VARCHAR(255)       
);

CREATE TABLE IF NOT EXISTS query_sql
(
    id               BIGINT             DEFAULT nextval('dashboard_sequence') PRIMARY KEY,
    name             VARCHAR(255)       
);

CREATE TABLE IF NOT EXISTS reporting_cause_sql
(
    id               BIGINT             DEFAULT nextval('dashboard_sequence') PRIMARY KEY,
    name             VARCHAR(255)       
);

CREATE TABLE IF NOT EXISTS ressort_sql
(
    id               BIGINT             DEFAULT nextval('dashboard_sequence') PRIMARY KEY,
    name             VARCHAR(255)       
);

CREATE TABLE IF NOT EXISTS specific_function_sql
(
    id               BIGINT             DEFAULT nextval('dashboard_sequence') PRIMARY KEY,
    name             VARCHAR(255)       
);

CREATE TABLE IF NOT EXISTS status_sql
(
    id               BIGINT             DEFAULT nextval('dashboard_sequence') PRIMARY KEY,
    name             VARCHAR(255)       
);

CREATE TABLE IF NOT EXISTS design_guide_sql
(
    id               BIGINT             DEFAULT nextval('dashboard_sequence') PRIMARY KEY,
    name             VARCHAR(255)       
);

CREATE TABLE IF NOT EXISTS agile_releasetrain_sql
(
    id               BIGINT             DEFAULT nextval('dashboard_sequence') PRIMARY KEY,
    name             VARCHAR(255)     
);

CREATE TABLE IF NOT EXISTS subsystem_sql
(
    id               BIGINT             DEFAULT nextval('dashboard_sequence') PRIMARY KEY,
    name             VARCHAR(255)     
);

CREATE TABLE IF NOT EXISTS connection_type_sql
(
    id               BIGINT             DEFAULT nextval('dashboard_sequence') PRIMARY KEY,
    name             VARCHAR(255)     
);

CREATE TABLE IF NOT EXISTS datawarehouse_nsql (
    id text NOT NULL,
    data jsonb NOT NULL
);

CREATE TABLE IF NOT EXISTS tag_sql
(
    id               BIGINT             DEFAULT nextval('dashboard_sequence') PRIMARY KEY,
    name             VARCHAR(255)       
);

CREATE TABLE IF NOT EXISTS report_nsql (
    id text NOT NULL,
    data jsonb NOT NULL
);

CREATE TABLE IF NOT EXISTS customer_department_sql
(
    id               BIGINT             DEFAULT nextval('dashboard_sequence') PRIMARY KEY,
    name             VARCHAR(255)       
);

CREATE TABLE IF NOT EXISTS userwidgetpref_nsql (
    id text NOT NULL,
    data jsonb NOT NULL
);

