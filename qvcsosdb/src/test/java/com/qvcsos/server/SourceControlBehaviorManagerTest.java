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
package com.qvcsos.server;

import com.qumasoft.qvcslib.BogusResponseObject;
import com.qvcsos.CommonTestHelper;
import com.qvcsos.server.dataaccess.CommitDAO;
import com.qvcsos.server.dataaccess.FileDAO;
import com.qvcsos.server.dataaccess.FileNameDAO;
import com.qvcsos.server.dataaccess.FileRevisionDAO;
import com.qvcsos.server.dataaccess.impl.CommitDAOImpl;
import com.qvcsos.server.dataaccess.impl.FileDAOImpl;
import com.qvcsos.server.dataaccess.impl.FileNameDAOImpl;
import com.qvcsos.server.dataaccess.impl.FileRevisionDAOImpl;
import com.qvcsos.server.datamodel.Commit;
import com.qvcsos.server.datamodel.File;
import com.qvcsos.server.datamodel.FileName;
import com.qvcsos.server.datamodel.FileRevision;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Jim Voris.
 */
public class SourceControlBehaviorManagerTest {
    /**
     * Create our logger object.
     */
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(SourceControlBehaviorManagerTest.class);

    private static DatabaseManager databaseManager;
    private static final List<java.io.File> testFileArray = new ArrayList<>();
    private static final List<Integer> testFileIdArray = new ArrayList<>();
    private static final List<Integer> testFileRevisionIdArray = new ArrayList<>();

    public SourceControlBehaviorManagerTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        CommonTestHelper.getCommonTestHelper().acquireSyncObject();
        CommonTestHelper.getCommonTestHelper().resetTestDatabaseViaPsqlScript();
        CommonTestHelper.getCommonTestHelper().resetQvcsosTestDatabaseViaPsqlScript();
        databaseManager = DatabaseManager.getInstance();
        databaseManager.initializeDatabase();

        // Guarantee the database has some files there...
        Integer branchId = 1;
        Integer projectId = 1;
        Integer directoryId = 1;
        SourceControlBehaviorManager instance = SourceControlBehaviorManager.getInstance();
        instance.setResponse(new BogusResponseObject());
        instance.setUserId(2);
        for (int i = 0; i < 10; i++) {
            testFileArray.add(createTestFile(i));
        }
        AtomicInteger mutableFileRevisionId = new AtomicInteger();
        BogusResponseObject response = new BogusResponseObject();
        DbTestHelper.beginTransaction(response);
        for (int i = 0; i < 4; i++) {
            Date now = new Date();
            Timestamp nowTimestamp = new Timestamp(now.getTime());
            Integer fileId = instance.addFile(branchId, projectId, directoryId, String.format("TestFile%d", i), testFileArray.get(i), null, null, nowTimestamp,
                    "Add file.", mutableFileRevisionId);
            testFileIdArray.add(fileId);
            testFileRevisionIdArray.add(mutableFileRevisionId.get());
        }
        DbTestHelper.endTransaction(response);
        databaseManager.closeConnection();
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        databaseManager.closeConnection();
        databaseManager.shutdownDatabase();
        CommonTestHelper.getCommonTestHelper().releaseSyncObject();
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testAll() throws Exception {
        testCreateProject();
        testCreateFeatureBranch();
        testAddFile();
        testAddFiles();
        testRenameTrunkFile();
        testRenameTrunkFileOnFeatureBranch();
        testMoveTrunkFile();
        testDeleteTrunkFile();
        testMoveAndRenameFile();
        testRenameTrunkDirectory();
        testRenameTrunkOnBranchDirectory();
        testMoveTrunkDirectory();
        testMoveTrunkOnBranchDirectory();
        testDeleteTrunkOnBranchDirectory();
        testGetDirectoryId();
        testDeleteBranchDirectory();
        testAddAndGetFileRevision();
    }

    /**
     * Test of createProject method, of class SourceControlBehaviorManager.
     * @throws java.lang.Exception
     */
    public void testCreateProject() throws Exception {
        LOGGER.info("createProject");
        BogusResponseObject response = new BogusResponseObject();
        DbTestHelper.beginTransaction(response);
        String projectName = "Another Project";
        SourceControlBehaviorManager instance = SourceControlBehaviorManager.getInstance();
        Integer result = instance.createProject(projectName);
        DbTestHelper.endTransaction(response);
        assertNotNull("Expected non-null projectId", result);
    }

    public void testCreateFeatureBranch() throws SQLException {
        LOGGER.info("createFeatureBranch");
        BogusResponseObject response = new BogusResponseObject();
        DbTestHelper.beginTransaction(response);
        SourceControlBehaviorManager instance = SourceControlBehaviorManager.getInstance();
        Integer result = instance.createFeatureBranch("Functional Test Feature Branch", 1, 1);
        DbTestHelper.endTransaction(response);
        LOGGER.info("Feature branch id: [{}]", result);
        assertNotNull("Expected non-null branchId", result);
    }

    /**
     * Test of addFile method, of class SourceControlBehaviorManager.
     * @throws java.lang.Exception
     */
    public void testAddFile() throws Exception {
        LOGGER.info("addFile");
        BogusResponseObject response = new BogusResponseObject();
        DbTestHelper.beginTransaction(response);
        Integer branchId = 1;
        Integer projectId = 1;
        Integer directoryId = 1;
        String filename = "Test File";
        SourceControlBehaviorManager instance = SourceControlBehaviorManager.getInstance();
        Date now = new Date();
        Timestamp nowTimestamp = new Timestamp(now.getTime());
        Integer result = instance.addFile(branchId, projectId, directoryId, filename, testFileArray.get(4), null, null, nowTimestamp, "Add first file.", new AtomicInteger());
        DbTestHelper.endTransaction(response);
        assertNotNull("Expected non-null fileId", result);

        // Add a 2nd file.
        DbTestHelper.beginTransaction(response);
        filename = "File 2";
        result = instance.addFile(branchId, projectId, directoryId, filename, testFileArray.get(5), null, null, nowTimestamp, "Add another file.", new AtomicInteger());
        DbTestHelper.endTransaction(response);
        assertNotNull("Expected non-null fileId", result);
    }

    /**
     * Test of addFile method, here we add 2 files on the same commit.
     * @throws java.lang.Exception
     */
    public void testAddFiles() throws Exception {
        LOGGER.info("addFiles");
        BogusResponseObject response = new BogusResponseObject();
        DbTestHelper.beginTransaction(response);
        Integer branchId = 1;
        Integer projectId = 1;
        Integer directoryId = 1;
        String filename = "File 3";
        String schemaName = databaseManager.getSchemaName();
        Connection connection = databaseManager.getConnection();
        connection.setAutoCommit(false);
        Commit commit = new Commit();
        commit.setCommitMessage("Add multiple files.");
        commit.setUserId(2);
        CommitDAO commitDAO = new CommitDAOImpl(schemaName);
        Integer commitId = commitDAO.insert(commit);
        SourceControlBehaviorManager instance = SourceControlBehaviorManager.getInstance();
        Date now = new Date();
        Timestamp nowTimestamp = new Timestamp(now.getTime());
        Integer file6Id1 = instance.addFile(branchId, projectId, directoryId, filename, testFileArray.get(6), null, commitId, nowTimestamp, "Add first file.", new AtomicInteger());
        assertNotNull("Expected non-null fileId", file6Id1);

        // Add a revision to the file we just 'checked in'...
        Integer revisionId1 = instance.addRevision(branchId, file6Id1, testFileArray.get(7), commitId, new Date(), null);
        assertNotNull("Expected non-null revisionId1", revisionId1);

        // Add a 2nd file.
        filename = "File 4";
        Integer file8Id = instance.addFile(branchId, projectId, directoryId, filename, testFileArray.get(8), null, commitId, nowTimestamp, "Add another file.", new AtomicInteger());

        // Add a revision to the 2nd file we just 'checked in'...
        Integer revisionId2 = instance.addRevision(branchId, file8Id, testFileArray.get(2), commitId, new Date(), null);
        connection.commit();

        assertNotNull("Expected non-null revisionId2", revisionId2);
        assertNotNull("Expected non-null fileId", file8Id);
        assertNotEquals("Unexpected match of returned fileIds", file6Id1, file8Id);
        assertNotEquals("Unexpected match of returned revisionIds", revisionId1, revisionId2);
        DbTestHelper.endTransaction(response);
    }

    public void testRenameTrunkFile() throws Exception {
        LOGGER.info("testRenameTrunkFile");
        BogusResponseObject response = new BogusResponseObject();
        DbTestHelper.beginTransaction(response);
        SourceControlBehaviorManager instance = SourceControlBehaviorManager.getInstance();
        Integer fileNameId = instance.renameFile(1, 1, "New filename from functional test.");
        DbTestHelper.endTransaction(response);
        assertNotNull("Expected non-null fileNameId", fileNameId);
    }

    public void testRenameTrunkFileOnFeatureBranch() throws Exception {
        LOGGER.info("testRenameTrunkFileOnFeatureBranch");
        BogusResponseObject response = new BogusResponseObject();
        DbTestHelper.beginTransaction(response);
        SourceControlBehaviorManager instance = SourceControlBehaviorManager.getInstance();
        Integer fileNameId = instance.renameFile(2, 1, "New branch filename from functional test.");
        assertNotNull("Expected non-null fileNameId", fileNameId);

        FileNameDAO fileNameDAO = new FileNameDAOImpl(databaseManager.getSchemaName());
        FileName fileName = fileNameDAO.findById(fileNameId);
        FileDAO fileDAO = new FileDAOImpl(databaseManager.getSchemaName());
        File file = fileDAO.findById(fileName.getFileId());
        assertEquals("Expected matching fileIds", file.getId(), fileName.getFileId());

        // Rename the file again. This should NOT create a new file object, or a new fileName object.
        Integer secondFileNameId = instance.renameFile(2, fileName.getFileId(), "Yet another branch filename");
        LOGGER.info("2nd fileNameId: [{}]", secondFileNameId);
        DbTestHelper.endTransaction(response);
        assertEquals("Expected identical fileNameIds", fileNameId, secondFileNameId);
    }

    public void testMoveTrunkFile() throws Exception {
        LOGGER.info("testMoveTrunkFile");
        BogusResponseObject response = new BogusResponseObject();
        DbTestHelper.beginTransaction(response);
        SourceControlBehaviorManager instance = SourceControlBehaviorManager.getInstance();
        Integer fileNameId = instance.moveFile(1, 2, 2);
        assertNotNull("Expected non-null fileNameId", fileNameId);

        Integer branchfileNameId = instance.moveFile(2, fileNameId, 2);
        DbTestHelper.endTransaction(response);
        assertNotNull("Expected non-null fileNameId", branchfileNameId);
        assertNotEquals("Expected unequal branch fileNameId", branchfileNameId, fileNameId);
    }

    public void testDeleteTrunkFile() throws Exception {
        LOGGER.info("testDeleteTrunkFile");
        BogusResponseObject response = new BogusResponseObject();
        DbTestHelper.beginTransaction(response);
        SourceControlBehaviorManager instance = SourceControlBehaviorManager.getInstance();
        Integer fileNameId = instance.deleteFile(1, 3);
        assertNotNull("Expected non-null fileNameId", fileNameId);
        assertEquals("Expected matching fileNameId's", fileNameId, Integer.valueOf(3));

        Integer fileNameId4 = instance.deleteFile(2, 4);
        DbTestHelper.endTransaction(response);
        assertNotNull("Expected non-null fileNameId", fileNameId4);
        assertNotEquals("Expected non-matching fileNameId's", fileNameId4, Integer.valueOf(4));
    }

    public void testMoveAndRenameFile() throws Exception {
        LOGGER.info("testMoveAndRenameTrunkFile");
        BogusResponseObject response = new BogusResponseObject();
        DbTestHelper.beginTransaction(response);
        SourceControlBehaviorManager instance = SourceControlBehaviorManager.getInstance();
        Integer returnedfileNameId = instance.moveAndRenameFile(1, 6, 5, "NewNameForMovedFile");
        assertEquals("Expected matching fileNameId's", Integer.valueOf(6), returnedfileNameId);

        Integer branchFileNameId = instance.moveAndRenameFile(2, returnedfileNameId, 4, "NewNameOnBranch");
        DbTestHelper.endTransaction(response);
        assertNotEquals("Expected non-matching fileNameId's", branchFileNameId, returnedfileNameId);
    }

    public void testRenameTrunkDirectory() throws Exception {
        LOGGER.info("testRenameTrunkDirectory");
        BogusResponseObject response = new BogusResponseObject();
        DbTestHelper.beginTransaction(response);
        SourceControlBehaviorManager instance = SourceControlBehaviorManager.getInstance();
        Integer returnedDirectoryLocationId = instance.renameDirectory(1, 2, "Functional test new directory name.");
        DbTestHelper.endTransaction(response);
        assertEquals("Expected matching directoryLocationId's", returnedDirectoryLocationId, Integer.valueOf(2));
    }

    public void testRenameTrunkOnBranchDirectory() throws Exception {
        LOGGER.info("testRenameTrunkOnBranchDirectory");
        BogusResponseObject response = new BogusResponseObject();
        DbTestHelper.beginTransaction(response);
        SourceControlBehaviorManager instance = SourceControlBehaviorManager.getInstance();
        Integer returnedDirectoryLocationId = instance.renameDirectory(2, 3, "Functional test new directory name on branch.");
        DbTestHelper.endTransaction(response);
        assertNotEquals("Expected non-matching directoryLocationId's", returnedDirectoryLocationId, Integer.valueOf(2));
    }

    public void testMoveTrunkDirectory() throws SQLException {
        LOGGER.info("testMoveTrunkDirectory");
        BogusResponseObject response = new BogusResponseObject();
        DbTestHelper.beginTransaction(response);
        SourceControlBehaviorManager instance = SourceControlBehaviorManager.getInstance();
        Integer returnedDirectoryLocationId = instance.moveDirectory(4, 5);
        DbTestHelper.endTransaction(response);
        assertEquals("Expected matching directoryLocationId's", Integer.valueOf(4), returnedDirectoryLocationId);
    }

    public void testMoveTrunkOnBranchDirectory() throws SQLException {
        LOGGER.info("testMoveTrunkOnBranchDirectory");
        BogusResponseObject response = new BogusResponseObject();
        DbTestHelper.beginTransaction(response);
        SourceControlBehaviorManager instance = SourceControlBehaviorManager.getInstance();
        Integer returnedDirectoryLocationId = instance.moveDirectory(5, 7);
        DbTestHelper.endTransaction(response);
        assertNotEquals("Expected non-matching directoryLocationId's", Integer.valueOf(7), returnedDirectoryLocationId);
    }

    public void testDeleteTrunkOnBranchDirectory() throws SQLException {
        LOGGER.info("testDeleteTrunkDirectory");
        BogusResponseObject response = new BogusResponseObject();
        DbTestHelper.beginTransaction(response);
        SourceControlBehaviorManager instance = SourceControlBehaviorManager.getInstance();
        Integer returnedDirectoryLocationId = instance.deleteDirectory(2, 6);
        DbTestHelper.endTransaction(response);
        assertNotEquals("Expected non-matching directoryLocationId's", Integer.valueOf(5), returnedDirectoryLocationId);
    }

    public void testGetDirectoryId() throws SQLException {
        LOGGER.info("testGetDirectoryId");
        BogusResponseObject response = new BogusResponseObject();
        DbTestHelper.beginTransaction(response);
        SourceControlBehaviorManager instance = SourceControlBehaviorManager.getInstance();
        Integer returnedDirectoryId = instance.getDirectoryId("Test Project", "Trunk", "2nd Scripted Child Directory Name");
        DbTestHelper.endTransaction(response);
        assertEquals("Expected directoryId of 3", Integer.valueOf(3), returnedDirectoryId);
    }

    public void testDeleteBranchDirectory() throws SQLException {
        LOGGER.info("testDeleteBranchDirectory");
        BogusResponseObject response = new BogusResponseObject();
        DbTestHelper.beginTransaction(response);
        SourceControlBehaviorManager instance = SourceControlBehaviorManager.getInstance();
        // Note that directory_location 8 is already on branch 2.
        Integer returnedDirectoryLocationId = instance.deleteDirectory(2, 8);
        DbTestHelper.endTransaction(response);
        assertEquals("Expected matching directoryLocationId's", returnedDirectoryLocationId, Integer.valueOf(8));
    }

    public void testAddAndGetFileRevision() throws SQLException, IOException {
        LOGGER.info("testAddAndGetFileRevision");
        BogusResponseObject response = new BogusResponseObject();
        DbTestHelper.beginTransaction(response);
        String userDir = System.getProperty("user.dir");

        // Populate all the revisions first.
        String[] floridaCSVFileNames = {
            "revisionTestFiles/florida_by_date-1.0.csv",
            "revisionTestFiles/florida_by_date-1.1.csv",
            "revisionTestFiles/florida_by_date-1.2.csv",
            "revisionTestFiles/florida_by_date-1.3.csv",
            "revisionTestFiles/florida_by_date-1.4.csv",
            "revisionTestFiles/florida_by_date-1.5.csv",
            "revisionTestFiles/florida_by_date-1.6.csv",
            "revisionTestFiles/florida_by_date-1.7.csv",
            "revisionTestFiles/florida_by_date-1.8.csv",
            "revisionTestFiles/florida_by_date-1.9.csv",
            "revisionTestFiles/florida_by_date-1.10.csv",
            "revisionTestFiles/florida_by_date-1.11.csv"
        };
        Integer branchId = 1;
        Integer projectId = 1;
        Integer directoryId = 1;
        String filename = "florida_by_date.csv";
        SourceControlBehaviorManager instance = SourceControlBehaviorManager.getInstance();
        String schemaName = databaseManager.getSchemaName();
        FileRevisionDAO fileRevisionDAO = new FileRevisionDAOImpl(schemaName);
        java.io.File florida_by_date = new java.io.File(userDir + java.io.File.separator + floridaCSVFileNames[0]);
        Date now = new Date();
        Timestamp nowTimestamp = new Timestamp(now.getTime());
        Integer fileId = instance.addFile(branchId, projectId, directoryId, filename, florida_by_date, null, null, nowTimestamp, "Florida mortality data.", new AtomicInteger());
        List<java.io.File> createRevisionFileList = new ArrayList<>();
        createRevisionFileList.add(florida_by_date);

        for (int i = 1; i < 12; i++) {
            java.io.File revisionFile = new java.io.File(userDir + java.io.File.separator + floridaCSVFileNames[i]);
            createRevisionFileList.add(revisionFile);
            Integer revisionId = instance.addRevision(1, fileId, revisionFile, null, new Date(), "Adding revision: " + i);
            LOGGER.info("Added revision [{}] creating revisonId: [{}] using file: [{}]", i, revisionId, floridaCSVFileNames[i]);
        }
        DbTestHelper.endTransaction(response);

        // Get the list of FileRevisions... newest to oldest.
        int index = 11;
        List<FileRevision> fileRevisionList = fileRevisionDAO.findFileRevisions("1", fileId);
        for (FileRevision fileRevision : fileRevisionList) {
            java.io.File fetchRevisionFile = instance.getFileRevision(fileRevision.getId());
            java.io.File usedToCreateRevision = createRevisionFileList.get(index--);

            // Compare the fetched file to the file that we used to create the revision.
            boolean compareFlag = DbTestHelper.compareFilesByteForByte(fetchRevisionFile, usedToCreateRevision);
            LOGGER.info("Compared these files: [{}] to [{}]", fetchRevisionFile.getCanonicalPath(), usedToCreateRevision.getCanonicalPath());
            assertTrue("file are different!", compareFlag);
        }
    }

    private static java.io.File createTestFile(int i) {
        java.io.File tempFile = null;
        try {
            tempFile = java.io.File.createTempFile("qvcsos-", ".tmp");
            String fileData = String.format("This is some file data %d", i);
            Files.write(tempFile.toPath(), fileData.getBytes());
            tempFile.deleteOnExit();
        } catch (IOException e) {
            LOGGER.warn("Problem creating test file.", e);
        }
        return tempFile;
    }
}
