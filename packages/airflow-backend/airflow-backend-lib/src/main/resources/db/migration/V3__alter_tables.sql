--
-- add new columns in dna_project for storing work tables
--



ALTER TABLE dna_project
  ADD COLUMN project_status text;
  
ALTER TABLE dna_project
  ADD COLUMN collabs text;
  
--
-- alter Script end
--