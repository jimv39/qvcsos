DROP DATABASE qvcsedev;

DROP USER qvcsedev;

CREATE USER qvcsedev WITH PASSWORD 'qvcsedevPG$Admin';

-- Database: qvcsedev
CREATE DATABASE qvcsedev
    WITH 
    OWNER = qvcsedev
    ENCODING = 'UTF8'
    LC_COLLATE = 'en_US.UTF-8'
    LC_CTYPE = 'en_US.UTF-8'
    TABLESPACE = pg_default
    CONNECTION LIMIT = -1;

GRANT ALL ON DATABASE qvcsedev TO qvcsedev;

GRANT TEMPORARY, CONNECT ON DATABASE qvcsedev TO PUBLIC;

\c qvcsedev

-- SCHEMA: qvcse
CREATE SCHEMA qvcsedev
    AUTHORIZATION qvcsedev;

-- FUNCTION: qvcsedev.directory_history_trigger()
CREATE FUNCTION qvcsedev.directory_history_trigger()
    RETURNS trigger
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE NOT LEAKPROOF
AS $BODY$
BEGIN
		 INSERT INTO qvcsedev.directory_history(directory_id, root_directory_id, parent_directory_id, branch_id, appended_path, insert_date, update_date, deleted_flag)
		 VALUES(OLD.directory_id, OLD.root_directory_id, OLD.parent_directory_id, OLD.branch_id, OLD.appended_path, OLD.insert_date, OLD.update_date, OLD.deleted_flag);

	RETURN NEW;
END;
$BODY$;

ALTER FUNCTION qvcsedev.directory_history_trigger()
    OWNER TO qvcsedev;


-- FUNCTION: qvcsedev.file_history_trigger()
CREATE FUNCTION qvcsedev.file_history_trigger()
    RETURNS trigger
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE NOT LEAKPROOF
AS $BODY$
BEGIN
		 INSERT INTO qvcsedev.file_history(file_id, branch_id, directory_id, file_name, insert_date, update_date, deleted_flag)
		 VALUES(OLD.file_id, OLD.branch_id, OLD.directory_id, OLD.file_name, OLD.insert_date, OLD.update_date, OLD.deleted_flag);

	RETURN NEW;
END;
$BODY$;

ALTER FUNCTION qvcsedev.file_history_trigger()
    OWNER TO qvcsedev;


-- Table: qvcsedev.branch_type
CREATE TABLE qvcsedev.branch_type
(
    branch_type_id integer NOT NULL,
    branch_type_name character varying(256) COLLATE pg_catalog."default" NOT NULL,
    CONSTRAINT branch_type_pk PRIMARY KEY (branch_type_id)
)

TABLESPACE pg_default;

ALTER TABLE qvcsedev.branch_type
    OWNER to qvcsedev;


-- Table: qvcsedev.project
CREATE TABLE qvcsedev.project
(
    project_id integer NOT NULL GENERATED ALWAYS AS IDENTITY ( INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 2147483647 CACHE 1 ),
    project_name character varying(256) COLLATE pg_catalog."default" NOT NULL,
    insert_date timestamp without time zone NOT NULL,
    CONSTRAINT project_pk PRIMARY KEY (project_id),
    CONSTRAINT project_name_unique UNIQUE (project_name)
)

TABLESPACE pg_default;

ALTER TABLE qvcsedev.project
    OWNER to qvcsedev;


-- Table: qvcsedev.branch
CREATE TABLE qvcsedev.branch
(
    branch_id integer NOT NULL GENERATED ALWAYS AS IDENTITY ( INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 2147483647 CACHE 1 ),
    project_id integer NOT NULL,
    branch_name character varying(256) COLLATE pg_catalog."default" NOT NULL,
    branch_type_id integer NOT NULL,
    insert_date timestamp without time zone NOT NULL,
    CONSTRAINT branch_pk PRIMARY KEY (branch_id),
    CONSTRAINT branch_type_fk FOREIGN KEY (branch_type_id)
        REFERENCES qvcsedev.branch_type (branch_type_id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT project_fk FOREIGN KEY (project_id)
        REFERENCES qvcsedev.project (project_id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
)

TABLESPACE pg_default;

ALTER TABLE qvcsedev.branch
    OWNER to qvcsedev;

-- Index: branch_idx
CREATE UNIQUE INDEX branch_idx
    ON qvcsedev.branch USING btree
    (project_id ASC NULLS LAST, branch_name COLLATE pg_catalog."default" ASC NULLS LAST)
    TABLESPACE pg_default;

-- Table: qvcsedev.commit_history
CREATE TABLE qvcsedev.commit_history
(
    id integer NOT NULL GENERATED ALWAYS AS IDENTITY ( INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 2147483647 CACHE 1 ),
    commit_date timestamp without time zone NOT NULL,
    commit_message character varying(2048) COLLATE pg_catalog."default" NOT NULL,
    CONSTRAINT commit_history_pk PRIMARY KEY (id)
)

TABLESPACE pg_default;

ALTER TABLE qvcsedev.commit_history
    OWNER to qvcsedev;

-- Table: qvcsedev.directory
CREATE TABLE qvcsedev.directory
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
        REFERENCES qvcsedev.branch (branch_id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
)

TABLESPACE pg_default;

ALTER TABLE qvcsedev.directory
    OWNER to qvcsedev;

-- Index: directory_idx
CREATE INDEX directory_idx
    ON qvcsedev.directory USING btree
    (directory_id ASC NULLS LAST)
    TABLESPACE pg_default;

-- Trigger: directory_changes
CREATE TRIGGER directory_changes
    AFTER UPDATE 
    ON qvcsedev.directory
    FOR EACH ROW
    EXECUTE FUNCTION qvcsedev.directory_history_trigger();

-- Table: qvcsedev.directory_history
CREATE TABLE qvcsedev.directory_history
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

ALTER TABLE qvcsedev.directory_history
    OWNER to qvcsedev;

-- Table: qvcsedev.file
CREATE TABLE qvcsedev.file
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
        REFERENCES qvcsedev.branch (branch_id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
)

TABLESPACE pg_default;

ALTER TABLE qvcsedev.file
    OWNER to qvcsedev;

-- Trigger: file_changes
CREATE TRIGGER file_changes
    AFTER UPDATE 
    ON qvcsedev.file
    FOR EACH ROW
    EXECUTE FUNCTION qvcsedev.file_history_trigger();

-- Table: qvcsedev.file_history
CREATE TABLE qvcsedev.file_history
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

ALTER TABLE qvcsedev.file_history
    OWNER to qvcsedev;

-- Table: qvcsedev.promotion_candidate
CREATE TABLE qvcsedev.promotion_candidate
(
    file_id integer NOT NULL,
    branch_id integer NOT NULL,
    insert_date timestamp without time zone NOT NULL,
    CONSTRAINT promotion_pk PRIMARY KEY (file_id, branch_id)
)

TABLESPACE pg_default;

ALTER TABLE qvcsedev.promotion_candidate
    OWNER to qvcsedev;

-- Table: qvcsedev.revision
CREATE TABLE qvcsedev.revision
(
    id integer NOT NULL GENERATED ALWAYS AS IDENTITY ( INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 2147483647 CACHE 1 ),
    branch_id integer NOT NULL,
    file_id integer NOT NULL,
    revision_string character varying(256) COLLATE pg_catalog."default" NOT NULL,
    insert_date timestamp without time zone NOT NULL,
    CONSTRAINT revision_pk PRIMARY KEY (id)
)

TABLESPACE pg_default;

ALTER TABLE qvcsedev.revision
    OWNER to qvcsedev;

-- Insert branch type data
INSERT INTO qvcsedev.branch_type (branch_type_id, branch_type_name) VALUES (1, 'Trunk');
INSERT INTO qvcsedev.branch_type (branch_type_id, branch_type_name) VALUES (2, 'Read Only Date Based Branch');
INSERT INTO qvcsedev.branch_type (branch_type_id, branch_type_name) VALUES (3, 'Feature Branch');
INSERT INTO qvcsedev.branch_type (branch_type_id, branch_type_name) VALUES (4, 'Release Branch');
