\c qvcsos410legacy

-- Add ADMIN user and a vanilla user so we can have a user constraint on the comit table.
INSERT INTO qvcsos410legacy."user" (user_name, password, deleted_flag) VALUES ('ADMIN', 'foobar'::bytea, FALSE);
INSERT INTO qvcsos410legacy."user" (user_name, password, deleted_flag) VALUES ('Migrator', 'foobar'::bytea, FALSE);
