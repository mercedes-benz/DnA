CREATE TABLE if not exists analyticsSolution_nsql (
    id TEXT primary key,
    data jsonb NOT NULL
);

insert into analyticsSolution_nsql (id, data)
values('1','{"name": "analyticsSolution1"}'),
('2','{"name": "analyticsSolution2"}');