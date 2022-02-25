--
-- PostgreSQL database dump
--

-- Dumped from database version 10.3
-- Dumped by pg_dump version 10.3

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: dai_admin; Type: SCHEMA; Schema: -; Owner: postgres
--

CREATE SCHEMA dai_admin;


ALTER SCHEMA dai_admin OWNER TO admin;

--
-- Name: plpgsql; Type: EXTENSION; Schema: -; Owner: 
--

CREATE EXTENSION IF NOT EXISTS plpgsql WITH SCHEMA pg_catalog;


--
-- Name: EXTENSION plpgsql; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION plpgsql IS 'PL/pgSQL procedural language';


--
-- Name: pg_stat_statements; Type: EXTENSION; Schema: -; Owner: 
--

CREATE EXTENSION IF NOT EXISTS pg_stat_statements WITH SCHEMA public;


--
-- Name: EXTENSION pg_stat_statements; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION pg_stat_statements IS 'track execution statistics of all SQL statements executed';


--
-- Name: pgcrypto; Type: EXTENSION; Schema: -; Owner: 
--

CREATE EXTENSION IF NOT EXISTS pgcrypto WITH SCHEMA public;


--
-- Name: EXTENSION pgcrypto; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION pgcrypto IS 'cryptographic functions';


SET default_tablespace = '';

SET default_with_oids = false;

--
-- Name: parameters; Type: TABLE; Schema: dai_admin; Owner: postgres
--

CREATE TABLE dai_admin.parameters (
    key character varying(500),
    val character varying(500)
);


ALTER TABLE dai_admin.parameters OWNER TO admin;

--
-- Name: parameters_history; Type: TABLE; Schema: dai_admin; Owner: postgres
--

CREATE TABLE dai_admin.parameters_history (
    ksi character varying(10),
    key character varying(30),
    val character varying(10),
    "timestamp" timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


ALTER TABLE dai_admin.parameters_history OWNER TO admin;

--
-- Name: algorithm_nsql; Type: TABLE; Schema: public; Owner: admin
--

CREATE TABLE public.algorithm_nsql (
    id text NOT NULL PRIMARY KEY,
    data jsonb NOT NULL
);


ALTER TABLE public.algorithm_nsql OWNER TO admin;

--
-- Name: appsubscription_nsql; Type: TABLE; Schema: public; Owner: admin
--

CREATE TABLE public.appsubscription_nsql (
    id text NOT NULL PRIMARY KEY,
    data jsonb NOT NULL
);


ALTER TABLE public.appsubscription_nsql OWNER TO admin;

--
-- Name: benefitrelevance_nsql; Type: TABLE; Schema: public; Owner: admin
--

CREATE TABLE public.benefitrelevance_nsql (
    id text NOT NULL PRIMARY KEY,
    data jsonb NOT NULL
);


ALTER TABLE public.benefitrelevance_nsql OWNER TO admin;

--
-- Name: businessgoal_nsql; Type: TABLE; Schema: public; Owner: admin
--

CREATE TABLE public.businessgoal_nsql (
    id text NOT NULL PRIMARY KEY,
    data jsonb NOT NULL
);


ALTER TABLE public.businessgoal_nsql OWNER TO admin;

--
-- Name: category_nsql; Type: TABLE; Schema: public; Owner: admin
--

CREATE TABLE public.category_nsql (
    id text NOT NULL PRIMARY KEY,
    data jsonb NOT NULL
);


ALTER TABLE public.category_nsql OWNER TO admin;

--
-- Name: dataiku_nsql; Type: TABLE; Schema: public; Owner: admin
--

CREATE TABLE public.dataiku_nsql (
    id text NOT NULL PRIMARY KEY,
    data jsonb NOT NULL
);


ALTER TABLE public.dataiku_nsql OWNER TO admin;

--
-- Name: datasource_nsql; Type: TABLE; Schema: public; Owner: admin
--

CREATE TABLE public.datasource_nsql (
    id text NOT NULL PRIMARY KEY,
    data jsonb NOT NULL
);


ALTER TABLE public.datasource_nsql OWNER TO admin;

--
-- Name: datavolume_nsql; Type: TABLE; Schema: public; Owner: admin
--

CREATE TABLE public.datavolume_nsql (
    id text NOT NULL PRIMARY KEY,
    data jsonb NOT NULL
);


ALTER TABLE public.datavolume_nsql OWNER TO admin;

--
-- Name: division_nsql; Type: TABLE; Schema: public; Owner: admin
--

CREATE TABLE public.division_nsql (
    id text NOT NULL PRIMARY KEY,
    data jsonb NOT NULL
);


ALTER TABLE public.division_nsql OWNER TO admin;

--
-- Name: itsmmgameeventdetails_nsql; Type: TABLE; Schema: public; Owner: admin
--

CREATE TABLE public.itsmmgameeventdetails_nsql (
    id text NOT NULL PRIMARY KEY,
    data jsonb NOT NULL
);


ALTER TABLE public.itsmmgameeventdetails_nsql OWNER TO admin;

--
-- Name: itsmmgameuserdetails_nsql; Type: TABLE; Schema: public; Owner: admin
--

CREATE TABLE public.itsmmgameuserdetails_nsql (
    id text NOT NULL PRIMARY KEY,
    data jsonb NOT NULL
);


ALTER TABLE public.itsmmgameuserdetails_nsql OWNER TO admin;

--
-- Name: language_nsql; Type: TABLE; Schema: public; Owner: admin
--

CREATE TABLE public.language_nsql (
    id text NOT NULL PRIMARY KEY,
    data jsonb NOT NULL
);


ALTER TABLE public.language_nsql OWNER TO admin;

--
-- Name: location_nsql; Type: TABLE; Schema: public; Owner: admin
--

CREATE TABLE public.location_nsql (
    id text NOT NULL PRIMARY KEY,
    data jsonb NOT NULL
);


ALTER TABLE public.location_nsql OWNER TO admin;

--
-- Name: maturitylevel_nsql; Type: TABLE; Schema: public; Owner: admin
--

CREATE TABLE public.maturitylevel_nsql (
    id text NOT NULL PRIMARY KEY,
    data jsonb NOT NULL
);


ALTER TABLE public.maturitylevel_nsql OWNER TO admin;

--
-- Name: notebook_nsql; Type: TABLE; Schema: public; Owner: admin
--

CREATE TABLE public.notebook_nsql (
    id text NOT NULL PRIMARY KEY,
    data jsonb NOT NULL
);


ALTER TABLE public.notebook_nsql OWNER TO admin;

--
-- Name: phase_nsql; Type: TABLE; Schema: public; Owner: admin
--

CREATE TABLE public.phase_nsql (
    id text NOT NULL PRIMARY KEY,
    data jsonb NOT NULL
);


ALTER TABLE public.phase_nsql OWNER TO admin;

--
-- Name: platform_nsql; Type: TABLE; Schema: public; Owner: admin
--

CREATE TABLE public.platform_nsql (
    id text NOT NULL PRIMARY KEY,
    data jsonb NOT NULL
);


ALTER TABLE public.platform_nsql OWNER TO admin;

--
-- Name: projectstatus_nsql; Type: TABLE; Schema: public; Owner: admin
--

CREATE TABLE public.projectstatus_nsql (
    id text NOT NULL PRIMARY KEY,
    data jsonb NOT NULL
);


ALTER TABLE public.projectstatus_nsql OWNER TO admin;

--
-- Name: relatedproduct_nsql; Type: TABLE; Schema: public; Owner: admin
--

CREATE TABLE public.relatedproduct_nsql (
    id text NOT NULL PRIMARY KEY,
    data jsonb NOT NULL
);


ALTER TABLE public.relatedproduct_nsql OWNER TO admin;

--
-- Name: result_nsql; Type: TABLE; Schema: public; Owner: admin
--

CREATE TABLE public.result_nsql (
    id text NOT NULL PRIMARY KEY,
    data jsonb NOT NULL
);


ALTER TABLE public.result_nsql OWNER TO admin;

--
-- Name: solution_nsql; Type: TABLE; Schema: public; Owner: admin
--

CREATE TABLE public.solution_nsql (
    id text NOT NULL PRIMARY KEY,
    data jsonb NOT NULL
);


ALTER TABLE public.solution_nsql OWNER TO admin;

--
-- Name: strategicrelevance_nsql; Type: TABLE; Schema: public; Owner: admin
--

CREATE TABLE public.strategicrelevance_nsql (
    id text NOT NULL PRIMARY KEY,
    data jsonb NOT NULL
);


ALTER TABLE public.strategicrelevance_nsql OWNER TO admin;

--
-- Name: tag_nsql; Type: TABLE; Schema: public; Owner: admin
--

CREATE TABLE public.tag_nsql (
    id text NOT NULL PRIMARY KEY,
    data jsonb NOT NULL
);


ALTER TABLE public.tag_nsql OWNER TO admin;

--
-- Name: topic_nsql; Type: TABLE; Schema: public; Owner: admin
--

CREATE TABLE public.topic_nsql (
    id text NOT NULL PRIMARY KEY,
    data jsonb NOT NULL
);


ALTER TABLE public.topic_nsql OWNER TO admin;

--
-- Name: userinfo_nsql; Type: TABLE; Schema: public; Owner: admin
--

CREATE TABLE public.userinfo_nsql (
    id text NOT NULL PRIMARY KEY,
    data jsonb NOT NULL,
    is_logged_in character varying,
    token character varying
);


ALTER TABLE public.userinfo_nsql OWNER TO admin;

--
-- Name: userrole_nsql; Type: TABLE; Schema: public; Owner: admin
--

CREATE TABLE public.userrole_nsql (
    id text NOT NULL PRIMARY KEY,
    data jsonb NOT NULL
);


ALTER TABLE public.userrole_nsql OWNER TO admin;

--
-- Name: userwidgetpref_nsql; Type: TABLE; Schema: public; Owner: admin
--

CREATE TABLE public.userwidgetpref_nsql (
    id text NOT NULL PRIMARY KEY,
    data jsonb NOT NULL
);


ALTER TABLE public.userwidgetpref_nsql OWNER TO admin;

--
-- Name: visualization_nsql; Type: TABLE; Schema: public; Owner: admin
--

CREATE TABLE public.visualization_nsql (
    id text NOT NULL PRIMARY KEY,
    data jsonb NOT NULL
);


ALTER TABLE public.visualization_nsql OWNER TO admin;

--
-- Name: widget_nsql; Type: TABLE; Schema: public; Owner: admin
--

CREATE TABLE public.widget_nsql (
    id text NOT NULL PRIMARY KEY,
    data jsonb NOT NULL
);


ALTER TABLE public.widget_nsql OWNER TO admin;

--
-- DDL Script start
--

create table public.datastrategydomain_nsql
(
id TEXT primary key,
data jsonb not null
);

create table public.skill_nsql
(
id TEXT primary key,
data jsonb not null
);

--
-- DDL Script end
--
