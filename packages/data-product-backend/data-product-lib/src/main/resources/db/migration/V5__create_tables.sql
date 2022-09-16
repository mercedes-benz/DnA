CREATE TABLE if not exists eventpushexception_nsql (
    id TEXT primary key,
    data jsonb NOT NULL
);

CREATE TABLE if not exists department_nsql (
    id TEXT primary key,
    data jsonb NOT NULL
);

CREATE SEQUENCE IF NOT EXISTS data_product_seq
  START WITH 1 INCREMENT BY 1;
  
CREATE TABLE if not exists classification_nsql (
    id TEXT primary key,
    data jsonb NOT NULL
);

CREATE TABLE if not exists legal_basis_nsql (
    id TEXT primary key,
    data jsonb NOT NULL
);