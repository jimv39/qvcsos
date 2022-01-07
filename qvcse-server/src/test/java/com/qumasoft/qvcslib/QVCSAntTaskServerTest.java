/*   Copyright 2004-2021 Jim Voris
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
import com.qumasoft.clientapi.ClientAPI;
import com.qumasoft.clientapi.ClientAPIContext;
import com.qumasoft.clientapi.ClientAPIException;
import com.qumasoft.clientapi.ClientAPIFactory;
import com.qumasoft.clientapi.FileInfo;
import com.qumasoft.clientapi.RevisionInfo;
import com.qumasoft.server.ServerUtility;
import com.qvcsos.server.DatabaseManager;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.junit.AfterClass;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
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
        TestHelper.stopServerImmediately(null);
        TestHelper.resetTestDatabaseViaPsqlScript();
        TestHelper.resetQvcsosTestDatabaseViaPsqlScript();
        databaseManager = DatabaseManager.getInstance();
        databaseManager.initializeDatabase();
        TestHelper.addUserToDatabase(TestHelper.USER_NAME, TestHelper.PASSWORD);
        TestHelper.initRoleProjectBranchStore();
        TestHelper.addTestFilesToTestProject();
        TestHelper.initProjectProperties();
        TestHelper.initClientBranchManager();
        serverSyncObject = TestHelper.startServer();
    }

    /**
     * Execute this just once after we complete all the tests defined in this class.
     *
     * @throws Exception if we have a problem tearing things down.
     */
    @AfterClass
    public static void tearDownClass() throws Exception {
        TestHelper.stopServer(serverSyncObject);
        TestHelper.deleteBranchStore();
        TestHelper.removeArchiveFiles();
        LOGGER.info("Ending test class");
    }

    /**
     * Set up the things common to all the tests.
     * @param testName the name of the test.
     */
    public void setUp(String testName) {
        LOGGER.info("############################# Starting test: [{}] #####################################", testName);
        emptyTestDirectory();
    }

    private QVCSAntTask initQVCSAntTask() throws InterruptedException {
        // Add some time so the transport proxy can get shut down from previous call.
        Thread.sleep(2000);
        QVCSAntTask qvcsAntTask = new QVCSAntTask();
        qvcsAntTask.setUserName(TestHelper.USER_NAME);
        qvcsAntTask.setPassword(TestHelper.PASSWORD);
        qvcsAntTask.setUserDirectory(System.getProperty("user.dir"));
        qvcsAntTask.setProjectName(TestHelper.getTestProjectName());
        qvcsAntTask.setServerName(TestHelper.SERVER_NAME);
        qvcsAntTask.setAppendedPath("");
        qvcsAntTask.setWorkfileLocation(TestHelper.buildTestDirectoryName(TEST_SUBDIRECTORY));
        qvcsAntTask.setProject(new Project());
        qvcsAntTask.init();
        return qvcsAntTask;
    }

    @Test
    public void testAntTask() throws ClientAPIException {
        testGet();
        testGetByFileExtension();
        testGetCheckInAndGet();
        testReport();
        testReportWithCurrentStatus();
        testClientAPIGetMostRecentActivity();
        testReportOnFeatureBranch();
        testMoveFileOnTrunk();
        testMoveFileOnBranch();
        testRenameOnTrunk();
        testRenameOnFeatureBranch();
        testDeleteOnTrunk();
        testDeleteOnFeatureBranch();
    }

    /**
     * Test of execute method, of class QVCSAntTask.
     */
    public void testGet() {
        setUp("testGet");
        try {
            QVCSAntTask qvcsAntTask = initQVCSAntTask();
            qvcsAntTask.setOperation("get");
            qvcsAntTask.execute();
            File testDirectory = new File(TestHelper.buildTestDirectoryName(TEST_SUBDIRECTORY));
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
    public void testGetByFileExtension() {
        setUp("testGetByFileExtension");
        try {
            QVCSAntTask qvcsAntTask = initQVCSAntTask();
            qvcsAntTask.setOperation("get");
            qvcsAntTask.setFileExtension("java");
            qvcsAntTask.execute();
            File testDirectory = new File(TestHelper.buildTestDirectoryName(TEST_SUBDIRECTORY));
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
    public void testGetCheckInAndGet() {
        setUp("testGetCheckInAndGet");
        try {
            File file1 = new File(TestHelper.buildTestDirectoryName(TEST_SUBDIRECTORY) + File.separator + "Server.java");
            File file2 = new File(TestHelper.buildTestDirectoryName(TEST_SUBDIRECTORY) + File.separator + "OriginalServer.java");
            String userDir = System.getProperty("user.dir");
            File file3 = new File(userDir + File.separator + "Serverb.java");

            QVCSAntTask getAntTask = initQVCSAntTask();
            getAntTask.setOverWriteFlag(true);
            getAntTask.setOperation("get");
            getAntTask.setFileName("Server.java");
            getAntTask.setRecurseFlag(false);
            getAntTask.execute();

            ServerUtility.copyFile(file1, file2);
            ServerUtility.copyFile(file3, file1);

            QVCSAntTask checkInAntTask = initQVCSAntTask();
            checkInAntTask.setOperation("checkin");
            checkInAntTask.setCheckInComment("Test checkin");
            checkInAntTask.setFileName("Server.java");
            checkInAntTask.setRecurseFlag(false);
            checkInAntTask.execute();

            // Compare fetched file with file that was checked in to verify that it matches byte for byte
            assertTrue(TestHelper.compareFilesByteForByte(file1, file2));
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
    public void testReport() {
        setUp("testReport");
        try {
            QVCSAntTask reportAntTask = initQVCSAntTask();
            reportAntTask.setOperation("report");
            reportAntTask.execute();
        } catch (BuildException e) {
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
    public void testReportWithCurrentStatus() {
        setUp("testReportWithCurrentStatus");
        try {
            QVCSAntTask getAntTask = initQVCSAntTask();
            getAntTask.setOperation("get");
            getAntTask.execute();

            QVCSAntTask reportAntTask = initQVCSAntTask();
            reportAntTask.setReportFilesWithStatus("Current");
            reportAntTask.setOperation("report");
            reportAntTask.execute();
        } catch (BuildException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
            fail("Caught unexpected exception:" + Utility.expandStackTraceToString(e));
        }
        catch (InterruptedException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
            fail("Caught interrupted exception.");
        }
    }

    public void testClientAPIGetMostRecentActivity() throws ClientAPIException {
        setUp("testClientAPIGetMostRecentActivity");
        ClientAPIContext clientAPIContext = ClientAPIFactory.createClientAPIContext();
        clientAPIContext.setUserName(TestHelper.USER_NAME);
        clientAPIContext.setPassword(TestHelper.PASSWORD);
        clientAPIContext.setServerIPAddress("localhost");
        clientAPIContext.setPort(29889);
        clientAPIContext.setProjectName(TestHelper.getTestProjectName());
        clientAPIContext.setBranchName(QVCSConstants.QVCS_TRUNK_BRANCH);
        clientAPIContext.setAppendedPath("");
        ClientAPI instance = ClientAPIFactory.createClientAPI(clientAPIContext);
        Date mostRecentActivity = instance.getMostRecentActivity();
        assertNotNull(mostRecentActivity);
    }

    public void testReportOnFeatureBranch() {
        setUp("testReportOnFeatureBranch");
        try {
            QVCSAntTask getAntTask = initQVCSAntTask();
            getAntTask.setBranchName(TestHelper.getFeatureBranchName());
            getAntTask.setOperation("get");
            getAntTask.execute();

            QVCSAntTask reportAntTask = initQVCSAntTask();
            reportAntTask.setBranchName(TestHelper.getFeatureBranchName());
            reportAntTask.setReportFilesWithStatus("Current");
            reportAntTask.setOperation("report");
            reportAntTask.execute();
        } catch (BuildException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
            fail("Caught unexpected exception:" + Utility.expandStackTraceToString(e));
        }
        catch (InterruptedException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
            fail("Caught interrupted exception.");
        }
    }

    public void testMoveFileOnTrunk() {
        setUp("testMoveFileOnTrunk");
        try {
            String rootDirectoryName = System.getProperty(TestHelper.USER_DIR)
                    + File.separator
                    + QVCSConstants.QVCS_PROJECTS_DIRECTORY
                    + File.separator
                    + TestHelper.getTestProjectName();
            File rootDirectory = new File(rootDirectoryName);
            LOGGER.info("======================================== Before move:");

            QVCSAntTask getAntTask = initQVCSAntTask();
            getAntTask.setOperation("get");
            getAntTask.execute();
            File testDirectory = new File(TestHelper.buildTestDirectoryName(TEST_SUBDIRECTORY));
            File[] files = testDirectory.listFiles();
            assertTrue("Nothing was fetched!", files.length > 0);

            QVCSAntTask moveAntTask = initQVCSAntTask();
            moveAntTask.setOperation(QVCSAntTask.OPERATION_MOVE);
            moveAntTask.setFileName("Server.java");
            moveAntTask.setAppendedPath("");
            moveAntTask.setMoveToAppendedPath(TestHelper.SUBPROJECT_DIR_NAME);
            moveAntTask.execute();
            LOGGER.info("======================================== After move:");

            Thread.sleep(2000);
            ClientAPIContext clientAPIContext = ClientAPIFactory.createClientAPIContext();
            clientAPIContext.setUserName(TestHelper.USER_NAME);
            clientAPIContext.setPassword(TestHelper.PASSWORD);
            clientAPIContext.setServerIPAddress("localhost");
            clientAPIContext.setPort(29889);
            clientAPIContext.setProjectName(TestHelper.getTestProjectName());
            clientAPIContext.setBranchName(QVCSConstants.QVCS_TRUNK_BRANCH);
            clientAPIContext.setAppendedPath(TestHelper.SUBPROJECT_DIR_NAME);
            clientAPIContext.setFileName("Server.java");
            ClientAPI instance = ClientAPIFactory.createClientAPI(clientAPIContext);
            List<RevisionInfo> result = instance.getRevisionInfoList();
            assertTrue(result.size() > 0);
            String revisionDescription = result.get(0).getRevisionDescription();
            assertTrue("unexpected revision description for tip revision.", revisionDescription.startsWith("Moving file from directoryId"));
        } catch (ClientAPIException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
            fail("Caught client api exception:" + Utility.expandStackTraceToString(e));
        }
        catch (BuildException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
            fail("Caught build exception:" + Utility.expandStackTraceToString(e));
        } catch (InterruptedException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
            fail("Caught interrupted exception.");
        }
    }

    public void testMoveFileOnBranch() {
        setUp("testMoveFileOnBranch");
        try {
            String rootDirectoryName = System.getProperty(TestHelper.USER_DIR)
                    + File.separator
                    + QVCSConstants.QVCS_PROJECTS_DIRECTORY
                    + File.separator
                    + TestHelper.getTestProjectName();
            File rootDirectory = new File(rootDirectoryName);
            LOGGER.info("======================================== Before move:");

            QVCSAntTask getAntTask = initQVCSAntTask();
            getAntTask.setOperation(QVCSAntTask.OPERATION_GET);
            getAntTask.setBranchName(TestHelper.getFeatureBranchName());
            getAntTask.execute();
            File testDirectory = new File(TestHelper.buildTestDirectoryName(TEST_SUBDIRECTORY));
            File[] files = testDirectory.listFiles();
            assertTrue("Nothing was fetched!", files.length > 0);

            QVCSAntTask moveAntTask = initQVCSAntTask();
            moveAntTask.setOperation(QVCSAntTask.OPERATION_MOVE);
            moveAntTask.setBranchName(TestHelper.getFeatureBranchName());
            moveAntTask.setFileName("Server.java");
            moveAntTask.setAppendedPath(TestHelper.SUBPROJECT_DIR_NAME);
            moveAntTask.setMoveToAppendedPath("");
            moveAntTask.execute();
            LOGGER.info("======================================== After move:");

            Thread.sleep(2000);
            ClientAPIContext clientAPIContext = ClientAPIFactory.createClientAPIContext();
            clientAPIContext.setUserName(TestHelper.USER_NAME);
            clientAPIContext.setPassword(TestHelper.PASSWORD);
            clientAPIContext.setServerIPAddress("localhost");
            clientAPIContext.setPort(29889);
            clientAPIContext.setProjectName(TestHelper.getTestProjectName());
            clientAPIContext.setBranchName(TestHelper.getFeatureBranchName());
            clientAPIContext.setAppendedPath("");
            clientAPIContext.setFileName("Server.java");
            ClientAPI instance = ClientAPIFactory.createClientAPI(clientAPIContext);
            List<RevisionInfo> result = instance.getRevisionInfoList();
            assertTrue(result.size() > 0);
            String revisionDescription = result.get(0).getRevisionDescription();
            assertTrue("unexpected revision description for tip revision.", revisionDescription.startsWith("Moving file from directoryId"));
        } catch (ClientAPIException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
            fail("Caught client api exception.");
        }
        catch (BuildException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
            fail("Caught build exception.");
        } catch (InterruptedException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
            fail("Caught interrupted exception.");
        }
    }

    public void testRenameOnTrunk() {
        setUp("testRenameOnTrunk");
        try {
            QVCSAntTask renameAntTask = initQVCSAntTask();
            renameAntTask.setFileName(TestHelper.BASE_DIR_SHORTWOFILENAME_A);
            renameAntTask.setRenameToFileName(TestHelper.BASE_DIR_SHORTWOFILENAME_A + ".Renamed");
            renameAntTask.setOperation("rename");
            renameAntTask.execute();

            Thread.sleep(2000);
            ClientAPIContext clientAPIContext = ClientAPIFactory.createClientAPIContext();
            clientAPIContext.setUserName(TestHelper.USER_NAME);
            clientAPIContext.setPassword(TestHelper.PASSWORD);
            clientAPIContext.setServerIPAddress("localhost");
            clientAPIContext.setPort(29889);
            clientAPIContext.setProjectName(TestHelper.getTestProjectName());
            clientAPIContext.setBranchName(QVCSConstants.QVCS_TRUNK_BRANCH);
            clientAPIContext.setAppendedPath("");
            clientAPIContext.setFileName(TestHelper.BASE_DIR_SHORTWOFILENAME_A + ".Renamed");
            ClientAPI instance = ClientAPIFactory.createClientAPI(clientAPIContext);
            List<RevisionInfo> result = instance.getRevisionInfoList();
            assertTrue(result.size() > 0);
            String revisionDescription = result.get(0).getRevisionDescription();
            assertTrue("checkin comment missing expected text.", revisionDescription.equalsIgnoreCase("Renaming file from [ServerB.java] to [ServerB.java.Renamed]"));
        } catch (ClientAPIException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
            fail("Caught client api exception");
        }
        catch (BuildException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
            fail("Caught unexpected exception.");
        } catch (InterruptedException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
            fail("Caught interrupted exception.");
        }
    }

    public void testRenameOnFeatureBranch() {
        setUp("testRenameOnFeatureBranch");
        try {
            QVCSAntTask renameAntTask = initQVCSAntTask();
            renameAntTask.setBranchName(TestHelper.getFeatureBranchName());
            renameAntTask.setFileName(TestHelper.BASE_DIR_SHORTWOFILENAME_B);
            renameAntTask.setRenameToFileName(TestHelper.BASE_DIR_SHORTWOFILENAME_B + ".Renamed");
            renameAntTask.setOperation("rename");
            renameAntTask.execute();

            Thread.sleep(2000);
            ClientAPIContext clientAPIContext = ClientAPIFactory.createClientAPIContext();
            clientAPIContext.setUserName(TestHelper.USER_NAME);
            clientAPIContext.setPassword(TestHelper.PASSWORD);
            clientAPIContext.setServerIPAddress("localhost");
            clientAPIContext.setPort(29889);
            clientAPIContext.setProjectName(TestHelper.getTestProjectName());
            clientAPIContext.setBranchName(TestHelper.getFeatureBranchName());
            clientAPIContext.setAppendedPath("");
            clientAPIContext.setFileName(TestHelper.BASE_DIR_SHORTWOFILENAME_B + ".Renamed");
            ClientAPI instance = ClientAPIFactory.createClientAPI(clientAPIContext);
            List<RevisionInfo> result = instance.getRevisionInfoList();
            assertTrue(result.size() > 0);
            String revisionDescription = result.get(0).getRevisionDescription();
            assertTrue("unexpected revision description for tip revision.", revisionDescription.equalsIgnoreCase("Renaming file from [ServerC.java] to [ServerC.java.Renamed]"));
        } catch (ClientAPIException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
            fail("Caught client api exception");
        }
        catch (BuildException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
            fail("Caught unexpected exception.");
        } catch (InterruptedException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
            fail("Caught interrupted exception.");
        }
    }

    public void testDeleteOnTrunk() {
        setUp("testDeleteOnTrunk");
        try {
            QVCSAntTask deleteAntTask = initQVCSAntTask();
            String fileToDelete = TestHelper.BASE_DIR_SHORTWOFILENAME_A + ".Renamed";
            deleteAntTask.setFileName(fileToDelete);
            deleteAntTask.setOperation("delete");
            deleteAntTask.execute();

            Thread.sleep(2000);
            ClientAPIContext clientAPIContext = ClientAPIFactory.createClientAPIContext();
            clientAPIContext.setUserName(TestHelper.USER_NAME);
            clientAPIContext.setPassword(TestHelper.PASSWORD);
            clientAPIContext.setServerIPAddress("localhost");
            clientAPIContext.setPort(29889);
            clientAPIContext.setProjectName(TestHelper.getTestProjectName());
            clientAPIContext.setBranchName(QVCSConstants.QVCS_TRUNK_BRANCH);
            clientAPIContext.setAppendedPath("");
            ClientAPI instance = ClientAPIFactory.createClientAPI(clientAPIContext);
            List<FileInfo> result = instance.getFileInfoList();
            boolean foundFile = false;
            for (FileInfo fileInfo : result) {
                String fileName = fileInfo.getShortWorkfileName();
                if (fileName.equals(fileToDelete)) {
                    foundFile = true;
                    break;
                }
            }
            assertFalse("file not deleted", foundFile);
        } catch (ClientAPIException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
            fail("Caught client api exception");
        } catch (BuildException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
            fail("Caught unexpected exception.");
        } catch (InterruptedException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
            fail("Caught interrupted exception.");
        }
    }

    public void testDeleteOnFeatureBranch() {
        setUp("testDeleteOnFeatureBranch");
        try {
            QVCSAntTask deleteAntTask = initQVCSAntTask();
            deleteAntTask.setBranchName(TestHelper.getFeatureBranchName());
            String fileToDelete = TestHelper.BASE_DIR_SHORTWOFILENAME_B + ".Renamed";
            deleteAntTask.setFileName(fileToDelete);
            deleteAntTask.setOperation("delete");
            deleteAntTask.execute();

            Thread.sleep(2000);
            ClientAPIContext clientAPIContext = ClientAPIFactory.createClientAPIContext();
            clientAPIContext.setUserName(TestHelper.USER_NAME);
            clientAPIContext.setPassword(TestHelper.PASSWORD);
            clientAPIContext.setServerIPAddress("localhost");
            clientAPIContext.setPort(29889);
            clientAPIContext.setProjectName(TestHelper.getTestProjectName());
            clientAPIContext.setBranchName(TestHelper.getFeatureBranchName());
            clientAPIContext.setAppendedPath("");
            ClientAPI instance = ClientAPIFactory.createClientAPI(clientAPIContext);
            List<FileInfo> result = instance.getFileInfoList();
            boolean foundFile = false;
            for (FileInfo fileInfo : result) {
                String fileName = fileInfo.getShortWorkfileName();
                if (fileName.equals(fileToDelete)) {
                    foundFile = true;
                    break;
                }
            }
            assertFalse("file not deleted", foundFile);
        } catch (ClientAPIException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
            fail("Caught client api exception");
        } catch (BuildException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
            fail("Caught unexpected exception.");
        } catch (InterruptedException e) {
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
