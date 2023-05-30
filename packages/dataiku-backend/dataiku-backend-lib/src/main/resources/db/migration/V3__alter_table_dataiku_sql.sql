ALTER TABLE dataiku_sql
ADD COLUMN status text,
ADD COLUMN classification_type text,
ADD COLUMN has_pii boolean default FALSE,
ADD COLUMN division_id text,
ADD COLUMN division_name text,
ADD COLUMN subdivision_id text,
ADD COLUMN subdivision_name text,
ADD COLUMN department text;