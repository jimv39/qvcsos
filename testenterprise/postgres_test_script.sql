DROP DATABASE qvcsetest;

DROP USER qvcsetest;

CREATE USER qvcsetest WITH PASSWORD 'qvcsetestPG$Admin';

-- Database: qvcsetest
CREATE DATABASE qvcsetest
    WITH 
    OWNER = qvcsetest
    ENCODING = 'UTF8'
    LC_COLLATE = 'en_US.UTF-8'
    LC_CTYPE = 'en_US.UTF-8'
    TABLESPACE = pg_default
    CONNECTION LIMIT = -1;

GRANT ALL ON DATABASE qvcsetest TO qvcsetest;

GRANT TEMPORARY, CONNECT ON DATABASE qvcsetest TO PUBLIC;

\c qvcsetest

-- SCHEMA: qvcse
CREATE SCHEMA qvcsetest
    AUTHORIZATION qvcsetest;

-- FUNCTION: qvcsetest.directory_history_trigger()
CREATE FUNCTION qvcsetest.directory_history_trigger()
    RETURNS trigger
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE NOT LEAKPROOF
AS $BODY$
BEGIN
		 INSERT INTO qvcsetest.directory_history(directory_id, root_directory_id, parent_directory_id, branch_id, appended_path, insert_date, update_date, deleted_flag)
		 VALUES(OLD.directory_id, OLD.root_directory_id, OLD.parent_directory_id, OLD.branch_id, OLD.appended_path, OLD.insert_date, OLD.update_date, OLD.deleted_flag);

	RETURN NEW;
END;
$BODY$;

ALTER FUNCTION qvcsetest.directory_history_trigger()
    OWNER TO qvcsetest;


-- FUNCTION: qvcsetest.file_history_trigger()
CREATE FUNCTION qvcsetest.file_history_trigger()
    RETURNS trigger
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE NOT LEAKPROOF
AS $BODY$
BEGIN
		 INSERT INTO qvcsetest.file_history(file_id, branch_id, directory_id, file_name, insert_date, update_date, deleted_flag)
		 VALUES(OLD.file_id, OLD.branch_id, OLD.directory_id, OLD.file_name, OLD.insert_date, OLD.update_date, OLD.deleted_flag);

	RETURN NEW;
END;
$BODY$;

ALTER FUNCTION qvcsetest.file_history_trigger()
    OWNER TO qvcsetest;


-- Table: qvcsetest.branch_type
CREATE TABLE qvcsetest.branch_type
(
    branch_type_id integer NOT NULL,
    branch_type_name character varying(256) COLLATE pg_catalog."default" NOT NULL,
    CONSTRAINT branch_type_pk PRIMARY KEY (branch_type_id)
)

TABLESPACE pg_default;

ALTER TABLE qvcsetest.branch_type
    OWNER to qvcsetest;


-- Table: qvcsetest.project
CREATE TABLE qvcsetest.project
(
    project_id integer NOT NULL GENERATED ALWAYS AS IDENTITY ( INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 2147483647 CACHE 1 ),
    project_name character varying(256) COLLATE pg_catalog."default" NOT NULL,
    insert_date timestamp without time zone NOT NULL,
    CONSTRAINT project_pk PRIMARY KEY (project_id),
    CONSTRAINT project_name_unique UNIQUE (project_name)
)

TABLESPACE pg_default;

ALTER TABLE qvcsetest.project
    OWNER to qvcsetest;


-- Table: qvcsetest.branch
CREATE TABLE qvcsetest.branch
(
    branch_id integer NOT NULL GENERATED ALWAYS AS IDENTITY ( INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 2147483647 CACHE 1 ),
    project_id integer NOT NULL,
    branch_name character varying(256) COLLATE pg_catalog."default" NOT NULL,
    branch_type_id integer NOT NULL,
    insert_date timestamp without time zone NOT NULL,
    CONSTRAINT branch_pk PRIMARY KEY (branch_id),
    CONSTRAINT branch_type_fk FOREIGN KEY (branch_type_id)
        REFERENCES qvcsetest.branch_type (branch_type_id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT project_fk FOREIGN KEY (project_id)
        REFERENCES qvcsetest.project (project_id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
)

TABLESPACE pg_default;

ALTER TABLE qvcsetest.branch
    OWNER to qvcsetest;

-- Index: branch_idx
CREATE UNIQUE INDEX branch_idx
    ON qvcsetest.branch USING btree
    (project_id ASC NULLS LAST, branch_name COLLATE pg_catalog."default" ASC NULLS LAST)
    TABLESPACE pg_default;

-- Table: qvcsetest.commit_history
CREATE TABLE qvcsetest.commit_history
(
    id integer NOT NULL GENERATED ALWAYS AS IDENTITY ( INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 2147483647 CACHE 1 ),
    commit_date timestamp without time zone NOT NULL,
    commit_message character varying(2048) COLLATE pg_catalog."default" NOT NULL,
    CONSTRAINT commit_history_pk PRIMARY KEY (id)
)

TABLESPACE pg_default;

ALTER TABLE qvcsetest.commit_history
    OWNER to qvcsetest;

-- Table: qvcsetest.directory
CREATE TABLE qvcsetest.directory
(
    directory_id integer NOT NULL,
    root_directory_id integer NOT NULL,
    parent_directory_id integer,
    branch_id integer NOT NULL,
    appended_path character varying(2048) COLLATE pg_catalog."default" NOT NULL,
    insert_date timestamp without time zone NOT NULL,
    update_date timestamp without time zone NOT NULL,
    deleted_flag boolean NOT NULL,
    CONSTRAINT directory_pk PRIMARY KEY (directory_id, branch_id, deleted_flag),
    CONSTRAINT branch_fk FOREIGN KEY (branch_id)
        REFERENCES qvcsetest.branch (branch_id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
)

TABLESPACE pg_default;

ALTER TABLE qvcsetest.directory
    OWNER to qvcsetest;

-- Index: directory_idx
CREATE INDEX directory_idx
    ON qvcsetest.directory USING btree
    (directory_id ASC NULLS LAST)
    TABLESPACE pg_default;

-- Trigger: directory_changes
CREATE TRIGGER directory_changes
    AFTER UPDATE 
    ON qvcsetest.directory
    FOR EACH ROW
    EXECUTE FUNCTION qvcsetest.directory_history_trigger();

-- Table: qvcsetest.directory_history
CREATE TABLE qvcsetest.directory_history
(
    id integer NOT NULL GENERATED ALWAYS AS IDENTITY ( INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 2147483647 CACHE 1 ),
    directory_id integer NOT NULL,
    root_directory_id integer NOT NULL,
    parent_directory_id integer,
    branch_id integer NOT NULL,
    appended_path character varying(2048) COLLATE pg_catalog."default" NOT NULL,
    insert_date timestamp without time zone NOT NULL,
    update_date timestamp without time zone NOT NULL,
    deleted_flag boolean NOT NULL,
    CONSTRAINT directory_history_pk PRIMARY KEY (id)
)

TABLESPACE pg_default;

ALTER TABLE qvcsetest.directory_history
    OWNER to qvcsetest;

-- Table: qvcsetest.file
CREATE TABLE qvcsetest.file
(
    file_id integer NOT NULL,
    branch_id integer NOT NULL,
    directory_id integer NOT NULL,
    file_name character varying(256) COLLATE pg_catalog."default" NOT NULL,
    insert_date timestamp without time zone NOT NULL,
    update_date timestamp without time zone NOT NULL,
    deleted_flag boolean NOT NULL,
    CONSTRAINT file_pk PRIMARY KEY (file_id, branch_id, deleted_flag),
    CONSTRAINT branch_fk2 FOREIGN KEY (branch_id)
        REFERENCES qvcsetest.branch (branch_id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
)

TABLESPACE pg_default;

ALTER TABLE qvcsetest.file
    OWNER to qvcsetest;

-- Trigger: file_changes
CREATE TRIGGER file_changes
    AFTER UPDATE 
    ON qvcsetest.file
    FOR EACH ROW
    EXECUTE FUNCTION qvcsetest.file_history_trigger();

-- Table: qvcsetest.file_history
CREATE TABLE qvcsetest.file_history
(
    id integer NOT NULL GENERATED ALWAYS AS IDENTITY ( INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 2147483647 CACHE 1 ),
    file_id integer,
    branch_id integer NOT NULL,
    directory_id integer NOT NULL,
    file_name character varying(256) COLLATE pg_catalog."default" NOT NULL,
    insert_date timestamp without time zone NOT NULL,
    update_date timestamp without time zone NOT NULL,
    deleted_flag boolean NOT NULL,
    CONSTRAINT id_pk PRIMARY KEY (id)
)

TABLESPACE pg_default;

ALTER TABLE qvcsetest.file_history
    OWNER to qvcsetest;

-- Table: qvcsetest.promotion_candidate
CREATE TABLE qvcsetest.promotion_candidate
(
    file_id integer NOT NULL,
    branch_id integer NOT NULL,
    insert_date timestamp without time zone NOT NULL,
    CONSTRAINT promotion_pk PRIMARY KEY (file_id, branch_id)
)

TABLESPACE pg_default;

ALTER TABLE qvcsetest.promotion_candidate
    OWNER to qvcsetest;

-- Table: qvcsetest.revision
CREATE TABLE qvcsetest.revision
(
    id integer NOT NULL GENERATED ALWAYS AS IDENTITY ( INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 2147483647 CACHE 1 ),
    branch_id integer NOT NULL,
    file_id integer NOT NULL,
    revision_string character varying(256) COLLATE pg_catalog."default" NOT NULL,
    insert_date timestamp without time zone NOT NULL,
    CONSTRAINT revision_pk PRIMARY KEY (id)
)

TABLESPACE pg_default;

ALTER TABLE qvcsetest.revision
    OWNER to qvcsetest;

-- Insert branch type data
INSERT INTO qvcsetest.branch_type (branch_type_id, branch_type_name) VALUES (1, 'Trunk');
INSERT INTO qvcsetest.branch_type (branch_type_id, branch_type_name) VALUES (2, 'Read Only Date Based Branch');
INSERT INTO qvcsetest.branch_type (branch_type_id, branch_type_name) VALUES (3, 'Feature Branch');
INSERT INTO qvcsetest.branch_type (branch_type_id, branch_type_name) VALUES (4, 'Release Branch');
