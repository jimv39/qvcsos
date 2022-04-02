/*   Copyright 2004-2019 Jim Voris
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
import com.qumasoft.server.ServerUtility;
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
 * Use the ant task to test that we correctly fixed our use of apache compare.
 *
 * @author Jim Voris
 */
public class ApacheCompareServerTest {
    private static DatabaseManager databaseManager;

    private static final Logger LOGGER = LoggerFactory.getLogger(ApacheCompareServerTest.class);
    private static final String TEST_SUBDIRECTORY = "AntQVCSTestFiles";
    private static Object serverSyncObject = null;

    /**
     * Default constructor.
     */
    public ApacheCompareServerTest() {
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
        CommonTestHelper.getCommonTestHelper().acquireSyncObject();
        CommonTestHelper.getCommonTestHelper().resetTestDatabaseViaPsqlScript();
        CommonTestHelper.getCommonTestHelper().resetQvcsosTestDatabaseViaPsqlScript();
        databaseManager = DatabaseManager.getInstance();
        databaseManager.initializeDatabase();
        TestHelper.initializeApacheCompareTestArchiveFiles();
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
//        TestHelper.deleteBranchStore();
//        TestHelper.removeArchiveFiles();
        CommonTestHelper.getCommonTestHelper().releaseSyncObject();
        LOGGER.info("Ending test class");
    }

    /**
     * Set up the things common to all the tests.
     */
    @org.junit.Before
    public void setUp() {
        LOGGER.info("Starting test");
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
        qvcsAntTask.setRecurseFlag(false);
        qvcsAntTask.init();
        return qvcsAntTask;
    }

    /**
     * Test of execute method, of class QVCSAntTask.
     */
    @Test
    public void testGet() {
        try {
            QVCSAntTask getAntTask = initQVCSAntTask();
            getAntTask.setOperation("get");
            getAntTask.execute();
            File testDirectory = new File(TestHelper.buildTestDirectoryName(TEST_SUBDIRECTORY));
            File[] files = testDirectory.listFiles();
            assertTrue("Nothing was fetched!", files.length > 0);
        } catch (BuildException e) {
            fail("Caught build exception.");
        } catch (InterruptedException e) {
            fail("Caught interrupted exception.");
        }
    }

    /**
     * Test of execute method, of class QVCSAntTask.
     */
    @Test
    public void testCompareTest1a() {
        helperMethoda("CompareTest1a.txt", "CompareTest1b.txt");
    }

    /**
     * Test of execute method, of class QVCSAntTask.
     */
    @Test
    public void testCompareTest1b() {
        helperMethodb("CompareTest1a.txt", "CompareTest1b.txt");
    }

    /**
     * Test of execute method, of class QVCSAntTask.
     */
    @Test
    public void testCompareTest2a() {
        helperMethoda("CompareTest2a.txt", "CompareTest2b.txt");
    }

    /**
     * Test of execute method, of class QVCSAntTask.
     */
    @Test
    public void testCompareTest2b() {
        helperMethodb("CompareTest2a.txt", "CompareTest2b.txt");
    }

    /**
     * Test of execute method, of class QVCSAntTask.
     */
    @Test
    public void testCompareTest3a() {
        helperMethoda("CompareTest3a.txt", "CompareTest3b.txt");
    }

    /**
     * Test of execute method, of class QVCSAntTask.
     */
    @Test
    public void testCompareTest3b() {
        helperMethodb("CompareTest3a.txt", "CompareTest3b.txt");
    }

    /**
     * Test of execute method, of class QVCSAntTask.
     */
    @Test
    public void testCompareTest4a() {
        helperMethoda("CompareTest4a.txt", "CompareTest4b.txt");
    }

    /**
     * Test of execute method, of class QVCSAntTask.
     */
    @Test
    public void testCompareTest4b() {
        helperMethodb("CompareTest4a.txt", "CompareTest4b.txt");
    }

    /**
     * Test of execute method, of class QVCSAntTask.
     */
    @Test
    public void testCompareTest5a() {
        helperMethoda("CompareTest5a.txt", "CompareTest5b.txt");
    }

    /**
     * Test of execute method, of class QVCSAntTask.
     */
    @Test
    public void testCompareTest5b() {
        helperMethodb("CompareTest5a.txt", "CompareTest5b.txt");
    }

    /**
     * Test of execute method, of class QVCSAntTask.
     */
    @Test
    public void testCompareTest6a() {
        helperMethoda("CompareTest6a.txt", "CompareTest6b.txt");
    }

    /**
     * Test of execute method, of class QVCSAntTask.
     */
    @Test
    public void testCompareTest6b() {
        helperMethodb("CompareTest6a.txt", "CompareTest6b.txt");
    }

    /**
     * Test of execute method, of class QVCSAntTask.
     */
    @Test
    public void testCompareTest7a() {
        helperMethoda("CompareTest7a.txt", "CompareTest7b.txt");
    }

    /**
     * Test of execute method, of class QVCSAntTask.
     */
    @Test
    public void testCompareTest7b() {
        helperMethodb("CompareTest7a.txt", "CompareTest7b.txt");
    }

    /**
     * Test of execute method, of class QVCSAntTask.
     */
    @Test
    public void testCompareTest8a() {
        helperMethoda("CompareTest8a.txt", "CompareTest8b.txt");
    }

    /**
     * Test of execute method, of class QVCSAntTask.
     */
    @Test
    public void testCompareTest8b() {
        helperMethodb("CompareTest8a.txt", "CompareTest8b.txt");
    }

    /**
     * Test of execute method, of class QVCSAntTask.
     */
    @Test
    public void testCompareTest9a() {
        helperMethoda("CompareTest9a.txt", "CompareTest9b.txt");
    }

    /**
     * Test of execute method, of class QVCSAntTask.
     */
    @Test
    public void testCompareTest9b() {
        helperMethodb("CompareTest9a.txt", "CompareTest9b.txt");
    }

    /**
     * Test of execute method, of class QVCSAntTask.
     */
    @Test
    public void testCompareTest10a() {
        helperMethoda("CompareTest10a.txt", "CompareTest10b.txt");
    }

    /**
     * Test of execute method, of class QVCSAntTask.
     */
    @Test
    public void testCompareTest10b() {
        helperMethodb("CompareTest10a.txt", "CompareTest10b.txt");
    }

    /**
     * Test of execute method, of class QVCSAntTask.
     */
    @Test
    public void testCompareTest12a() {
        helperMethoda("CompareTest12a.txt", "CompareTest12b.txt");
    }

    /**
     * Test of execute method, of class QVCSAntTask.
     */
    @Test
    public void testCompareTest12b() {
        helperMethodb("CompareTest12a.txt", "CompareTest12b.txt");
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

    private void helperMethoda(String fileA, String fileB) {
        try {
            String userDir = System.getProperty("user.dir");

            File file0 = new File(userDir + File.separator + fileA);
            File file1 = new File(userDir + File.separator + fileB);
            File file2 = new File(TestHelper.buildTestDirectoryName(TEST_SUBDIRECTORY) + File.separator + fileA);

            QVCSAntTask getAntTask = initQVCSAntTask();
            getAntTask.setOperation("get");
            getAntTask.setOverWriteFlag(true);
            getAntTask.setFileName(fileA);
            getAntTask.execute();

            // Compare fetched file with file that was checked in to verify that it matches byte for byte
            assertTrue(TestHelper.compareFilesByteForByte(file0, file2));

            // Copy CompareTest1b.txt so we can check it in as CompareTest1a.txt (this will test that the computing of the delta is correct).
            ServerUtility.copyFile(file1, file2);

            QVCSAntTask checkInAntTask = initQVCSAntTask();
            checkInAntTask.setOperation("checkin");
            checkInAntTask.setCheckInComment("Test checkin");
            checkInAntTask.setFileName(fileA);
            checkInAntTask.execute();

            QVCSAntTask get2AntTask = initQVCSAntTask();
            get2AntTask.setOperation("get");
            get2AntTask.setOverWriteFlag(true);
            get2AntTask.setFileName(fileA);
            get2AntTask.execute();

            // Compare fetched file with file that was checked in to verify that it matches byte for byte
            assertTrue(TestHelper.compareFilesByteForByte(file0, file2));
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

    private void helperMethodb(String fileA, String fileB) {
        try {
            String userDir = System.getProperty("user.dir");

            File file0 = new File(userDir + File.separator + fileA);
            File file1 = new File(userDir + File.separator + fileB);
            File file2 = new File(TestHelper.buildTestDirectoryName(TEST_SUBDIRECTORY) + File.separator + fileB);

////            QVCSAntTask labelAntTask = initQVCSAntTask();
//            labelAntTask.setFileName(fileB);

            // Compare fetched file with file that was checked in to verify that it matches byte for byte
            assertTrue(TestHelper.compareFilesByteForByte(file1, file2));

            // Copy CompareTest12a.txt so we can check it in as CompareTest12b.txt (this will test that the computing of the delta is correct).
            ServerUtility.copyFile(file0, file2);

            QVCSAntTask checkInAntTask = initQVCSAntTask();
            checkInAntTask.setOperation("checkin");
            checkInAntTask.setCheckInComment("Test checkin");
            checkInAntTask.setFileName(fileB);
            checkInAntTask.execute();

            QVCSAntTask get2AntTask = initQVCSAntTask();
            get2AntTask.setOperation("get");
            get2AntTask.setOverWriteFlag(true);
            get2AntTask.setFileName(fileB);
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

}
