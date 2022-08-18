/*   Copyright 2004-2022 Jim Voris
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.qumasoft.qvcslib;

import com.qumasoft.TestHelper;
import com.qumasoft.server.QVCSEnterpriseServer;
import com.qvcsos.CommonTestHelper;
import com.qvcsos.server.DatabaseManager;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.junit.AfterClass;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test the QVCS custom ant task.
 *
 * @author Jim Voris
 */
public class QVCSAntTaskServerTest {
    private static DatabaseManager databaseManager;

    private static final Logger LOGGER = LoggerFactory.getLogger(QVCSAntTaskServerTest.class);
    private static final String TEST_SUBDIRECTORY = "AntQVCSTestFiles";
    private static Object serverSyncObject = null;
    private static final long ONE_SECOND = 1000L;

    /**
     * Default constructor.
     */
    public QVCSAntTaskServerTest() {
    }

    /**
     * Execute this stuff once when the class is loaded.
     *
     * @throws Exception if we have a problem with initialization.
     */
    @BeforeClass
    public static void setUpClass() throws Exception {
        LOGGER.info("Starting test class");
        while (QVCSEnterpriseServer.getServerIsRunningFlag()) {
            // We need to wait for the server to exit.
            LOGGER.info("Waiting for server to exit.");
            Thread.sleep(ONE_SECOND);
        }
        CommonTestHelper.getCommonTestHelper().acquireSyncObject();
        CommonTestHelper.getCommonTestHelper().resetTestDatabaseViaPsqlScript();
        CommonTestHelper.getCommonTestHelper().resetQvcsosTestDatabaseViaPsqlScript();
        databaseManager = DatabaseManager.getInstance();
        databaseManager.initializeDatabase();
        TestHelper.addUserToDatabase(TestHelper.USER_NAME, TestHelper.PASSWORD);
        TestHelper.updateAdminPassword();
        TestHelper.addTestFilesToTestProject();
        TestHelper.initClientBranchManager();
        // Only the server should have a db connection. We use the db only to set things up before starting the test.
        databaseManager.closeConnection();
        databaseManager.shutdownDatabase();
        serverSyncObject = TestHelper.startServer();
    }

    /**
     * Execute this just once after we complete all the tests defined in this class.
     *
     * @throws Exception if we have a problem tearing things down.
     */
    @AfterClass
    public static void tearDownClass() throws Exception {
        TestHelper.stopServerByMessage();
        CommonTestHelper.getCommonTestHelper().releaseSyncObject();
        LOGGER.info("Ending test class");
    }

    /**
     * Set up the things common to all the tests.
     * @param testName the name of the test.
     */
    public void setUp(String testName) {
        LOGGER.info("############################# Starting test: [{}] #####################################", testName);
    }

    private QVCSAntTask initQVCSAntTask(String testName) throws InterruptedException {
        // Add some time so the transport proxy can get shut down from previous call.
        Thread.sleep(2000L);
        QVCSAntTask qvcsAntTask = new QVCSAntTask();
        qvcsAntTask.setUserName(TestHelper.USER_NAME);
        qvcsAntTask.setPassword(TestHelper.PASSWORD);
        qvcsAntTask.setUserDirectory(System.getProperty("user.dir"));
        qvcsAntTask.setProjectName(TestHelper.getTestProjectName());
        qvcsAntTask.setServerName(TestHelper.SERVER_NAME);
        qvcsAntTask.setAppendedPath("");
        qvcsAntTask.setWorkfileLocation(TestHelper.buildTestDirectoryName(TEST_SUBDIRECTORY + File.separator + testName));
        qvcsAntTask.setProject(new Project());
        qvcsAntTask.init();
        return qvcsAntTask;
    }

    /**
     * Test of execute method, of class QVCSAntTask.
     */
    @Test
    public void testGet() {
        setUp("testGet");
        try {
            QVCSAntTask qvcsAntTask = initQVCSAntTask("testGet");
            qvcsAntTask.setOperation("get");
            qvcsAntTask.execute();
            File testDirectory = new File(TestHelper.buildTestDirectoryName(TEST_SUBDIRECTORY + File.separator + "testGet"));
            File[] files = testDirectory.listFiles();
            assertTrue("Nothing was fetched!", files.length > 0);

            // Make sure we recursed into the directory tree.
            boolean foundSubdirectory = false;
            for (File file : files) {
                if (file.isDirectory()) {
                    foundSubdirectory = true;
                    File[] subFiles = file.listFiles();
                    assertTrue("Empty subdirectory", subFiles.length > 0);
                }
            }
            if (!foundSubdirectory) {
                fail("No subdirectories fetched.");
            }
        } catch (BuildException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
            fail("Caught unexpected build exception." + e.getLocalizedMessage());
        } catch (InterruptedException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
            fail("Caught interrupted interrupted exception." + e.getLocalizedMessage());
        }
    }

    /**
     * Test of execute method, of class QVCSAntTask.
     */
    @Test
    public void testGetByFileExtension() {
        setUp("testGetByFileExtension");
        try {
            QVCSAntTask qvcsAntTask = initQVCSAntTask("testGetByFileExtension");
            qvcsAntTask.setOperation("get");
            qvcsAntTask.setFileExtension("java");
            qvcsAntTask.execute();
            File testDirectory = new File(TestHelper.buildTestDirectoryName(TEST_SUBDIRECTORY + File.separator + "testGetByFileExtension"));
            File[] files = testDirectory.listFiles();
            assertTrue("Nothing was fetched!", files.length > 0);
        } catch (BuildException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
            fail("Caught unexpected exception.");
        } catch (InterruptedException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
            fail("Caught interrupted exception.");
        }
    }

    /**
     * Test of execute method, of class QVCSAntTask.
     */
    @Test
    public void testGetCheckInAndGet() {
        setUp("testGetCheckInAndGet");
        try {
            String fileNameForThisTest = "QVCSEnterpriseServer.java";

            QVCSAntTask getAntTask = initQVCSAntTask("testGetCheckInAndGet");
            getAntTask.setOverWriteFlag(true);
            getAntTask.setOperation("get");
            getAntTask.setFileName(fileNameForThisTest);
            getAntTask.setRecurseFlag(false);
            getAntTask.execute();

            File file1 = new File(TestHelper.buildTestDirectoryName(TEST_SUBDIRECTORY) + File.separator + "testGetCheckInAndGet" + File.separator + fileNameForThisTest);
            if (!file1.exists()) {
                fail("Test file is missing: " + fileNameForThisTest);
            }

            File file2 = new File(TestHelper.buildTestDirectoryName(TEST_SUBDIRECTORY) + File.separator + "testGetCheckInAndGet" + File.separator + "OriginalServer.java");
            String userDir = System.getProperty("user.dir");
            File file3 = new File(userDir + File.separator + "Serverb.java");

            TestHelper.copyFile(file1, file2);
            TestHelper.copyFile(file3, file1);

            QVCSAntTask checkInAntTask = initQVCSAntTask("testGetCheckInAndGet");
            checkInAntTask.setOperation("checkin");
            checkInAntTask.setCheckInComment("testGetCheckInAndGet checkin");
            checkInAntTask.setFileName(fileNameForThisTest);
            checkInAntTask.setRecurseFlag(false);
            checkInAntTask.execute();

            emptyTestDirectory();
            getAntTask = initQVCSAntTask("testGetCheckInAndGet");
            getAntTask.execute();

            // Compare fetched file with file that was checked in to verify that it matches byte for byte
            file1 = new File(TestHelper.buildTestDirectoryName(TEST_SUBDIRECTORY) + File.separator + "testGetCheckInAndGet" + File.separator + fileNameForThisTest);
            assertTrue("Files not the same", TestHelper.compareFilesByteForByte(file1, file3));
        } catch (FileNotFoundException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
            fail("File not found exception:" + Utility.expandStackTraceToString(e));
        } catch (IOException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
            fail("IO exception:" + Utility.expandStackTraceToString(e));
        } catch (BuildException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
            fail("Caught unexpected exception:" + Utility.expandStackTraceToString(e));
        } catch (InterruptedException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
            fail("Caught interrupted exception.");
        }
    }

    /**
     * Test of execute method, of class QVCSAntTask.
     */
    @Test
    public void testReport() {
        setUp("testReport");
        try {
            QVCSAntTask reportAntTask = initQVCSAntTask("testReport");
            reportAntTask.setOperation("report");
            reportAntTask.execute();
        }
        catch (BuildException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
            fail("Caught unexpected exception:" + Utility.expandStackTraceToString(e));
        }
        catch (InterruptedException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
            fail("Caught interrupted exception.");
        }
    }

    /**
     * Test of execute method, of class QVCSAntTask.
     */
    @Test
    public void testReportWithCurrentStatus() {
        setUp("testReportWithCurrentStatus");
        try {
            QVCSAntTask getAntTask = initQVCSAntTask("testReportWithCurrentStatus");
            getAntTask.setOperation("get");
            getAntTask.execute();

            QVCSAntTask reportAntTask = initQVCSAntTask("testReportWithCurrentStatus");
            reportAntTask.setReportFilesWithStatus("Current");
            reportAntTask.setOperation("report");
            reportAntTask.execute();
        }
        catch (BuildException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
            fail("Caught unexpected exception:" + Utility.expandStackTraceToString(e));
        }
        catch (InterruptedException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
            fail("Caught interrupted exception.");
        }
    }

    @Test
    public void testReportOnFeatureBranch() {
        setUp("testReportOnFeatureBranch");
        try {
            QVCSAntTask getAntTask = initQVCSAntTask("testReportOnFeatureBranch");
            getAntTask.setBranchName(TestHelper.getFeatureBranchName());
            getAntTask.setOperation("get");
            getAntTask.execute();

            QVCSAntTask reportAntTask = initQVCSAntTask("testReportOnFeatureBranch");
            reportAntTask.setBranchName(TestHelper.getFeatureBranchName());
            reportAntTask.setReportFilesWithStatus("Current");
            reportAntTask.setOperation("report");
            reportAntTask.execute();
        }
        catch (BuildException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
            fail("Caught unexpected exception:" + Utility.expandStackTraceToString(e));
        }
        catch (InterruptedException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
            fail("Caught interrupted exception.");
        }
    }

    @Test
    public void testMoveFileOnTrunk() {
        setUp("testMoveFileOnTrunk");
        try {
            LOGGER.info("======================================== Before move 320:");

            QVCSAntTask getAntTask = initQVCSAntTask("testMoveFileOnTrunk");
            getAntTask.setOperation("get");
            getAntTask.execute();
            File testDirectory = new File(TestHelper.buildTestDirectoryName(TEST_SUBDIRECTORY) + File.separator + "testMoveFileOnTrunk");
            File[] files = testDirectory.listFiles();
            assertTrue("Nothing was fetched!", files.length > 0);

            QVCSAntTask moveAntTask = initQVCSAntTask("testMoveFileOnTrunk");
            moveAntTask.setOperation(QVCSAntTask.OPERATION_MOVE);
            moveAntTask.setFileName("Server.java");
            moveAntTask.setAppendedPath("");
            moveAntTask.setMoveToAppendedPath(TestHelper.SUBPROJECT_DIR_NAME);
            moveAntTask.execute();
            LOGGER.info("======================================== After move 335:");

            emptyTestDirectory();
            getAntTask = initQVCSAntTask("testMoveFileOnTrunk");
            getAntTask.setOperation("get");
            getAntTask.execute();

            Thread.sleep(1000);
            File movedFile = new File(TestHelper.buildTestDirectoryName(TEST_SUBDIRECTORY) + File.separator + "testMoveFileOnTrunk" + File.separator
                    + TestHelper.SUBPROJECT_DIR_NAME + File.separator + "Server.java");
            assertTrue("File not moved.", movedFile.exists());
        }
        catch (BuildException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
            fail("Caught build exception:" + Utility.expandStackTraceToString(e));
        }
        catch (InterruptedException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
            fail("Caught interrupted exception.");
        }
    }

    @Test
    public void testMoveFileOnBranch() {
        setUp("testMoveFileOnBranch");
        try {
            LOGGER.info("======================================== Before move 368:");

            QVCSAntTask getAntTask = initQVCSAntTask("testMoveFileOnBranch");
            getAntTask.setOperation(QVCSAntTask.OPERATION_GET);
            getAntTask.setBranchName(TestHelper.getFeatureBranchName());
            getAntTask.execute();
            File testDirectory = new File(TestHelper.buildTestDirectoryName(TEST_SUBDIRECTORY) + File.separator + "testMoveFileOnBranch");
            File[] files = testDirectory.listFiles();
            assertTrue("Nothing was fetched!", files.length > 0);

            QVCSAntTask moveAntTask = initQVCSAntTask("testMoveFileOnBranch");
            moveAntTask.setOperation(QVCSAntTask.OPERATION_MOVE);
            moveAntTask.setBranchName(TestHelper.getFeatureBranchName());
            moveAntTask.setFileName("Server.java");
            moveAntTask.setAppendedPath(TestHelper.SUBPROJECT_DIR_NAME);
            moveAntTask.setMoveToAppendedPath("");
            moveAntTask.execute();
            LOGGER.info("======================================== After move 385:");

            emptyTestDirectory();
            getAntTask = initQVCSAntTask("testMoveFileOnBranch");
            getAntTask.setOperation(QVCSAntTask.OPERATION_GET);
            getAntTask.setBranchName(TestHelper.getFeatureBranchName());
            getAntTask.execute();

            Thread.sleep(1000);
            File movedFile = new File(TestHelper.buildTestDirectoryName(TEST_SUBDIRECTORY) + File.separator + "testMoveFileOnBranch" + File.separator + "Server.java");
            assertTrue("File not moved.", movedFile.exists());
        }
        catch (BuildException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
            fail("Caught build exception.");
        }
        catch (InterruptedException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
            fail("Caught interrupted exception.");
        }
    }

    @Test
    public void testRenameOnTrunk() {
        setUp("testRenameOnTrunk");
        try {
            QVCSAntTask renameAntTask = initQVCSAntTask("testRenameOnTrunk");
            renameAntTask.setFileName(TestHelper.BASE_DIR_SHORTWOFILENAME_A);
            renameAntTask.setRenameToFileName(TestHelper.BASE_DIR_SHORTWOFILENAME_A + ".Renamed");
            renameAntTask.setOperation("rename");
            renameAntTask.execute();

            // Fetch the renamed file.
            emptyTestDirectory();
            QVCSAntTask getTask = initQVCSAntTask("testRenameOnTrunk");
            getTask.execute();

            // See if the renamed file exists.
            File renamedFile = new File(TestHelper.buildTestDirectoryName(TEST_SUBDIRECTORY) + File.separator + "testRenameOnTrunk" + File.separator
                    + TestHelper.BASE_DIR_SHORTWOFILENAME_A + ".Renamed");
            assertTrue("Renamed file missing.", renamedFile.exists());
        }
        catch (BuildException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
            fail("Caught unexpected exception.");
        }
        catch (InterruptedException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
            fail("Caught interrupted exception.");
        }
    }

    @Test
    public void testRenameOnFeatureBranch() {
        setUp("testRenameOnFeatureBranch");
        try {
            QVCSAntTask renameAntTask = initQVCSAntTask("testRenameOnFeatureBranch");
            renameAntTask.setBranchName(TestHelper.getFeatureBranchName());
            renameAntTask.setFileName(TestHelper.BASE_DIR_SHORTWOFILENAME_B);
            renameAntTask.setRenameToFileName(TestHelper.BASE_DIR_SHORTWOFILENAME_B + ".Renamed");
            renameAntTask.setOperation("rename");
            renameAntTask.execute();

            // Fetch the renamed file.
            emptyTestDirectory();
            QVCSAntTask getTask = initQVCSAntTask("testRenameOnFeatureBranch");
            getTask.setBranchName(TestHelper.getFeatureBranchName());
            getTask.execute();

            // See if the renamed file exists.
            File renamedFile = new File(TestHelper.buildTestDirectoryName(TEST_SUBDIRECTORY) + File.separator + "testRenameOnFeatureBranch" + File.separator
                    + TestHelper.BASE_DIR_SHORTWOFILENAME_B + ".Renamed");
            assertTrue("Renamed file missing.", renamedFile.exists());
        }
        catch (BuildException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
            fail("Caught unexpected exception.");
        }
        catch (InterruptedException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
            fail("Caught interrupted exception.");
        }
    }

    @Test
    public void testDeleteOnTrunk() {
        setUp("testDeleteOnTrunk");
        try {
            QVCSAntTask getTask = initQVCSAntTask("testDeleteOnTrunk");
            getTask.execute();

            QVCSAntTask deleteAntTask = initQVCSAntTask("testDeleteOnTrunk");
            String fileToDelete = TestHelper.BASE_DIR_SHORTWOFILENAME_B;
            deleteAntTask.setFileName(fileToDelete);
            deleteAntTask.setOperation("delete");
            deleteAntTask.execute();

            // Fetch the latest from the server.
            emptyTestDirectory();
            getTask = initQVCSAntTask("testDeleteOnTrunk");
            getTask.execute();

            Thread.sleep(1000);
            File deletedFile = new File(TestHelper.buildTestDirectoryName(TEST_SUBDIRECTORY) + File.separator + "testDeleteOnTrunk" + File.separator + fileToDelete);
            assertTrue("file not deleted", !deletedFile.exists());
        }
        catch (BuildException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
            fail("Caught unexpected exception.");
        }
        catch (InterruptedException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
            fail("Caught interrupted exception.");
        }
    }

    @Test
    public void testDeleteOnFeatureBranch() {
        setUp("testDeleteOnFeatureBranch");
        try {
            QVCSAntTask getTask = initQVCSAntTask("testDeleteOnFeatureBranch");
            getTask.setBranchName(TestHelper.getFeatureBranchName());
            getTask.execute();

            QVCSAntTask deleteAntTask = initQVCSAntTask("testDeleteOnFeatureBranch");
            deleteAntTask.setBranchName(TestHelper.getFeatureBranchName());
            String fileToDelete = TestHelper.BASE_DIR_SHORTWOFILENAME_B + ".Renamed";
            deleteAntTask.setFileName(fileToDelete);
            deleteAntTask.setOperation("delete");
            deleteAntTask.execute();

            // Fetch the latest from the server.
            emptyTestDirectory();
            getTask = initQVCSAntTask("testDeleteOnFeatureBranch");
            getTask.setBranchName(TestHelper.getFeatureBranchName());
            getTask.execute();

            Thread.sleep(1000);
            File deletedFile = new File(TestHelper.buildTestDirectoryName(TEST_SUBDIRECTORY) + File.separator + "testDeleteOnFeatureBranch" + File.separator + fileToDelete);
            assertTrue("file not deleted", !deletedFile.exists());
        }
        catch (BuildException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
            fail("Caught unexpected exception.");
        }
        catch (InterruptedException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
            fail("Caught interrupted exception.");
        }
    }

    /**
     * Clean out the test directory. This is not fully recursive, since we don't want a run-away delete to wipe out all the contents of the disk by mistake.
     */
    private void emptyTestDirectory() {
        // Delete the files in the /tmp/QVCSTestFiles directory.
        File tempDirectory = new File(TestHelper.buildTestDirectoryName(TEST_SUBDIRECTORY));
        File[] files = tempDirectory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    File[] subFiles = file.listFiles();
                    for (File subFile : subFiles) {
                        if (subFile.isDirectory()) {
                            File[] subSubFiles = subFile.listFiles();
                            for (File subSubFile : subSubFiles) {
                                subSubFile.delete();
                            }
                        }
                        subFile.delete();
                    }
                }
                file.delete();
            }
        }
    }
}
