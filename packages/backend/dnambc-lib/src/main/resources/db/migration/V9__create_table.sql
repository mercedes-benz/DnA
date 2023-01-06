CREATE TABLE if not exists department_nsql (
    id TEXT primary key,
    data jsonb NOT NULL
);

CREATE TABLE if not exists customerjourneyphase_nsql (
    id TEXT primary key,
    data jsonb NOT NULL
);

CREATE TABLE if not exists marketingcommunicationchannel_nsql (
    id TEXT primary key,
    data jsonb NOT NULL
);
