-- SEQUENCE: public.dna_project_id_seq

-- DROP SEQUENCE public.dna_project_id_seq;

CREATE SEQUENCE public.dna_project_id_seq
INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 2147483647
    CACHE 1;

ALTER SEQUENCE public.dna_project_id_seq
OWNER TO airflow;

-- SEQUENCE: public.dna_project_user_dag_id_seq

-- DROP SEQUENCE public.dna_project_user_dag_id_seq;

CREATE SEQUENCE public.dna_project_user_dag_id_seq
INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 2147483647
    CACHE 1;

ALTER SEQUENCE public.dna_project_user_dag_id_seq
OWNER TO airflow;

-- SEQUENCE: public.dna_project_user_id_seq

-- DROP SEQUENCE public.dna_project_user_id_seq;

CREATE SEQUENCE public.dna_project_user_id_seq
INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 2147483647
    CACHE 1;

ALTER SEQUENCE public.dna_project_user_id_seq
OWNER TO airflow;



-- Table: public.dna_project_user_dag

-- DROP TABLE public.dna_project_user_dag;

CREATE TABLE public.dna_project_user_dag
(
    id integer NOT NULL DEFAULT nextval('dna_project_user_dag_id_seq'
    ::regclass),
    user_id integer,
    dag_id character varying
    (250) COLLATE pg_catalog."default",
    CONSTRAINT dna_project_user_dag_pkey PRIMARY KEY
    (id),
    CONSTRAINT dna_project_user_dag_dna_project_user_id_dag_id_id_key UNIQUE
    (user_id, dag_id),
    CONSTRAINT dna_project_user_dag_dag_id_fkey FOREIGN KEY
    (dag_id)
        REFERENCES public.dag
    (dag_id) MATCH SIMPLE
        ON
    UPDATE NO ACTION
        ON
    DELETE NO ACTION,
    CONSTRAINT dna_project_user_dag_user_id_fkey
    FOREIGN KEY
    (user_id)
        REFERENCES public.ab_user
    (id) MATCH SIMPLE
        ON
    UPDATE NO ACTION
        ON
    DELETE NO ACTION
        NOT VALID
);
    ALTER TABLE public.dna_project_user_dag OWNER to airflow;

    -- Table: public.dna_project

    -- DROP TABLE public.dna_project;
    CREATE TABLE public.dna_project
    (
        id integer NOT NULL DEFAULT nextval('dna_project_id_seq'
        ::regclass),
    project_id character varying
        (64) COLLATE pg_catalog."default" NOT NULL,
    project_name character varying
        (64) COLLATE pg_catalog."default",
    project_description character varying
        (256) COLLATE pg_catalog."default",
    created_by character varying
        (64) COLLATE pg_catalog."default" NOT NULL,
    CONSTRAINT dna_project_pkey PRIMARY KEY
        (id),
    CONSTRAINT dna_project_project_id_key UNIQUE
        (project_id)
);



        ALTER TABLE public.dna_project OWNER to airflow;

        -- Table: public.dna_project_user

        -- DROP TABLE public.dna_project_user;

        CREATE TABLE public.dna_project_user
        (
            id integer NOT NULL DEFAULT nextval('dna_project_user_id_seq'
            ::regclass),
    user_dag_id integer,
    dna_project_id integer,
    CONSTRAINT dna_project_user_pkey PRIMARY KEY
            (id),
    CONSTRAINT dna_project_user_user_id_dna_project_id_key UNIQUE
            (user_dag_id, dna_project_id),
    CONSTRAINT dna_project_user_dag_fkey FOREIGN KEY
            (user_dag_id)
        REFERENCES public.dna_project_user_dag
            (id) MATCH SIMPLE
        ON
            UPDATE NO ACTION
        ON
            DELETE NO ACTION
        NOT VALID,
    CONSTRAINT dna_project_user_dna_project_id_fkey
            FOREIGN KEY
            (dna_project_id)
        REFERENCES public.dna_project
            (id) MATCH SIMPLE
        ON
            UPDATE NO ACTION
        ON
            DELETE NO ACTION
);

            ALTER TABLE public.dna_project_user OWNER to airflow;

CREATE SEQUENCE public.dna_project_id_gen_seq
INCREMENT 1
START 1
MINVALUE 1
MAXVALUE 2147483647
CACHE 1;

ALTER SEQUENCE public.dna_project_id_gen_seq
OWNER TO airflow;

       