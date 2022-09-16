CREATE TABLE if not exists eventpushexception_nsql (
    id TEXT primary key,
    data jsonb NOT NULL
);

CREATE TABLE if not exists department_nsql (
    id TEXT primary key,
    data jsonb NOT NULL
);
