/*
 * Copyright 2021 Jim Voris.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qvcsos.server.functionaltests;

import com.qvcsos.server.DatabaseManager;
import com.qvcsos.server.TestHelper;
import com.qvcsos.server.dataaccess.BranchDAO;
import com.qvcsos.server.dataaccess.CommitDAO;
import com.qvcsos.server.dataaccess.DirectoryDAO;
import com.qvcsos.server.dataaccess.DirectoryLocationDAO;
import com.qvcsos.server.dataaccess.ProjectDAO;
import com.qvcsos.server.dataaccess.impl.BranchDAOImpl;
import com.qvcsos.server.dataaccess.impl.CommitDAOImpl;
import com.qvcsos.server.dataaccess.impl.DirectoryDAOImpl;
import com.qvcsos.server.dataaccess.impl.DirectoryLocationDAOImpl;
import com.qvcsos.server.dataaccess.impl.ProjectDAOImpl;
import com.qvcsos.server.datamodel.Branch;
import com.qvcsos.server.datamodel.Commit;
import com.qvcsos.server.datamodel.Directory;
import com.qvcsos.server.datamodel.DirectoryLocation;
import com.qvcsos.server.datamodel.Project;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Jim Voris
 */
public class CreateProjectServerTest {

    /**
     * Create our logger object.
     */
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(CreateProjectServerTest.class);

    private static DatabaseManager databaseManager;
    private static String schemaName;

    public CreateProjectServerTest() {
    }

    @BeforeClass
    public static void setUpClass() throws SQLException, ClassNotFoundException {
        TestHelper.resetTestDatabaseViaPsqlScript();
        TestHelper.createTestProjectViaPsqlScript();
        databaseManager = DatabaseManager.getInstance();
        schemaName = databaseManager.getSchemaName();
        databaseManager.initializeDatabase();
    }

    @AfterClass
    public static void tearDownClass() {
        databaseManager.shutdownDatabase();
    }

    /**
     * Test to create a Commit, and then add a Project to the database.
     * @throws java.sql.SQLException
     */
    @Test
    public void testCreateProject() throws SQLException {
        LOGGER.info("createProject Test");
        Connection connection = databaseManager.getConnection();
        connection.setAutoCommit(false);

        // Create a commit object, and insert into database.
        Commit commit = new Commit();
        String commitMessage = "Functional test commit";
        commit.setCommitMessage(commitMessage);
        commit.setUserId(2);
        CommitDAO commitDAO = new CommitDAOImpl(schemaName);
        Integer commitId = commitDAO.insert(commit);

        // Create a project object.
        Project project = new Project();
        project.setCommitId(commitId);
        project.setDeletedFlag(Boolean.FALSE);
        String projectName = "Functional Test Project";
        project.setProjectName(projectName);
        ProjectDAO projectDAO = new ProjectDAOImpl(schemaName);
        Integer projectId = projectDAO.insert(project);

        // Create the root directory for the 'trunk' branch.
        Directory directory = new Directory();
        directory.setProjectId(projectId);
        DirectoryDAO directoryDAO = new DirectoryDAOImpl(schemaName);
        Integer directoryId = directoryDAO.insert(directory);

        // Create the 'trunk' branch for the project.
        Branch branch = new Branch();
        String branchName = "Trunk";
        branch.setBranchName(branchName);
        branch.setBranchTypeId(1);
        branch.setCommitId(commitId);
        branch.setProjectId(projectId);
        branch.setRootDirectoryId(directoryId);
        branch.setDeletedFlag(Boolean.FALSE);
        BranchDAO branchDAO = new BranchDAOImpl(schemaName);
        Integer branchId = branchDAO.insert(branch);

        // Create the directory_location for the root directory.
        DirectoryLocation directoryLocation = new DirectoryLocation();
        directoryLocation.setCommitId(commitId);
        directoryLocation.setBranchId(branchId);
        directoryLocation.setDeletedFlag(Boolean.FALSE);
        directoryLocation.setDirectoryId(directoryId);
        directoryLocation.setDirectorySegmentName("");
        directoryLocation.setParentDirectoryLocationId(null);
        DirectoryLocationDAO directoryLocationDAO = new DirectoryLocationDAOImpl(schemaName);
        Integer directoryLocationId = directoryLocationDAO.insert(directoryLocation);
        LOGGER.info("CommitId: [{}], ProjectId: [{}], BranchId: [{}], DirectoryId: [{}], DirectoryLocationId: [{}]", commitId, projectId, branchId, directoryId, directoryLocationId);

        // Commit the transaction.
        connection.commit();

        // Verify that the commit row exists.
        Commit fetchedCommit = commitDAO.findById(commitId);
        assertEquals("Unexpected id.", commitId, fetchedCommit.getId());
        assertEquals("Unexpected commit message.", commitMessage, fetchedCommit.getCommitMessage());

        // Verify that the project row exists.
        Project fetchedProject = projectDAO.findById(projectId);
        assertEquals("Unexpected id.", projectId, fetchedProject.getId());
        assertEquals("Unexpected commitId.", commitId, fetchedProject.getCommitId());
        assertEquals("Unexpected deleted flag.", Boolean.FALSE, fetchedProject.getDeletedFlag());
        assertEquals("Unexpected project name", projectName, fetchedProject.getProjectName());

        // Verify that the branch row exists.
        Branch fetchedBranch = branchDAO.findById(branchId);
        assertEquals("Unexpected branch id.", branchId, fetchedBranch.getId());
        assertEquals("Unexpected branch name.", branchName, fetchedBranch.getBranchName());
        assertEquals("Unexpected branch type id.", Integer.valueOf(1), fetchedBranch.getBranchTypeId());
        assertEquals("Unexpected commit id.", commitId, fetchedBranch.getCommitId());
        assertEquals("Unexpected project id.", projectId, fetchedBranch.getProjectId());

        // Verify the directory row exists.
        Directory fetchedDirectory = directoryDAO.findById(directoryId);
        assertEquals("Unexpected directory id.", directoryId, fetchedDirectory.getId());
        assertEquals("Unexpected project id.", projectId, fetchedDirectory.getProjectId());

        // Verify the directory location row exits.
        DirectoryLocation fetchedDirectoryLocation = directoryLocationDAO.findById(directoryLocationId);
        assertEquals("Unexpected directoryLocation id.", directoryLocationId, fetchedDirectoryLocation.getId());
        assertEquals("Unexpected commit id.", commitId, fetchedDirectoryLocation.getCommitId());
        assertEquals("Unexpected directory id.", directoryId, fetchedDirectoryLocation.getDirectoryId());
        assertEquals("Unexpected branch id.", branchId, fetchedDirectoryLocation.getBranchId());
        assertEquals("Unexpected deleted flag.", Boolean.FALSE, fetchedDirectoryLocation.getDeletedFlag());
        assertEquals("Unexpected directory name id.", "", fetchedDirectoryLocation.getDirectorySegmentName());
        assertEquals("Unexpected parent directory location id.", null, fetchedDirectoryLocation.getParentDirectoryLocationId());

    }

    /**
     * Test to create a Commit, and then add a Project to the database, but rollback before commit.
     * @throws java.sql.SQLException
     */
    @Test
    public void testRollbackForCreateProject() throws SQLException {
        LOGGER.info("createProjectRollback Test");
        Connection connection = databaseManager.getConnection();
        connection.setAutoCommit(false);

        // Create a commit object, and insert into database.
        Commit commit = new Commit();
        String commitMessage = "Functional test commit";
        commit.setUserId(2);
        commit.setCommitMessage(commitMessage);
        Date now = new Date();
        Timestamp timestamp = new Timestamp(now.getTime());
        commit.setCommitDate(timestamp);
        CommitDAO commitDAO = new CommitDAOImpl(schemaName);
        Integer commitId = commitDAO.insert(commit);

        // Create a project object.
        Project project = new Project();
        project.setCommitId(commitId);
        project.setDeletedFlag(Boolean.FALSE);
        String projectName = "Functional Test Project";
        project.setProjectName(projectName);
        ProjectDAO projectDAO = new ProjectDAOImpl(schemaName);
        Integer projectId = projectDAO.insert(project);

        // Create the root directory for the 'trunk' branch.
        Directory directory = new Directory();
        directory.setProjectId(projectId);
        DirectoryDAO directoryDAO = new DirectoryDAOImpl(schemaName);
        Integer directoryId = directoryDAO.insert(directory);

        // Create the 'trunk' branch for the project.
        Branch branch = new Branch();
        String branchName = "Trunk";
        branch.setBranchName(branchName);
        branch.setBranchTypeId(1);
        branch.setCommitId(commitId);
        branch.setProjectId(projectId);
        branch.setRootDirectoryId(1);
        branch.setDeletedFlag(Boolean.FALSE);
        BranchDAO branchDAO = new BranchDAOImpl(schemaName);
        Integer branchId = branchDAO.insert(branch);

        // Create the directory_location for the root directory.
        DirectoryLocation directoryLocation = new DirectoryLocation();
        directoryLocation.setCommitId(commitId);
        directoryLocation.setDeletedFlag(Boolean.FALSE);
        directoryLocation.setDirectoryId(directoryId);
        directoryLocation.setBranchId(branchId);
        directoryLocation.setDirectorySegmentName("");
        directoryLocation.setParentDirectoryLocationId(null);
        DirectoryLocationDAO directoryLocationDAO = new DirectoryLocationDAOImpl(schemaName);
        Integer directoryLocationId = directoryLocationDAO.insert(directoryLocation);
        LOGGER.info("CommitId: [{}], ProjectId: [{}], BranchId: [{}], DirectoryId: [{}], DirectoryLocationId: [{}]", commitId, projectId, branchId, directoryId, directoryLocationId);

        // Rollback the transaction.
        connection.rollback();

        // Verify that the commit row does not exist.
        Commit fetchedCommit = commitDAO.findById(commitId);
        assertNull("Unexpected Commit exists. ", fetchedCommit);

        // Verify that the project row does not exist.
        Project fetchedProject = projectDAO.findById(projectId);
        assertNull("Unexpected Project exists. ", fetchedProject);

        // Verify that the branch row does not exist.
        Branch fetchedBranch = branchDAO.findById(branchId);
        assertNull("Unexpected Branch exists. ", fetchedBranch);

        // Verify that the directory row does not exist.
        Directory fetchedDirectory = directoryDAO.findById(directoryId);
        assertNull("Unexpected Directory exists. ", fetchedDirectory);

        DirectoryLocation fetchedDirectoryLocation = directoryLocationDAO.findById(directoryLocationId);
        assertNull("Unexpected DirectoryLocation exists. ", fetchedDirectoryLocation);
    }
}
