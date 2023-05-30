CREATE TABLE IF NOT EXISTS kpi_classification_sql
(
    id               BIGINT             DEFAULT nextval('lov_sequence') PRIMARY KEY,
    name             VARCHAR(255)       
);

INSERT INTO kpi_classification_sql (id, name) VALUES
(DEFAULT,'TEST');

-- 
-- 

ALTER TABLE kpi_name_sql
ADD classification varchar(255);
