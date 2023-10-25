CREATE TABLE if not exists analyticssolution_nsql (
    id TEXT primary key,
    data jsonb NOT NULL
);

insert into analyticssolution_nsql (id, data)
values('1','{"name": "analyticssolution1"}'),
('2','{"name": "analyticssolution2"}');