CREATE TABLE if not exists additionalresources_nsql (
    id TEXT primary key,
    data jsonb NOT NULL
);

CREATE TABLE if not exists algorithm_nsql (
    id TEXT primary key,
    data jsonb NOT NULL
);

CREATE TABLE if not exists appsubscription_nsql (
    id TEXT primary key,
    data jsonb NOT NULL
);

CREATE TABLE if not exists benefitrelevance_nsql (
    id TEXT primary key,
    data jsonb NOT NULL
);

CREATE TABLE if not exists businessgoal_nsql (
    id TEXT primary key,
    data jsonb NOT NULL
);

CREATE TABLE if not exists category_nsql (
    id TEXT primary key,
    data jsonb NOT NULL
);

CREATE TABLE if not exists dataiku_nsql (
    id TEXT primary key,
    data jsonb NOT NULL
);

CREATE TABLE if not exists datasource_nsql (
    id TEXT primary key,
    data jsonb NOT NULL
);

CREATE TABLE if not exists datastrategydomain_nsql (
    id TEXT primary key,
    data jsonb NOT NULL
);

CREATE TABLE if not exists datavolume_nsql (
    id TEXT primary key,
    data jsonb NOT NULL
);

CREATE TABLE if not exists division_nsql (
    id TEXT primary key,
    data jsonb NOT NULL
);

CREATE TABLE if not exists itsmmgameeventdetails_nsql (
    id TEXT primary key,
    data jsonb NOT NULL
);

CREATE TABLE if not exists itsmmgameuserdetails_nsql (
    id TEXT primary key,
    data jsonb NOT NULL
);

CREATE TABLE if not exists language_nsql (
    id TEXT primary key,
    data jsonb NOT NULL
);

CREATE TABLE if not exists location_nsql (
    id TEXT primary key,
    data jsonb NOT NULL
);

CREATE TABLE if not exists maturitylevel_nsql (
    id TEXT primary key,
    data jsonb NOT NULL
);

CREATE TABLE if not exists notebook_nsql (
    id TEXT primary key,
    data jsonb NOT NULL
);

CREATE TABLE if not exists phase_nsql (
    id TEXT primary key,
    data jsonb NOT NULL
);

CREATE TABLE if not exists platform_nsql (
    id TEXT primary key,
    data jsonb NOT NULL
);

CREATE TABLE if not exists projectstatus_nsql (
    id TEXT primary key,
    data jsonb NOT NULL
);

CREATE TABLE if not exists relatedproduct_nsql (
    id TEXT primary key,
    data jsonb NOT NULL
);

CREATE TABLE if not exists result_nsql (
    id TEXT primary key,
    data jsonb NOT NULL
);

CREATE TABLE if not exists skill_nsql (
    id TEXT primary key,
    data jsonb NOT NULL
);

CREATE TABLE if not exists solution_nsql (
    id TEXT primary key,
    data jsonb NOT NULL
);

CREATE TABLE if not exists strategicrelevance_nsql (
    id TEXT primary key,
    data jsonb NOT NULL
);

CREATE TABLE if not exists tag_nsql (
    id TEXT primary key,
    data jsonb NOT NULL
);

CREATE TABLE if not exists topic_nsql (
    id TEXT primary key,
    data jsonb NOT NULL
);

CREATE TABLE if not exists userinfo_nsql (
    id TEXT primary key,
    data jsonb NOT NULL,
    is_logged_in character varying
);

CREATE TABLE if not exists usernotificationpref_nsql (
    id TEXT primary key,
    data jsonb NOT NULL
);

CREATE TABLE if not exists userrole_nsql (
    id TEXT primary key,
    data jsonb NOT NULL
);

CREATE TABLE if not exists userwidgetpref_nsql (
    id TEXT primary key,
    data jsonb NOT NULL
);

CREATE TABLE if not exists visualization_nsql (
    id TEXT primary key,
    data jsonb NOT NULL
);

CREATE TABLE if not exists widget_nsql (
    id TEXT primary key,
    data jsonb NOT NULL
);