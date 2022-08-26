DROP DATABASE qvcsos410prod;

DROP USER qvcsos410prod;

CREATE USER qvcsos410prod WITH PASSWORD 'qvcsos410prodPG$Admin';

-- Database: qvcsos410prod
CREATE DATABASE qvcsos410prod
    WITH
    OWNER = qvcsos410prod
    ENCODING = 'UTF8'
    LC_COLLATE = 'en_US.UTF-8'
    LC_CTYPE = 'en_US.UTF-8'
    TABLESPACE = pg_default
    CONNECTION LIMIT = -1;

GRANT ALL ON DATABASE qvcsos410prod TO qvcsos410prod;

GRANT TEMPORARY, CONNECT ON DATABASE qvcsos410prod TO PUBLIC;

\c qvcsos410prod

-- SCHEMA: qvcsos410prod
CREATE SCHEMA qvcsos410prod
    AUTHORIZATION qvcsos410prod;

-- FUNCTION: qvcsos410prod.directory_location_history_trigger()
CREATE FUNCTION qvcsos410prod.directory_location_history_trigger()
    RETURNS trigger
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE NOT LEAKPROOF
AS $BODY$
BEGIN
    INSERT INTO qvcsos410prod.directory_location_history(directory_location_id, directory_id, branch_id, parent_directory_location_id, created_for_reason, commit_id, directory_segment_name, deleted_flag)
    VALUES(OLD.id, OLD.directory_id, OLD.branch_id, OLD.parent_directory_location_id, OLD.created_for_reason, OLD.commit_id, OLD.directory_segment_name, OLD.deleted_flag);

    RETURN NEW;
END;
$BODY$;

ALTER FUNCTION qvcsos410prod.directory_location_history_trigger()
    OWNER TO qvcsos410prod;


-- FUNCTION: qvcsos410prod.file_name_trigger()
CREATE FUNCTION qvcsos410prod.file_name_trigger()
    RETURNS trigger
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE NOT LEAKPROOF
AS $BODY$
BEGIN
    INSERT INTO qvcsos410prod.file_name_history(file_name_id, branch_id, directory_id, file_id, created_for_reason, commit_id, file_name, promoted_flag, deleted_flag, promotion_commit_id)
    VALUES(OLD.id, OLD.branch_id, OLD.directory_id, OLD.file_id, OLD.created_for_reason, OLD.commit_id, OLD.file_name, OLD.promoted_flag, OLD.deleted_flag, OLD.promotion_commit_id);

    RETURN NEW;
END;
$BODY$;

ALTER FUNCTION qvcsos410prod.file_name_trigger()
    OWNER TO qvcsos410prod;


-- Table: qvcsos410prod.branch_type
CREATE TABLE qvcsos410prod.branch_type
(
    branch_type_id integer NOT NULL,
    branch_type_name character varying(256) COLLATE pg_catalog."default" NOT NULL,
    CONSTRAINT branch_type_pk PRIMARY KEY (branch_type_id)
)

TABLESPACE pg_default;

ALTER TABLE qvcsos410prod.branch_type
    OWNER to qvcsos410prod;

-- Table: qvcsos410prod.user
CREATE TABLE qvcsos410prod."user"
(
    id integer NOT NULL GENERATED ALWAYS AS IDENTITY ( INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 2147483647 CACHE 1 ),
    user_name character varying(256) COLLATE pg_catalog."default" NOT NULL,
    password bytea NOT NULL,
    deleted_flag boolean NOT NULL,
    CONSTRAINT user_pkey PRIMARY KEY (id),
    CONSTRAINT user_name_idx UNIQUE (user_name)
)

TABLESPACE pg_default;

ALTER TABLE qvcsos410prod."user"
    OWNER to qvcsos410prod;

-- Table: qvcsos410prod.commit
CREATE TABLE qvcsos410prod.comit
(
    id integer NOT NULL GENERATED ALWAYS AS IDENTITY ( INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 2147483647 CACHE 1 ),
    user_id integer NOT NULL,
    commit_date timestamp without time zone NOT NULL,
    commit_message character varying(2048) COLLATE pg_catalog."default" NOT NULL,
    CONSTRAINT commit_pk PRIMARY KEY (id),
    CONSTRAINT user_fk FOREIGN KEY (user_id)
        REFERENCES qvcsos410prod."user" (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
)

TABLESPACE pg_default;

ALTER TABLE qvcsos410prod.comit
    OWNER to qvcsos410prod;

-- Table: qvcsos410prod.project
CREATE TABLE qvcsos410prod.project
(
    id integer NOT NULL GENERATED ALWAYS AS IDENTITY ( INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 2147483647 CACHE 1 ),
    project_name character varying(256) COLLATE pg_catalog."default" NOT NULL,
    commit_id integer NOT NULL,
    deleted_flag boolean NOT NULL,
    CONSTRAINT project_pk PRIMARY KEY (id),
    CONSTRAINT commit_fk FOREIGN KEY (commit_id)
        REFERENCES qvcsos410prod.comit (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT project_name_unique UNIQUE (project_name)
)

TABLESPACE pg_default;

ALTER TABLE qvcsos410prod.project
    OWNER to qvcsos410prod;

-- Table: qvcsos410prod.branch
CREATE TABLE qvcsos410prod.branch
(
    id integer NOT NULL GENERATED ALWAYS AS IDENTITY ( INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 2147483647 CACHE 1 ),
    parent_branch_id integer,
    project_id integer NOT NULL,
    root_directory_id integer NOT NULL,
    commit_id integer NOT NULL,
    branch_name character varying(256) COLLATE pg_catalog."default" NOT NULL,
    branch_type_id integer NOT NULL,
    tag_id integer,
    deleted_flag boolean NOT NULL,
    CONSTRAINT branch_pk PRIMARY KEY (id),
    CONSTRAINT branch_type_fk FOREIGN KEY (branch_type_id)
        REFERENCES qvcsos410prod.branch_type (branch_type_id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT commit_fk FOREIGN KEY (commit_id)
        REFERENCES qvcsos410prod.comit (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT project_fk FOREIGN KEY (project_id)
        REFERENCES qvcsos410prod.project (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
)

TABLESPACE pg_default;

ALTER TABLE qvcsos410prod.branch
    OWNER to qvcsos410prod;

-- Index: branch_idx
CREATE UNIQUE INDEX branch_idx
    ON qvcsos410prod.branch USING btree
    (project_id ASC NULLS LAST, branch_name COLLATE pg_catalog."default" ASC NULLS LAST)
    TABLESPACE pg_default;

CREATE TABLE qvcsos410prod.directory
(
    id integer NOT NULL GENERATED ALWAYS AS IDENTITY ( INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 2147483647 CACHE 1 ),
    project_id integer NOT NULL,
    CONSTRAINT directory_pk PRIMARY KEY (id),
    CONSTRAINT project_fk FOREIGN KEY (project_id)
        REFERENCES qvcsos410prod.project (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
)

TABLESPACE pg_default;

ALTER TABLE qvcsos410prod.directory
    OWNER to qvcsos410prod;

ALTER TABLE qvcsos410prod.branch
    ADD CONSTRAINT directory_fk FOREIGN KEY (root_directory_id)
    REFERENCES qvcsos410prod.directory (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION;


-- Index: directory_idx
CREATE UNIQUE INDEX directory_idx
    ON qvcsos410prod.directory USING btree
    (project_id ASC NULLS LAST, id ASC NULLS LAST)
    TABLESPACE pg_default;

COMMENT ON INDEX qvcsos410prod.directory_idx
    IS 'Project, branch, and directory must be unique.';

-- Table: qvcsos410prod.directory_location
CREATE TABLE qvcsos410prod.directory_location
(
    id integer NOT NULL GENERATED ALWAYS AS IDENTITY ( INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 2147483647 CACHE 1 ),
    directory_id integer NOT NULL,
    branch_id integer NOT NULL,
    parent_directory_location_id integer,
    created_for_reason integer,
    commit_id integer NOT NULL,
    directory_segment_name character varying(2048) COLLATE pg_catalog."default" NOT NULL,
    deleted_flag boolean NOT NULL,
    CONSTRAINT directory_location_pk PRIMARY KEY (id),
    CONSTRAINT branch_fk FOREIGN KEY (branch_id)
        REFERENCES qvcsos410prod.branch (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT commit_fk FOREIGN KEY (commit_id)
        REFERENCES qvcsos410prod.comit (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
)

TABLESPACE pg_default;

ALTER TABLE qvcsos410prod.directory_location
    OWNER to qvcsos410prod;

COMMENT ON COLUMN qvcsos410prod.directory_location.parent_directory_location_id
    IS 'For the root directory, the parent_directory_location_id is NULL; else the parent_directory_location_id identifies the directory_location''s parent directory_location_id.';

-- Index: directory_location_idx
CREATE INDEX directory_location_idx
    ON qvcsos410prod.directory_location USING btree
    (directory_id ASC NULLS LAST)
    TABLESPACE pg_default;

-- Trigger: directory_location_changes
CREATE TRIGGER directory_location_changes
    AFTER UPDATE
    ON qvcsos410prod.directory_location
    FOR EACH ROW
    EXECUTE FUNCTION qvcsos410prod.directory_location_history_trigger();

-- Table: qvcsos410prod.directory_history
CREATE TABLE qvcsos410prod.directory_location_history
(
    id integer NOT NULL GENERATED ALWAYS AS IDENTITY ( INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 2147483647 CACHE 1 ),
    directory_location_id integer NOT NULL,
    directory_id integer NOT NULL,
    branch_id integer NOT NULL,
    parent_directory_location_id integer,
    created_for_reason integer,
    commit_id integer NOT NULL,
    directory_segment_name character varying(2048) COLLATE pg_catalog."default" NOT NULL,
    deleted_flag boolean NOT NULL,
    CONSTRAINT directory_location_history_pk PRIMARY KEY (id)
)

TABLESPACE pg_default;

ALTER TABLE qvcsos410prod.directory_location_history
    OWNER to qvcsos410prod;

-- Table: qvcsos410prod.file
CREATE TABLE qvcsos410prod.file
(
    id integer NOT NULL GENERATED ALWAYS AS IDENTITY ( INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 2147483647 CACHE 1 ),
    project_id integer NOT NULL,
    CONSTRAINT file_pk PRIMARY KEY (id),
    CONSTRAINT project_fk FOREIGN KEY (project_id)
        REFERENCES qvcsos410prod.project (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
)

TABLESPACE pg_default;

ALTER TABLE qvcsos410prod.file
    OWNER to qvcsos410prod;

-- Table: qvcsos410prod.file_name
CREATE TABLE qvcsos410prod.file_name
(
    id integer NOT NULL GENERATED ALWAYS AS IDENTITY ( INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 2147483647 CACHE 1 ),
    branch_id integer NOT NULL,
    directory_id integer NOT NULL,
    file_id integer NOT NULL,
    created_for_reason integer,
    commit_id integer NOT NULL,
    file_name character varying(256) COLLATE pg_catalog."default" NOT NULL,
    promoted_flag boolean NOT NULL,
    deleted_flag boolean NOT NULL,
    promotion_commit_id integer,
    CONSTRAINT file_name_pk PRIMARY KEY (id),
    CONSTRAINT branch_dir_file_idx UNIQUE (branch_id, directory_id, file_id),
    CONSTRAINT branch_fk FOREIGN KEY (branch_id)
        REFERENCES qvcsos410prod.branch (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT commit_fk FOREIGN KEY (commit_id)
        REFERENCES qvcsos410prod.comit (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT directory_fk FOREIGN KEY (directory_id)
        REFERENCES qvcsos410prod.directory (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT file_fk FOREIGN KEY (file_id)
        REFERENCES qvcsos410prod.file (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
)

TABLESPACE pg_default;

ALTER TABLE qvcsos410prod.file_name
    OWNER to qvcsos410prod;

-- Trigger: file_name_changes
CREATE TRIGGER file_name_changes
    AFTER UPDATE
    ON qvcsos410prod.file_name
    FOR EACH ROW
    EXECUTE FUNCTION qvcsos410prod.file_name_trigger();

-- Table: qvcsos410prod.file_name_history
CREATE TABLE qvcsos410prod.file_name_history
(
    id integer NOT NULL GENERATED ALWAYS AS IDENTITY ( INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 2147483647 CACHE 1 ),
    file_name_id integer NOT NULL,
    branch_id integer NOT NULL,
    directory_id integer NOT NULL,
    file_id integer NOT NULL,
    created_for_reason integer,
    commit_id integer NOT NULL,
    file_name character varying(256) COLLATE pg_catalog."default" NOT NULL,
    promoted_flag boolean NOT NULL,
    deleted_flag boolean NOT NULL,
    promotion_commit_id integer,
    CONSTRAINT id_pk PRIMARY KEY (id)
)

TABLESPACE pg_default;

ALTER TABLE qvcsos410prod.file_name_history
    OWNER to qvcsos410prod;

-- Table: qvcsos410prod.file_revision
CREATE TABLE qvcsos410prod.file_revision
(
    id integer NOT NULL GENERATED ALWAYS AS IDENTITY ( INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 2147483647 CACHE 1 ),
    branch_id integer NOT NULL,
    file_id integer NOT NULL,
    ancestor_revision_id integer,
    reverse_delta_revision_id integer,
    commit_id integer NOT NULL,
    workfile_edit_date timestamp without time zone,
    revision_digest bytea NOT NULL,
    revision_data bytea NOT NULL,
    promoted_flag boolean NOT NULL,
    promotion_commit_id integer,
    CONSTRAINT revision_pk PRIMARY KEY (id),
    CONSTRAINT commit_fk FOREIGN KEY (commit_id)
        REFERENCES qvcsos410prod.comit (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT file_fk FOREIGN KEY (file_id)
        REFERENCES qvcsos410prod.file (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
)

TABLESPACE pg_default;

ALTER TABLE qvcsos410prod.file_revision
    OWNER to qvcsos410prod;

CREATE TABLE qvcsos410prod.tag
(
    id integer NOT NULL GENERATED ALWAYS AS IDENTITY ( INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 2147483647 CACHE 1 ),
    commit_id integer NOT NULL,
    branch_id integer NOT NULL,
    moveable_flag boolean NOT NULL,
    tag_text character varying COLLATE pg_catalog."default" NOT NULL,
    description character varying COLLATE pg_catalog."default",
    CONSTRAINT tag_pkey PRIMARY KEY (id),
    CONSTRAINT tag_text_idx UNIQUE (tag_text),
    CONSTRAINT commit_fk FOREIGN KEY (commit_id)
        REFERENCES qvcsos410prod.comit (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT branch_fk FOREIGN KEY (branch_id)
        REFERENCES qvcsos410prod.branch (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
)

TABLESPACE pg_default;

ALTER TABLE qvcsos410prod.tag
    OWNER to qvcsos410prod;

CREATE TABLE qvcsos410prod.promotion_candidate
(
    id integer NOT NULL GENERATED ALWAYS AS IDENTITY ( INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 2147483647 CACHE 1 ),
    file_id integer NOT NULL,
    branch_id integer NOT NULL,
    CONSTRAINT promotion_candidate_pkey PRIMARY KEY (id),
    CONSTRAINT file_fk FOREIGN KEY (file_id)
        REFERENCES qvcsos410prod.file (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT branch_fk FOREIGN KEY (branch_id)
        REFERENCES qvcsos410prod.branch (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
)

TABLESPACE pg_default;

ALTER TABLE qvcsos410prod.promotion_candidate
    OWNER to qvcsos410prod;

CREATE TABLE qvcsos410prod.role_type
(
    id integer NOT NULL GENERATED ALWAYS AS IDENTITY ( INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 2147483647 CACHE 1 ),
    role_name character varying COLLATE pg_catalog."default" NOT NULL,
    CONSTRAINT role_type_pkey PRIMARY KEY (id),
    CONSTRAINT role_name_idx UNIQUE (role_name)
)

TABLESPACE pg_default;

ALTER TABLE qvcsos410prod.role_type
    OWNER to qvcsos410prod;


CREATE TABLE qvcsos410prod.privileged_action
(
    action_id integer NOT NULL,
    action_name character varying COLLATE pg_catalog."default" NOT NULL,
    admin_only_flag boolean NOT NULL,
    CONSTRAINT privileged_action_pkey PRIMARY KEY (action_id)
)

TABLESPACE pg_default;

ALTER TABLE qvcsos410prod.privileged_action
    OWNER to qvcsos410prod;

CREATE TABLE qvcsos410prod.role_type_action_join
(
    id integer NOT NULL GENERATED ALWAYS AS IDENTITY ( INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 2147483647 CACHE 1 ),
    role_type_id integer NOT NULL,
    action_id integer NOT NULL,
    action_enabled_flag boolean NOT NULL,
    CONSTRAINT role_type_action_join_pkey PRIMARY KEY (id),
    CONSTRAINT role_type_action_id_idx UNIQUE (role_type_id, action_id),
    CONSTRAINT role_type_fk FOREIGN KEY (role_type_id)
        REFERENCES qvcsos410prod.role_type (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT action_fk FOREIGN KEY (action_id)
        REFERENCES qvcsos410prod.privileged_action (action_id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
)
TABLESPACE pg_default;

ALTER TABLE qvcsos410prod.role_type_action_join
    OWNER to qvcsos410prod;


CREATE TABLE qvcsos410prod.user_project_role
(
    id integer NOT NULL GENERATED ALWAYS AS IDENTITY ( INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 2147483647 CACHE 1 ),
    user_id integer NOT NULL,
    project_id integer NOT NULL,
    role_type_id integer NOT NULL,
    CONSTRAINT user_project_role_pkey PRIMARY KEY (id),
    CONSTRAINT user_project_role_type_id_idx UNIQUE (user_id, project_id, role_type_id),
    CONSTRAINT role_type_fk FOREIGN KEY (role_type_id)
        REFERENCES qvcsos410prod.role_type (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT user_fk FOREIGN KEY (user_id)
        REFERENCES qvcsos410prod.user (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT project_fk FOREIGN KEY (project_id)
        REFERENCES qvcsos410prod.project (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
)

TABLESPACE pg_default;

ALTER TABLE qvcsos410prod.user_project_role
    OWNER to qvcsos410prod;


-- Insert branch type data
INSERT INTO qvcsos410prod.branch_type (branch_type_id, branch_type_name) VALUES (1, 'Trunk');
INSERT INTO qvcsos410prod.branch_type (branch_type_id, branch_type_name) VALUES (2, 'Feature Branch');
INSERT INTO qvcsos410prod.branch_type (branch_type_id, branch_type_name) VALUES (3, 'Read Only Tag Based Branch');
INSERT INTO qvcsos410prod.branch_type (branch_type_id, branch_type_name) VALUES (4, 'Release Branch');

-- Insert role type data
INSERT INTO qvcsos410prod.role_type (role_name) VALUES ('ADMIN');
INSERT INTO qvcsos410prod.role_type (role_name) VALUES ('PROJECT_ADMIN');
INSERT INTO qvcsos410prod.role_type (role_name) VALUES ('READER');
INSERT INTO qvcsos410prod.role_type (role_name) VALUES ('WRITER');
INSERT INTO qvcsos410prod.role_type (role_name) VALUES ('DEVELOPER');
INSERT INTO qvcsos410prod.role_type (role_name) VALUES ('CEMETERY_ADMIN');

-- Insert action names data
INSERT INTO qvcsos410prod.privileged_action (action_id, action_name, admin_only_flag) VALUES (1, 'Get file', FALSE);
INSERT INTO qvcsos410prod.privileged_action (action_id, action_name, admin_only_flag) VALUES (2, 'Get directory', FALSE);
INSERT INTO qvcsos410prod.privileged_action (action_id, action_name, admin_only_flag) VALUES (3, 'Show cemetery', FALSE);
INSERT INTO qvcsos410prod.privileged_action (action_id, action_name, admin_only_flag) VALUES (4, 'Check in', FALSE);
INSERT INTO qvcsos410prod.privileged_action (action_id, action_name, admin_only_flag) VALUES (5, 'Rename file', FALSE);
INSERT INTO qvcsos410prod.privileged_action (action_id, action_name, admin_only_flag) VALUES (6, 'Move file', FALSE);
INSERT INTO qvcsos410prod.privileged_action (action_id, action_name, admin_only_flag) VALUES (7, 'Delete file', FALSE);
INSERT INTO qvcsos410prod.privileged_action (action_id, action_name, admin_only_flag) VALUES (12, 'Add file', FALSE);
INSERT INTO qvcsos410prod.privileged_action (action_id, action_name, admin_only_flag) VALUES (13, 'Add directory', FALSE);
INSERT INTO qvcsos410prod.privileged_action (action_id, action_name, admin_only_flag) VALUES (14, 'Merge from parent', FALSE);
INSERT INTO qvcsos410prod.privileged_action (action_id, action_name, admin_only_flag) VALUES (15, 'Promote to parent', FALSE);
INSERT INTO qvcsos410prod.privileged_action (action_id, action_name, admin_only_flag) VALUES (16, 'Delete directory', FALSE);
INSERT INTO qvcsos410prod.privileged_action (action_id, action_name, admin_only_flag) VALUES (17, 'Maintain branch', FALSE);
INSERT INTO qvcsos410prod.privileged_action (action_id, action_name, admin_only_flag) VALUES (18, '(Admin tool): Add user role', TRUE);
INSERT INTO qvcsos410prod.privileged_action (action_id, action_name, admin_only_flag) VALUES (19, '(Admin tool): Remove user role', TRUE);
INSERT INTO qvcsos410prod.privileged_action (action_id, action_name, admin_only_flag) VALUES (20, '(Admin tool): Assign user roles', TRUE);
INSERT INTO qvcsos410prod.privileged_action (action_id, action_name, admin_only_flag) VALUES (21, '(Admin tool): List project users', TRUE);
INSERT INTO qvcsos410prod.privileged_action (action_id, action_name, admin_only_flag) VALUES (22, '(Admin tool): List user roles', TRUE);
INSERT INTO qvcsos410prod.privileged_action (action_id, action_name, admin_only_flag) VALUES (23, '(Admin tool): Maintain project', TRUE);

-- Define privileges for ADMIN role
INSERT INTO qvcsos410prod.role_type_action_join (role_type_id, action_id, action_enabled_flag) VALUES (1, 1, FALSE);  -- GET
INSERT INTO qvcsos410prod.role_type_action_join (role_type_id, action_id, action_enabled_flag) VALUES (1, 2, FALSE);  -- GET DIRECTORY
INSERT INTO qvcsos410prod.role_type_action_join (role_type_id, action_id, action_enabled_flag) VALUES (1, 3, FALSE);  -- SHOW CEMETERY
INSERT INTO qvcsos410prod.role_type_action_join (role_type_id, action_id, action_enabled_flag) VALUES (1, 4, FALSE);  -- CHECKIN
INSERT INTO qvcsos410prod.role_type_action_join (role_type_id, action_id, action_enabled_flag) VALUES (1, 5, FALSE);  -- RENAME FILE
INSERT INTO qvcsos410prod.role_type_action_join (role_type_id, action_id, action_enabled_flag) VALUES (1, 6, FALSE);  -- MOVE FILE
INSERT INTO qvcsos410prod.role_type_action_join (role_type_id, action_id, action_enabled_flag) VALUES (1, 7, FALSE);  -- DELETE FILE
INSERT INTO qvcsos410prod.role_type_action_join (role_type_id, action_id, action_enabled_flag) VALUES (1, 12, FALSE); -- ADD FILE
INSERT INTO qvcsos410prod.role_type_action_join (role_type_id, action_id, action_enabled_flag) VALUES (1, 13, FALSE); -- ADD DIRECTORY
INSERT INTO qvcsos410prod.role_type_action_join (role_type_id, action_id, action_enabled_flag) VALUES (1, 14, FALSE); -- MERGE FROM PARENT
INSERT INTO qvcsos410prod.role_type_action_join (role_type_id, action_id, action_enabled_flag) VALUES (1, 15, FALSE); -- PROMOTE TO PARENT
INSERT INTO qvcsos410prod.role_type_action_join (role_type_id, action_id, action_enabled_flag) VALUES (1, 16, FALSE); -- DELETE DIRECTORY
INSERT INTO qvcsos410prod.role_type_action_join (role_type_id, action_id, action_enabled_flag) VALUES (1, 17, FALSE); -- MAINTAIN BRANCH
INSERT INTO qvcsos410prod.role_type_action_join (role_type_id, action_id, action_enabled_flag) VALUES (1, 18, TRUE);  -- ADD USER ROLE
INSERT INTO qvcsos410prod.role_type_action_join (role_type_id, action_id, action_enabled_flag) VALUES (1, 19, TRUE);  -- REMOVE USER ROLE
INSERT INTO qvcsos410prod.role_type_action_join (role_type_id, action_id, action_enabled_flag) VALUES (1, 20, TRUE);  -- ASSIGN USER ROLES
INSERT INTO qvcsos410prod.role_type_action_join (role_type_id, action_id, action_enabled_flag) VALUES (1, 21, TRUE);  -- LIST PROJECT USERS
INSERT INTO qvcsos410prod.role_type_action_join (role_type_id, action_id, action_enabled_flag) VALUES (1, 22, TRUE);  -- LIST USER ROLES
INSERT INTO qvcsos410prod.role_type_action_join (role_type_id, action_id, action_enabled_flag) VALUES (1, 23, FALSE); -- MAINTAIN PROJECT

-- Define privileges for PROJECT_ADMIN role
INSERT INTO qvcsos410prod.role_type_action_join (role_type_id, action_id, action_enabled_flag) VALUES (2, 1, FALSE);  -- GET
INSERT INTO qvcsos410prod.role_type_action_join (role_type_id, action_id, action_enabled_flag) VALUES (2, 2, FALSE);  -- GET DIRECTORY
INSERT INTO qvcsos410prod.role_type_action_join (role_type_id, action_id, action_enabled_flag) VALUES (2, 3, FALSE);  -- SHOW CEMETERY
INSERT INTO qvcsos410prod.role_type_action_join (role_type_id, action_id, action_enabled_flag) VALUES (2, 4, FALSE);  -- CHECKIN
INSERT INTO qvcsos410prod.role_type_action_join (role_type_id, action_id, action_enabled_flag) VALUES (2, 5, FALSE);  -- RENAME FILE
INSERT INTO qvcsos410prod.role_type_action_join (role_type_id, action_id, action_enabled_flag) VALUES (2, 6, FALSE);  -- MOVE FILE
INSERT INTO qvcsos410prod.role_type_action_join (role_type_id, action_id, action_enabled_flag) VALUES (2, 7, FALSE);  -- DELETE FILE
INSERT INTO qvcsos410prod.role_type_action_join (role_type_id, action_id, action_enabled_flag) VALUES (2, 12, FALSE); -- ADD FILE
INSERT INTO qvcsos410prod.role_type_action_join (role_type_id, action_id, action_enabled_flag) VALUES (2, 13, TRUE);  -- ADD DIRECTORY
INSERT INTO qvcsos410prod.role_type_action_join (role_type_id, action_id, action_enabled_flag) VALUES (2, 14, TRUE);  -- MERGE FROM PARENT
INSERT INTO qvcsos410prod.role_type_action_join (role_type_id, action_id, action_enabled_flag) VALUES (2, 15, TRUE);  -- PROMOTE TO PARENT
INSERT INTO qvcsos410prod.role_type_action_join (role_type_id, action_id, action_enabled_flag) VALUES (2, 16, TRUE);  -- DELETE DIRECTORY
INSERT INTO qvcsos410prod.role_type_action_join (role_type_id, action_id, action_enabled_flag) VALUES (2, 17, TRUE);  -- MAINTAIN BRANCH
INSERT INTO qvcsos410prod.role_type_action_join (role_type_id, action_id, action_enabled_flag) VALUES (2, 18, TRUE);  -- ADD USER ROLE
INSERT INTO qvcsos410prod.role_type_action_join (role_type_id, action_id, action_enabled_flag) VALUES (2, 19, TRUE);  -- REMOVE USER ROLE
INSERT INTO qvcsos410prod.role_type_action_join (role_type_id, action_id, action_enabled_flag) VALUES (2, 20, TRUE);  -- ASSIGN USER ROLES
INSERT INTO qvcsos410prod.role_type_action_join (role_type_id, action_id, action_enabled_flag) VALUES (2, 21, TRUE);  -- LIST PROJECT USERS
INSERT INTO qvcsos410prod.role_type_action_join (role_type_id, action_id, action_enabled_flag) VALUES (2, 22, TRUE);  -- LIST USER ROLES
INSERT INTO qvcsos410prod.role_type_action_join (role_type_id, action_id, action_enabled_flag) VALUES (2, 23, TRUE);  -- MAINTAIN PROJECT

-- Define privileges for READER role
INSERT INTO qvcsos410prod.role_type_action_join (role_type_id, action_id, action_enabled_flag) VALUES (3, 1, TRUE);   -- GET
INSERT INTO qvcsos410prod.role_type_action_join (role_type_id, action_id, action_enabled_flag) VALUES (3, 2, TRUE);   -- GET DIRECTORY
INSERT INTO qvcsos410prod.role_type_action_join (role_type_id, action_id, action_enabled_flag) VALUES (3, 3, FALSE);  -- SHOW CEMETERY
INSERT INTO qvcsos410prod.role_type_action_join (role_type_id, action_id, action_enabled_flag) VALUES (3, 4, FALSE);  -- CHECKIN
INSERT INTO qvcsos410prod.role_type_action_join (role_type_id, action_id, action_enabled_flag) VALUES (3, 5, FALSE);  -- RENAME FILE
INSERT INTO qvcsos410prod.role_type_action_join (role_type_id, action_id, action_enabled_flag) VALUES (3, 6, FALSE);  -- MOVE FILE
INSERT INTO qvcsos410prod.role_type_action_join (role_type_id, action_id, action_enabled_flag) VALUES (3, 7, FALSE);  -- DELETE FILE
INSERT INTO qvcsos410prod.role_type_action_join (role_type_id, action_id, action_enabled_flag) VALUES (3, 12, FALSE); -- ADD FILE
INSERT INTO qvcsos410prod.role_type_action_join (role_type_id, action_id, action_enabled_flag) VALUES (3, 13, FALSE); -- ADD DIRECTORY
INSERT INTO qvcsos410prod.role_type_action_join (role_type_id, action_id, action_enabled_flag) VALUES (3, 14, FALSE); -- MERGE FROM PARENT
INSERT INTO qvcsos410prod.role_type_action_join (role_type_id, action_id, action_enabled_flag) VALUES (3, 15, FALSE); -- PROMOTE TO PARENT
INSERT INTO qvcsos410prod.role_type_action_join (role_type_id, action_id, action_enabled_flag) VALUES (3, 16, FALSE); -- DELETE DIRECTORY
INSERT INTO qvcsos410prod.role_type_action_join (role_type_id, action_id, action_enabled_flag) VALUES (3, 17, FALSE); -- MAINTAIN BRANCH
INSERT INTO qvcsos410prod.role_type_action_join (role_type_id, action_id, action_enabled_flag) VALUES (3, 18, FALSE); -- ADD USER ROLE
INSERT INTO qvcsos410prod.role_type_action_join (role_type_id, action_id, action_enabled_flag) VALUES (3, 19, FALSE); -- REMOVE USER ROLE
INSERT INTO qvcsos410prod.role_type_action_join (role_type_id, action_id, action_enabled_flag) VALUES (3, 20, FALSE); -- ASSIGN USER ROLES
INSERT INTO qvcsos410prod.role_type_action_join (role_type_id, action_id, action_enabled_flag) VALUES (3, 21, FALSE); -- LIST PROJECT USERS
INSERT INTO qvcsos410prod.role_type_action_join (role_type_id, action_id, action_enabled_flag) VALUES (3, 22, FALSE); -- LIST USER ROLES
INSERT INTO qvcsos410prod.role_type_action_join (role_type_id, action_id, action_enabled_flag) VALUES (3, 23, FALSE); -- MAINTAIN PROJECT

-- Define privileges for WRITER role
INSERT INTO qvcsos410prod.role_type_action_join (role_type_id, action_id, action_enabled_flag) VALUES (4, 1, FALSE);  -- GET
INSERT INTO qvcsos410prod.role_type_action_join (role_type_id, action_id, action_enabled_flag) VALUES (4, 2, FALSE);  -- GET DIRECTORY
INSERT INTO qvcsos410prod.role_type_action_join (role_type_id, action_id, action_enabled_flag) VALUES (4, 3, FALSE);  -- SHOW CEMETERY
INSERT INTO qvcsos410prod.role_type_action_join (role_type_id, action_id, action_enabled_flag) VALUES (4, 4, TRUE);   -- CHECKIN
INSERT INTO qvcsos410prod.role_type_action_join (role_type_id, action_id, action_enabled_flag) VALUES (4, 5, TRUE);   -- RENAME FILE
INSERT INTO qvcsos410prod.role_type_action_join (role_type_id, action_id, action_enabled_flag) VALUES (4, 6, TRUE);   -- MOVE FILE
INSERT INTO qvcsos410prod.role_type_action_join (role_type_id, action_id, action_enabled_flag) VALUES (4, 7, TRUE);   -- DELETE FILE
INSERT INTO qvcsos410prod.role_type_action_join (role_type_id, action_id, action_enabled_flag) VALUES (4, 12, TRUE);  -- ADD FILE
INSERT INTO qvcsos410prod.role_type_action_join (role_type_id, action_id, action_enabled_flag) VALUES (4, 13, TRUE);  -- ADD DIRECTORY
INSERT INTO qvcsos410prod.role_type_action_join (role_type_id, action_id, action_enabled_flag) VALUES (4, 14, TRUE);  -- MERGE FROM PARENT
INSERT INTO qvcsos410prod.role_type_action_join (role_type_id, action_id, action_enabled_flag) VALUES (4, 15, TRUE);  -- PROMOTE TO PARENT
INSERT INTO qvcsos410prod.role_type_action_join (role_type_id, action_id, action_enabled_flag) VALUES (4, 16, TRUE);  -- DELETE DIRECTORY
INSERT INTO qvcsos410prod.role_type_action_join (role_type_id, action_id, action_enabled_flag) VALUES (4, 17, TRUE);  -- MAINTAIN BRANCH
INSERT INTO qvcsos410prod.role_type_action_join (role_type_id, action_id, action_enabled_flag) VALUES (4, 18, FALSE); -- ADD USER ROLE
INSERT INTO qvcsos410prod.role_type_action_join (role_type_id, action_id, action_enabled_flag) VALUES (4, 19, FALSE); -- REMOVE USER ROLE
INSERT INTO qvcsos410prod.role_type_action_join (role_type_id, action_id, action_enabled_flag) VALUES (4, 20, FALSE); -- ASSIGN USER ROLES
INSERT INTO qvcsos410prod.role_type_action_join (role_type_id, action_id, action_enabled_flag) VALUES (4, 21, FALSE); -- LIST PROJECT USERS
INSERT INTO qvcsos410prod.role_type_action_join (role_type_id, action_id, action_enabled_flag) VALUES (4, 22, FALSE); -- LIST USER ROLES
INSERT INTO qvcsos410prod.role_type_action_join (role_type_id, action_id, action_enabled_flag) VALUES (4, 23, FALSE); -- MAINTAIN PROJECT

-- Define privileges for DEVELOPER role
INSERT INTO qvcsos410prod.role_type_action_join (role_type_id, action_id, action_enabled_flag) VALUES (5, 1, TRUE);    -- GET
INSERT INTO qvcsos410prod.role_type_action_join (role_type_id, action_id, action_enabled_flag) VALUES (5, 2, TRUE);    -- GET DIRECTORY
INSERT INTO qvcsos410prod.role_type_action_join (role_type_id, action_id, action_enabled_flag) VALUES (5, 3, FALSE);   -- SHOW CEMETERY
INSERT INTO qvcsos410prod.role_type_action_join (role_type_id, action_id, action_enabled_flag) VALUES (5, 4, TRUE);    -- CHECKIN
INSERT INTO qvcsos410prod.role_type_action_join (role_type_id, action_id, action_enabled_flag) VALUES (5, 5, TRUE);    -- RENAME FILE
INSERT INTO qvcsos410prod.role_type_action_join (role_type_id, action_id, action_enabled_flag) VALUES (5, 6, TRUE);    -- MOVE FILE
INSERT INTO qvcsos410prod.role_type_action_join (role_type_id, action_id, action_enabled_flag) VALUES (5, 7, TRUE);    -- DELETE FILE
INSERT INTO qvcsos410prod.role_type_action_join (role_type_id, action_id, action_enabled_flag) VALUES (5, 12, TRUE);   -- ADD FILE
INSERT INTO qvcsos410prod.role_type_action_join (role_type_id, action_id, action_enabled_flag) VALUES (5, 13, TRUE);   -- ADD DIRECTORY
INSERT INTO qvcsos410prod.role_type_action_join (role_type_id, action_id, action_enabled_flag) VALUES (5, 14, TRUE);   -- MERGE FROM PARENT
INSERT INTO qvcsos410prod.role_type_action_join (role_type_id, action_id, action_enabled_flag) VALUES (5, 15, TRUE);   -- PROMOTE TO PARENT
INSERT INTO qvcsos410prod.role_type_action_join (role_type_id, action_id, action_enabled_flag) VALUES (5, 16, TRUE);   -- DELETE DIRECTORY
INSERT INTO qvcsos410prod.role_type_action_join (role_type_id, action_id, action_enabled_flag) VALUES (5, 17, TRUE);   -- MAINTAIN BRANCH
INSERT INTO qvcsos410prod.role_type_action_join (role_type_id, action_id, action_enabled_flag) VALUES (5, 18, FALSE);  -- ADD USER ROLE
INSERT INTO qvcsos410prod.role_type_action_join (role_type_id, action_id, action_enabled_flag) VALUES (5, 19, FALSE);  -- REMOVE USER ROLE
INSERT INTO qvcsos410prod.role_type_action_join (role_type_id, action_id, action_enabled_flag) VALUES (5, 20, FALSE);  -- ASSIGN USER ROLES
INSERT INTO qvcsos410prod.role_type_action_join (role_type_id, action_id, action_enabled_flag) VALUES (5, 21, FALSE);  -- LIST PROJECT USERS
INSERT INTO qvcsos410prod.role_type_action_join (role_type_id, action_id, action_enabled_flag) VALUES (5, 22, FALSE);  -- LIST USER ROLES
INSERT INTO qvcsos410prod.role_type_action_join (role_type_id, action_id, action_enabled_flag) VALUES (5, 23, FALSE);  -- MAINTAIN PROJECT

-- Define privileges for CEMETERY_ADMIN role
INSERT INTO qvcsos410prod.role_type_action_join (role_type_id, action_id, action_enabled_flag) VALUES (6, 1, FALSE);   -- GET
INSERT INTO qvcsos410prod.role_type_action_join (role_type_id, action_id, action_enabled_flag) VALUES (6, 2, FALSE);   -- GET DIRECTORY
INSERT INTO qvcsos410prod.role_type_action_join (role_type_id, action_id, action_enabled_flag) VALUES (6, 3, TRUE);    -- SHOW CEMETERY
INSERT INTO qvcsos410prod.role_type_action_join (role_type_id, action_id, action_enabled_flag) VALUES (6, 4, FALSE);   -- CHECKIN
INSERT INTO qvcsos410prod.role_type_action_join (role_type_id, action_id, action_enabled_flag) VALUES (6, 5, FALSE);   -- RENAME FILE
INSERT INTO qvcsos410prod.role_type_action_join (role_type_id, action_id, action_enabled_flag) VALUES (6, 6, FALSE);   -- MOVE FILE
INSERT INTO qvcsos410prod.role_type_action_join (role_type_id, action_id, action_enabled_flag) VALUES (6, 7, FALSE);   -- DELETE FILE
INSERT INTO qvcsos410prod.role_type_action_join (role_type_id, action_id, action_enabled_flag) VALUES (6, 12, FALSE);  -- ADD FILE
INSERT INTO qvcsos410prod.role_type_action_join (role_type_id, action_id, action_enabled_flag) VALUES (6, 13, FALSE);  -- ADD DIRECTORY
INSERT INTO qvcsos410prod.role_type_action_join (role_type_id, action_id, action_enabled_flag) VALUES (6, 14, FALSE);  -- MERGE FROM PARENT
INSERT INTO qvcsos410prod.role_type_action_join (role_type_id, action_id, action_enabled_flag) VALUES (6, 15, FALSE);  -- PROMOTE TO PARENT
INSERT INTO qvcsos410prod.role_type_action_join (role_type_id, action_id, action_enabled_flag) VALUES (6, 16, FALSE);  -- DELETE DIRECTORY
INSERT INTO qvcsos410prod.role_type_action_join (role_type_id, action_id, action_enabled_flag) VALUES (6, 17, FALSE);  -- MAINTAIN BRANCH
INSERT INTO qvcsos410prod.role_type_action_join (role_type_id, action_id, action_enabled_flag) VALUES (6, 18, FALSE);  -- ADD USER ROLE
INSERT INTO qvcsos410prod.role_type_action_join (role_type_id, action_id, action_enabled_flag) VALUES (6, 19, FALSE);  -- REMOVE USER ROLE
INSERT INTO qvcsos410prod.role_type_action_join (role_type_id, action_id, action_enabled_flag) VALUES (6, 20, FALSE);  -- ASSIGN USER ROLES
INSERT INTO qvcsos410prod.role_type_action_join (role_type_id, action_id, action_enabled_flag) VALUES (6, 21, FALSE);  -- LIST PROJECT USERS
INSERT INTO qvcsos410prod.role_type_action_join (role_type_id, action_id, action_enabled_flag) VALUES (6, 22, FALSE);  -- LIST USER ROLES
INSERT INTO qvcsos410prod.role_type_action_join (role_type_id, action_id, action_enabled_flag) VALUES (6, 23, FALSE);  -- MAINTAIN PROJECT