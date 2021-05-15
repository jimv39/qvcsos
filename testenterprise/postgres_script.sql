DROP DATABASE qvcse;

DROP USER qvcse;

CREATE USER qvcse WITH PASSWORD 'qvcsePG$Admin';

-- Database: qvcse
CREATE DATABASE qvcse
    WITH 
    OWNER = qvcse
    ENCODING = 'UTF8'
    LC_COLLATE = 'en_US.UTF-8'
    LC_CTYPE = 'en_US.UTF-8'
    TABLESPACE = pg_default
    CONNECTION LIMIT = -1;

GRANT ALL ON DATABASE qvcse TO qvcse;

GRANT TEMPORARY, CONNECT ON DATABASE qvcse TO PUBLIC;

\c qvcse

-- SCHEMA: qvcse
CREATE SCHEMA qvcse
    AUTHORIZATION qvcse;

-- FUNCTION: qvcse.directory_history_trigger()
CREATE FUNCTION qvcse.directory_history_trigger()
    RETURNS trigger
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE NOT LEAKPROOF
AS $BODY$
BEGIN
		 INSERT INTO qvcse.directory_history(directory_id, root_directory_id, parent_directory_id, branch_id, appended_path, insert_date, update_date, deleted_flag)
		 VALUES(OLD.directory_id, OLD.root_directory_id, OLD.parent_directory_id, OLD.branch_id, OLD.appended_path, OLD.insert_date, OLD.update_date, OLD.deleted_flag);

	RETURN NEW;
END;
$BODY$;

ALTER FUNCTION qvcse.directory_history_trigger()
    OWNER TO qvcse;


-- FUNCTION: qvcse.file_history_trigger()
CREATE FUNCTION qvcse.file_history_trigger()
    RETURNS trigger
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE NOT LEAKPROOF
AS $BODY$
BEGIN
		 INSERT INTO qvcse.file_history(file_id, branch_id, directory_id, file_name, insert_date, update_date, deleted_flag)
		 VALUES(OLD.file_id, OLD.branch_id, OLD.directory_id, OLD.file_name, OLD.insert_date, OLD.update_date, OLD.deleted_flag);

	RETURN NEW;
END;
$BODY$;

ALTER FUNCTION qvcse.file_history_trigger()
    OWNER TO qvcse;


-- Table: qvcse.branch_type
CREATE TABLE qvcse.branch_type
(
    branch_type_id integer NOT NULL,
    branch_type_name character varying(256) COLLATE pg_catalog."default" NOT NULL,
    CONSTRAINT branch_type_pk PRIMARY KEY (branch_type_id)
)

TABLESPACE pg_default;

ALTER TABLE qvcse.branch_type
    OWNER to qvcse;


-- Table: qvcse.project
CREATE TABLE qvcse.project
(
    project_id integer NOT NULL GENERATED ALWAYS AS IDENTITY ( INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 2147483647 CACHE 1 ),
    project_name character varying(256) COLLATE pg_catalog."default" NOT NULL,
    insert_date timestamp without time zone NOT NULL,
    CONSTRAINT project_pk PRIMARY KEY (project_id),
    CONSTRAINT project_name_unique UNIQUE (project_name)
)

TABLESPACE pg_default;

ALTER TABLE qvcse.project
    OWNER to qvcse;


-- Table: qvcse.branch
CREATE TABLE qvcse.branch
(
    branch_id integer NOT NULL GENERATED ALWAYS AS IDENTITY ( INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 2147483647 CACHE 1 ),
    project_id integer NOT NULL,
    branch_name character varying(256) COLLATE pg_catalog."default" NOT NULL,
    branch_type_id integer NOT NULL,
    insert_date timestamp without time zone NOT NULL,
    CONSTRAINT branch_pk PRIMARY KEY (branch_id),
    CONSTRAINT branch_type_fk FOREIGN KEY (branch_type_id)
        REFERENCES qvcse.branch_type (branch_type_id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT project_fk FOREIGN KEY (project_id)
        REFERENCES qvcse.project (project_id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
)

TABLESPACE pg_default;

ALTER TABLE qvcse.branch
    OWNER to qvcse;

-- Index: branch_idx
CREATE UNIQUE INDEX branch_idx
    ON qvcse.branch USING btree
    (project_id ASC NULLS LAST, branch_name COLLATE pg_catalog."default" ASC NULLS LAST)
    TABLESPACE pg_default;

-- Table: qvcse.commit_history
CREATE TABLE qvcse.commit_history
(
    id integer NOT NULL GENERATED ALWAYS AS IDENTITY ( INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 2147483647 CACHE 1 ),
    commit_date timestamp without time zone NOT NULL,
    commit_message character varying(2048) COLLATE pg_catalog."default" NOT NULL,
    CONSTRAINT commit_history_pk PRIMARY KEY (id)
)

TABLESPACE pg_default;

ALTER TABLE qvcse.commit_history
    OWNER to qvcse;

-- Table: qvcse.directory
CREATE TABLE qvcse.directory
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
        REFERENCES qvcse.branch (branch_id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
)

TABLESPACE pg_default;

ALTER TABLE qvcse.directory
    OWNER to qvcse;

-- Index: directory_idx
CREATE INDEX directory_idx
    ON qvcse.directory USING btree
    (directory_id ASC NULLS LAST)
    TABLESPACE pg_default;

-- Trigger: directory_changes
CREATE TRIGGER directory_changes
    AFTER UPDATE 
    ON qvcse.directory
    FOR EACH ROW
    EXECUTE FUNCTION qvcse.directory_history_trigger();

-- Table: qvcse.directory_history
CREATE TABLE qvcse.directory_history
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

ALTER TABLE qvcse.directory_history
    OWNER to qvcse;

-- Table: qvcse.file
CREATE TABLE qvcse.file
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
        REFERENCES qvcse.branch (branch_id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
)

TABLESPACE pg_default;

ALTER TABLE qvcse.file
    OWNER to qvcse;

-- Trigger: file_changes
CREATE TRIGGER file_changes
    AFTER UPDATE 
    ON qvcse.file
    FOR EACH ROW
    EXECUTE FUNCTION qvcse.file_history_trigger();

-- Table: qvcse.file_history
CREATE TABLE qvcse.file_history
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

ALTER TABLE qvcse.file_history
    OWNER to qvcse;

-- Table: qvcse.promotion_candidate
CREATE TABLE qvcse.promotion_candidate
(
    file_id integer NOT NULL,
    branch_id integer NOT NULL,
    insert_date timestamp without time zone NOT NULL,
    CONSTRAINT promotion_pk PRIMARY KEY (file_id, branch_id)
)

TABLESPACE pg_default;

ALTER TABLE qvcse.promotion_candidate
    OWNER to qvcse;

-- Table: qvcse.revision
CREATE TABLE qvcse.revision
(
    id integer NOT NULL GENERATED ALWAYS AS IDENTITY ( INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 2147483647 CACHE 1 ),
    branch_id integer NOT NULL,
    file_id integer NOT NULL,
    revision_string character varying(256) COLLATE pg_catalog."default" NOT NULL,
    insert_date timestamp without time zone NOT NULL,
    CONSTRAINT revision_pk PRIMARY KEY (id)
)

TABLESPACE pg_default;

ALTER TABLE qvcse.revision
    OWNER to qvcse;

-- Insert branch type data
INSERT INTO qvcse.branch_type (branch_type_id, branch_type_name) VALUES (1, 'Trunk');
INSERT INTO qvcse.branch_type (branch_type_id, branch_type_name) VALUES (2, 'Read Only Date Based Branch');
INSERT INTO qvcse.branch_type (branch_type_id, branch_type_name) VALUES (3, 'Feature Branch');
INSERT INTO qvcse.branch_type (branch_type_id, branch_type_name) VALUES (4, 'Release Branch');
