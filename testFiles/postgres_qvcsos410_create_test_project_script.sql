\c qvcsos410test

-- Add vanilla users so we can have a user constraint on the comit table.
INSERT INTO qvcsos410test."user" (user_name, password, deleted_flag) VALUES ('ScriptedTestUser', 'foobar'::bytea, FALSE);
INSERT INTO qvcsos410test."user" (user_name, password, deleted_flag) VALUES ('JoeSmith', 'foobar'::bytea, FALSE);
INSERT INTO qvcsos410test."user" (user_name, password, deleted_flag) VALUES ('JaneSmith', 'foobar'::bytea, FALSE);
INSERT INTO qvcsos410test."user" (user_name, password, deleted_flag) VALUES ('JeffSmith', 'foobar'::bytea, FALSE);
INSERT INTO qvcsos410test."user" (user_name, password, deleted_flag) VALUES ('JoeAdmin', 'foobar'::bytea, FALSE);
INSERT INTO qvcsos410test."user" (user_name, password, deleted_flag) VALUES ('RalphSmith', 'foobar'::bytea, FALSE);

INSERT INTO qvcsos410test.comit (commit_date, user_id, commit_message) VALUES (CURRENT_TIMESTAMP, 2, 'Create Project: Test Project');
INSERT INTO qvcsos410test.project (project_name, commit_id, deleted_flag) VALUES ('Test Project', 1, FALSE);
INSERT INTO qvcsos410test.directory (project_id) VALUES (1);
INSERT INTO qvcsos410test.branch (project_id, root_directory_id, commit_id, branch_name, branch_type_id, deleted_flag) VALUES (1, 1, 1, 'Trunk', 1, FALSE);

-- Add the root directory to Trunk branch.
INSERT INTO qvcsos410test.directory_location (directory_id, branch_id, commit_id, directory_segment_name, deleted_flag) VALUES (1, 1, 1, '', FALSE);

-- Add a 2nd directory to the Trunk branch.
INSERT INTO qvcsos410test.directory (project_id) VALUES (1);
INSERT INTO qvcsos410test.directory_location (directory_id, branch_id, parent_directory_location_id, commit_id, directory_segment_name, deleted_flag) VALUES (2, 1, 1, 1, '1st Scripted Child Directory Name', FALSE);

-- Add a 3rd directory to the Trunk branch.
INSERT INTO qvcsos410test.directory (project_id) VALUES (1);
INSERT INTO qvcsos410test.directory_location (directory_id, branch_id, parent_directory_location_id, commit_id, directory_segment_name, deleted_flag) VALUES (3, 1, 1, 1, '2nd Scripted Child Directory Name', FALSE);

-- Add a 4th directory to the Trunk branch. -- use for directory move tests.
INSERT INTO qvcsos410test.directory (project_id) VALUES (1);
INSERT INTO qvcsos410test.directory_location (directory_id, branch_id, parent_directory_location_id, commit_id, directory_segment_name, deleted_flag) VALUES (4, 1, 1, 1, '3rd Scripted Child Directory Name', FALSE);

-- Add a 5th directory to the Trunk branch. -- use for directory move tests.
INSERT INTO qvcsos410test.directory (project_id) VALUES (1);
INSERT INTO qvcsos410test.directory_location (directory_id, branch_id, parent_directory_location_id, commit_id, directory_segment_name, deleted_flag) VALUES (5, 1, 1, 1, '4th Scripted Child Directory Name', FALSE);

-- Add a 6th directory to the Trunk branch. -- use for directory delete tests.
INSERT INTO qvcsos410test.directory (project_id) VALUES (1);
INSERT INTO qvcsos410test.directory_location (directory_id, branch_id, parent_directory_location_id, commit_id, directory_segment_name, deleted_flag) VALUES (6, 1, 1, 1, '5th Scripted Child Directory Name', FALSE);

-- Add a single feature branch to the db.
INSERT INTO qvcsos410test.branch (project_id, parent_branch_id, root_directory_id, commit_id, branch_name, branch_type_id, deleted_flag) VALUES (1, 1, 1, 1, 'Scripted Feature Branch', 2, FALSE);

-- Add another feature branch to the db.
INSERT INTO qvcsos410test.branch (project_id, parent_branch_id, root_directory_id, commit_id, branch_name, branch_type_id, deleted_flag) VALUES (1, 1, 1, 1, '2.2.2', 2, FALSE);

-- Add root directory on the feature branch.
INSERT INTO qvcsos410test.directory (project_id) VALUES (1);
INSERT INTO qvcsos410test.directory_location (directory_id, branch_id, commit_id, directory_segment_name, deleted_flag) VALUES (7, 2, 1, '', FALSE);

-- Add child directory on the feature branch. -- use for directory delete tests.
INSERT INTO qvcsos410test.directory (project_id) VALUES (1);
INSERT INTO qvcsos410test.directory_location (directory_id, branch_id, parent_directory_location_id, commit_id, directory_segment_name, deleted_flag) VALUES (8, 2, 7, 1, '1st Scripted Feature Branch Directory Name', FALSE);
