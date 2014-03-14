/*   Copyright 2004-2014 Jim Voris
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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.junit.AfterClass;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test the QVCS custom ant task.
 *
 * @author Jim Voris
 */
public class QVCSAntTaskServerTest {

    private static final Logger LOGGER = Logger.getLogger("com.qumasoft.qvcslib");
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
        LOGGER.log(Level.INFO, "Starting test class");
        TestHelper.stopServerImmediately(null);
        TestHelper.removeArchiveFiles();
        TestHelper.deleteViewStore();
        TestHelper.initProjectProperties();
        TestHelper.initializeArchiveFiles();
        serverSyncObject = TestHelper.startServer();
        // We can't create the translucent branch until after the server has started, since the db has to be up in order to add the branch.
        TestHelper.initializeTranslucentBranch();
    }

    /**
     * Execute this just once after we complete all the tests defined in this class.
     *
     * @throws Exception if we have a problem tearing things down.
     */
    @AfterClass
    public static void tearDownClass() throws Exception {
        TestHelper.stopServer(serverSyncObject);
        TestHelper.deleteViewStore();
        TestHelper.removeArchiveFiles();
        LOGGER.log(Level.INFO, "Ending test class");
    }

    /**
     * Set up the things common to all the tests.
     */
    public void setUp() {
        LOGGER.log(Level.INFO, "Starting test");
        emptyTestDirectory();
    }

    private QVCSAntTask initQVCSAntTask() throws InterruptedException {
        // Add some time so the transport proxy can get shut down from previous call.
        Thread.sleep(1000);
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
        testLabel();
        testLockAndUnlock();
        testLockGetCheckInAndGet();
        testGetByLabel();
        testReport();
        testReportWithCurrentStatus();
        testClientAPIGetMostRecentActivity();
        testReportOnTranslucentBranch();
        testMoveFileOnTrunk();
        testMoveFileOnBranch();
        testRenameOnTrunk();
        testRenameOnTranslucentBranch();
        testDeleteOnTrunk();
        testDeleteOnTranslucentBranch();
    }

    /**
     * Test of execute method, of class QVCSAntTask.
     */
    public void testGet() {
        setUp();
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
            fail("Caught unexpected build exception." + e.getLocalizedMessage());
        } catch (InterruptedException e) {
            fail("Caught interrupted interrupted exception." + e.getLocalizedMessage());
        }
    }

    /**
     * Test of execute method, of class QVCSAntTask.
     */
    public void testGetByFileExtension() {
        setUp();
        try {
            QVCSAntTask qvcsAntTask = initQVCSAntTask();
            qvcsAntTask.setOperation("get");
            qvcsAntTask.setFileExtension("java");
            qvcsAntTask.execute();
            File testDirectory = new File(TestHelper.buildTestDirectoryName(TEST_SUBDIRECTORY));
            File[] files = testDirectory.listFiles();
            assertTrue("Nothing was fetched!", files.length > 0);
        } catch (BuildException e) {
            fail("Caught unexpected exception.");
        } catch (InterruptedException e) {
            fail("Caught interrupted exception.");
        }
    }

    /**
     * Test of execute method, of class QVCSAntTask.
     */
    public void testLabel() {
        setUp();
        try {
            QVCSAntTask qvcsAntTask = initQVCSAntTask();
            qvcsAntTask.setOperation("label");
            qvcsAntTask.setLabel("Test label");
            qvcsAntTask.execute();
        } catch (BuildException e) {
            fail("Caught unexpected exception.");
        } catch (InterruptedException e) {
            fail("Caught interrupted exception.");
        }
    }

    /**
     * Test of execute method, of class QVCSAntTask.
     */
    public void testLockAndUnlock() {
        setUp();
        try {
            QVCSAntTask lockAntTask = initQVCSAntTask();
            lockAntTask.setOperation("lock");
            lockAntTask.execute();

            QVCSAntTask unlockAntTask = initQVCSAntTask();
            unlockAntTask.setOperation("unlock");
            unlockAntTask.execute();
        } catch (BuildException e) {
            fail("Caught unexpected exception.");
        } catch (InterruptedException e) {
            fail("Caught interrupted exception.");
        }
    }

    /**
     * Test of execute method, of class QVCSAntTask.
     */
    public void testLockGetCheckInAndGet() {
        setUp();
        final String COMPARE_TEST_LABEL = "Compare Test Label";
        try {
            File file1 = new File(TestHelper.buildTestDirectoryName(TEST_SUBDIRECTORY) + File.separator + "Server.java");
            File file2 = new File(TestHelper.buildTestDirectoryName(TEST_SUBDIRECTORY) + File.separator + "OriginalServer.java");
            String userDir = System.getProperty("user.dir");
            File file3 = new File(userDir + File.separator + "Serverb.java");

            QVCSAntTask labelAntTask = initQVCSAntTask();
            labelAntTask.setOperation("label");
            labelAntTask.setLabel(COMPARE_TEST_LABEL);
            labelAntTask.setFileName("Server.java");
            labelAntTask.setRecurseFlag(false);
            labelAntTask.execute();

            QVCSAntTask getAntTask = initQVCSAntTask();
            getAntTask.setOverWriteFlag(true);
            getAntTask.setLabel(COMPARE_TEST_LABEL);
            getAntTask.setOperation("get");
            getAntTask.setFileName("Server.java");
            getAntTask.setRecurseFlag(false);
            getAntTask.execute();

            ServerUtility.copyFile(file1, file2);
            ServerUtility.copyFile(file3, file1);

            QVCSAntTask lockAntTask = initQVCSAntTask();
            lockAntTask.setLabel(null);
            lockAntTask.setOperation("lock");
            lockAntTask.setFileName("Server.java");
            lockAntTask.setRecurseFlag(false);
            lockAntTask.execute();

            QVCSAntTask checkInAntTask = initQVCSAntTask();
            checkInAntTask.setOperation("checkin");
            checkInAntTask.setCheckInComment("Test checkin");
            checkInAntTask.setFileName("Server.java");
            checkInAntTask.setRecurseFlag(false);
            checkInAntTask.execute();

            QVCSAntTask get2AntTask = initQVCSAntTask();
            get2AntTask.setLabel(COMPARE_TEST_LABEL);
            get2AntTask.setFileName("Server.java");
            get2AntTask.setOverWriteFlag(true);
            get2AntTask.setOperation("get");
            get2AntTask.setRecurseFlag(false);
            get2AntTask.execute();

            // Compare fetched file with file that was checked in to verify that it matches byte for byte
            assertTrue(TestHelper.compareFilesByteForByte(file1, file2));
        } catch (FileNotFoundException e) {
            fail("File not found exception:" + Utility.expandStackTraceToString(e));
        } catch (IOException e) {
            fail("IO exception:" + Utility.expandStackTraceToString(e));
        } catch (BuildException e) {
            fail("Caught unexpected exception:" + Utility.expandStackTraceToString(e));
        } catch (InterruptedException e) {
            fail("Caught interrupted exception.");
        }
    }

    /**
     * Test of execute method, of class QVCSAntTask.
     */
    public void testGetByLabel() {
        setUp();
        try {
            // Make sure the label is applied... in case the tests are not run in the order they appear here.
            QVCSAntTask labelAntTask = initQVCSAntTask();
            labelAntTask.setOperation("label");
            labelAntTask.setLabel("Test label");
            labelAntTask.execute();

            QVCSAntTask getAntTask = initQVCSAntTask();
            getAntTask.setOperation("get");
            getAntTask.setLabel("Test label");
            getAntTask.execute();
            File testDirectory = new File(TestHelper.buildTestDirectoryName(TEST_SUBDIRECTORY));
            File[] files = testDirectory.listFiles();
            assertTrue("Nothing was fetched!", files.length > 0);
        } catch (BuildException e) {
            fail("Caught unexpected exception.");
        } catch (InterruptedException e) {
            fail("Caught interrupted exception.");
        }
    }

    /**
     * Test of execute method, of class QVCSAntTask.
     */
    public void testReport() {
        setUp();
        try {
            QVCSAntTask reportAntTask = initQVCSAntTask();
            reportAntTask.setOperation("report");
            reportAntTask.execute();
        } catch (BuildException e) {
            fail("Caught unexpected exception.");
        } catch (InterruptedException e) {
            fail("Caught interrupted exception.");
        }
    }

    /**
     * Test of execute method, of class QVCSAntTask.
     */
    public void testReportWithCurrentStatus() {
        setUp();
        try {
            QVCSAntTask getAntTask = initQVCSAntTask();
            getAntTask.setOperation("get");
            getAntTask.execute();

            QVCSAntTask reportAntTask = initQVCSAntTask();
            reportAntTask.setReportFilesWithStatus("Current");
            reportAntTask.setOperation("report");
            reportAntTask.execute();
        } catch (BuildException e) {
            fail("Caught unexpected exception.");
        } catch (InterruptedException e) {
            fail("Caught interrupted exception.");
        }
    }

    public void testClientAPIGetMostRecentActivity() throws ClientAPIException {
        setUp();
        ClientAPIContext clientAPIContext = ClientAPIFactory.createClientAPIContext();
        clientAPIContext.setUserName(TestHelper.USER_NAME);
        clientAPIContext.setPassword(TestHelper.PASSWORD);
        clientAPIContext.setServerIPAddress("localhost");
        clientAPIContext.setPort(Integer.valueOf(29889));
        clientAPIContext.setProjectName(TestHelper.getTestProjectName());
        clientAPIContext.setViewName(QVCSConstants.QVCS_TRUNK_VIEW);
        clientAPIContext.setAppendedPath("");
        ClientAPI instance = ClientAPIFactory.createClientAPI(clientAPIContext);
        Date mostRecentActivity = instance.getMostRecentActivity();
        assertNotNull(mostRecentActivity);
    }

    public void testReportOnTranslucentBranch() {
        setUp();
        try {
            QVCSAntTask getAntTask = initQVCSAntTask();
            getAntTask.setViewName(TestHelper.getTranslucentBranchName());
            getAntTask.setOperation("get");
            getAntTask.execute();

            QVCSAntTask reportAntTask = initQVCSAntTask();
            reportAntTask.setViewName(TestHelper.getTranslucentBranchName());
            reportAntTask.setReportFilesWithStatus("Current");
            reportAntTask.setOperation("report");
            reportAntTask.execute();
        } catch (BuildException e) {
            fail("Caught unexpected exception.");
        } catch (InterruptedException e) {
            fail("Caught interrupted exception.");
        }
    }

    public void testMoveFileOnTrunk() {
        setUp();
        try {
            String rootDirectoryName = System.getProperty(TestHelper.USER_DIR)
                    + File.separator
                    + QVCSConstants.QVCS_PROJECTS_DIRECTORY
                    + File.separator
                    + TestHelper.getTestProjectName();
            File rootDirectory = new File(rootDirectoryName);
            System.out.println("======================================== Before move:");
            dumpArchiveFileDirectoryList(rootDirectory);

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
            System.out.println("======================================== After move:");
            dumpArchiveFileDirectoryList(rootDirectory);

            ClientAPIContext clientAPIContext = ClientAPIFactory.createClientAPIContext();
            clientAPIContext.setUserName(TestHelper.USER_NAME);
            clientAPIContext.setPassword(TestHelper.PASSWORD);
            clientAPIContext.setServerIPAddress("localhost");
            clientAPIContext.setPort(29889);
            clientAPIContext.setProjectName(TestHelper.getTestProjectName());
            clientAPIContext.setViewName(QVCSConstants.QVCS_TRUNK_VIEW);
            clientAPIContext.setAppendedPath(TestHelper.SUBPROJECT_DIR_NAME);
            clientAPIContext.setFileName("Server.java");
            ClientAPI instance = ClientAPIFactory.createClientAPI(clientAPIContext);
            List<RevisionInfo> result = instance.getRevisionInfoList();
            assertTrue(result.size() > 0);
            String revisionDescription = result.get(0).getRevisionDescription();
            assertTrue("unexpected revision description for tip revision.", revisionDescription.startsWith(QVCSConstants.QVCS_INTERNAL_REV_COMMENT_PREFIX));
        } catch (ClientAPIException e) {
            fail("Caught client api exception");
        } catch (BuildException e) {
            fail("Caught build exception.");
        } catch (InterruptedException e) {
            fail("Caught interrupted exception.");
        }
    }

    public void testMoveFileOnBranch() {
        setUp();
        try {
            String rootDirectoryName = System.getProperty(TestHelper.USER_DIR)
                    + File.separator
                    + QVCSConstants.QVCS_PROJECTS_DIRECTORY
                    + File.separator
                    + TestHelper.getTestProjectName();
            File rootDirectory = new File(rootDirectoryName);
            System.out.println("======================================== Before move:");
            dumpArchiveFileDirectoryList(rootDirectory);

            QVCSAntTask getAntTask = initQVCSAntTask();
            getAntTask.setOperation("get");
            getAntTask.setViewName(TestHelper.getTranslucentBranchName());
            getAntTask.execute();
            File testDirectory = new File(TestHelper.buildTestDirectoryName(TEST_SUBDIRECTORY));
            File[] files = testDirectory.listFiles();
            assertTrue("Nothing was fetched!", files.length > 0);

            QVCSAntTask moveAntTask = initQVCSAntTask();
            moveAntTask.setOperation(QVCSAntTask.OPERATION_MOVE);
            moveAntTask.setViewName(TestHelper.getTranslucentBranchName());
            moveAntTask.setFileName("Server.java");
            moveAntTask.setAppendedPath(TestHelper.SUBPROJECT_DIR_NAME);
            moveAntTask.setMoveToAppendedPath("");
            moveAntTask.execute();
            System.out.println("======================================== After move:");
            dumpArchiveFileDirectoryList(rootDirectory);

            ClientAPIContext clientAPIContext = ClientAPIFactory.createClientAPIContext();
            clientAPIContext.setUserName(TestHelper.USER_NAME);
            clientAPIContext.setPassword(TestHelper.PASSWORD);
            clientAPIContext.setServerIPAddress("localhost");
            clientAPIContext.setPort(29889);
            clientAPIContext.setProjectName(TestHelper.getTestProjectName());
            clientAPIContext.setViewName(TestHelper.getTranslucentBranchName());
            clientAPIContext.setAppendedPath("");
            clientAPIContext.setFileName("Server.java");
            ClientAPI instance = ClientAPIFactory.createClientAPI(clientAPIContext);
            List<RevisionInfo> result = instance.getRevisionInfoList();
            assertTrue(result.size() > 0);
            String revisionDescription = result.get(0).getRevisionDescription();
            assertTrue("unexpected revision description for tip revision.", revisionDescription.startsWith(QVCSConstants.QVCS_INTERNAL_REV_COMMENT_PREFIX));
        } catch (ClientAPIException e) {
            fail("Caught client api exception");
            LOGGER.log(Level.WARNING, Utility.expandStackTraceToString(e));
        } catch (BuildException e) {
            fail("Caught build exception.");
        } catch (InterruptedException e) {
            fail("Caught interrupted exception.");
        }
    }

    public void testRenameOnTrunk() {
        setUp();
        try {
            QVCSAntTask renameAntTask = initQVCSAntTask();
            renameAntTask.setFileName(TestHelper.BASE_DIR_SHORTWOFILENAME_A);
            renameAntTask.setRenameToFileName(TestHelper.BASE_DIR_SHORTWOFILENAME_A + ".Renamed");
            renameAntTask.setOperation("rename");

            renameAntTask.execute();
            ClientAPIContext clientAPIContext = ClientAPIFactory.createClientAPIContext();
            clientAPIContext.setUserName(TestHelper.USER_NAME);
            clientAPIContext.setPassword(TestHelper.PASSWORD);
            clientAPIContext.setServerIPAddress("localhost");
            clientAPIContext.setPort(29889);
            clientAPIContext.setProjectName(TestHelper.getTestProjectName());
            clientAPIContext.setViewName(QVCSConstants.QVCS_TRUNK_VIEW);
            clientAPIContext.setAppendedPath("");
            clientAPIContext.setFileName(TestHelper.BASE_DIR_SHORTWOFILENAME_A + ".Renamed");
            ClientAPI instance = ClientAPIFactory.createClientAPI(clientAPIContext);
            List<RevisionInfo> result = instance.getRevisionInfoList();
            assertTrue(result.size() > 0);
            String revisionDescription = result.get(0).getRevisionDescription();
            assertTrue("unexpected revision description for tip revision.", revisionDescription.startsWith(QVCSConstants.QVCS_INTERNAL_REV_COMMENT_PREFIX));
            assertTrue("checkin comment missing expected text.", revisionDescription.contains(QVCSConstants.QVCS_INTERNAL_FILE_RENAMED_FROM));
        } catch (ClientAPIException e) {
            fail("Caught client api exception");
            LOGGER.log(Level.WARNING, Utility.expandStackTraceToString(e));
        } catch (BuildException e) {
            fail("Caught unexpected exception.");
        } catch (InterruptedException e) {
            fail("Caught interrupted exception.");
        }
    }

    public void testRenameOnTranslucentBranch() {
        setUp();
        try {
            QVCSAntTask renameAntTask = initQVCSAntTask();
            renameAntTask.setViewName(TestHelper.getTranslucentBranchName());
            renameAntTask.setFileName(TestHelper.BASE_DIR_SHORTWOFILENAME_B);
            renameAntTask.setRenameToFileName(TestHelper.BASE_DIR_SHORTWOFILENAME_B + ".Renamed");
            renameAntTask.setOperation("rename");

            renameAntTask.execute();
            ClientAPIContext clientAPIContext = ClientAPIFactory.createClientAPIContext();
            clientAPIContext.setUserName(TestHelper.USER_NAME);
            clientAPIContext.setPassword(TestHelper.PASSWORD);
            clientAPIContext.setServerIPAddress("localhost");
            clientAPIContext.setPort(29889);
            clientAPIContext.setProjectName(TestHelper.getTestProjectName());
            clientAPIContext.setViewName(TestHelper.getTranslucentBranchName());
            clientAPIContext.setAppendedPath("");
            clientAPIContext.setFileName(TestHelper.BASE_DIR_SHORTWOFILENAME_B + ".Renamed");
            ClientAPI instance = ClientAPIFactory.createClientAPI(clientAPIContext);
            List<RevisionInfo> result = instance.getRevisionInfoList();
            assertTrue(result.size() > 0);
            String revisionDescription = result.get(0).getRevisionDescription();
            assertTrue("unexpected revision description for tip revision.", revisionDescription.startsWith(QVCSConstants.QVCS_INTERNAL_REV_COMMENT_PREFIX));
            assertTrue("checkin comment missing expected text.", revisionDescription.contains(QVCSConstants.QVCS_INTERNAL_FILE_RENAMED_FROM));
        } catch (ClientAPIException e) {
            fail("Caught client api exception");
            LOGGER.log(Level.WARNING, Utility.expandStackTraceToString(e));
        } catch (BuildException e) {
            fail("Caught unexpected exception.");
        } catch (InterruptedException e) {
            fail("Caught interrupted exception.");
        }
    }

    public void testDeleteOnTrunk() {
        setUp();
        try {
            QVCSAntTask deleteAntTask = initQVCSAntTask();
            String fileToDelete = TestHelper.BASE_DIR_SHORTWOFILENAME_A + ".Renamed";
            deleteAntTask.setFileName(fileToDelete);
            deleteAntTask.setOperation("delete");

            deleteAntTask.execute();
            ClientAPIContext clientAPIContext = ClientAPIFactory.createClientAPIContext();
            clientAPIContext.setUserName(TestHelper.USER_NAME);
            clientAPIContext.setPassword(TestHelper.PASSWORD);
            clientAPIContext.setServerIPAddress("localhost");
            clientAPIContext.setPort(29889);
            clientAPIContext.setProjectName(TestHelper.getTestProjectName());
            clientAPIContext.setViewName(QVCSConstants.QVCS_TRUNK_VIEW);
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
            fail("Caught client api exception");
            LOGGER.log(Level.WARNING, Utility.expandStackTraceToString(e));
        } catch (BuildException e) {
            fail("Caught unexpected exception.");
        } catch (InterruptedException e) {
            fail("Caught interrupted exception.");
        }
    }

    public void testDeleteOnTranslucentBranch() {
        setUp();
        try {
            QVCSAntTask deleteAntTask = initQVCSAntTask();
            deleteAntTask.setViewName(TestHelper.getTranslucentBranchName());
            String fileToDelete = TestHelper.BASE_DIR_SHORTWOFILENAME_B + ".Renamed";
            deleteAntTask.setFileName(fileToDelete);
            deleteAntTask.setOperation("delete");

            deleteAntTask.execute();
            ClientAPIContext clientAPIContext = ClientAPIFactory.createClientAPIContext();
            clientAPIContext.setUserName(TestHelper.USER_NAME);
            clientAPIContext.setPassword(TestHelper.PASSWORD);
            clientAPIContext.setServerIPAddress("localhost");
            clientAPIContext.setPort(29889);
            clientAPIContext.setProjectName(TestHelper.getTestProjectName());
            clientAPIContext.setViewName(TestHelper.getTranslucentBranchName());
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
            fail("Caught client api exception");
            LOGGER.log(Level.WARNING, Utility.expandStackTraceToString(e));
        } catch (BuildException e) {
            fail("Caught unexpected exception.");
        } catch (InterruptedException e) {
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

    private void dumpArchiveFileDirectoryList(File rootDirectory) {
        File[] fileList = rootDirectory.listFiles();
        for (File containedFile : fileList) {
            try {
                System.out.println(containedFile.getCanonicalPath());
                if (containedFile.isDirectory()) {
                    dumpArchiveFileDirectoryList(containedFile);
                }
            } catch (IOException e) {
                Logger.getLogger(QVCSAntTaskServerTest.class.getName()).log(Level.SEVERE, null, e);
            }
        }
    }
}
