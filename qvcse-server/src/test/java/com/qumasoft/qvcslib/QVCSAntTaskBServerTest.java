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
import com.qumasoft.server.ServerUtility;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test the QVCS custom ant task.
 *
 * @author Jim Voris
 */
public class QVCSAntTaskBServerTest {

    private static final Logger logger = Logger.getLogger("com.qumasoft.qvcslib");
    private static Object serverSyncObject = null;

    /**
     * Default constructor.
     */
    public QVCSAntTaskBServerTest() {
    }

    /**
     * Execute this stuff once when the class is loaded.
     *
     * @throws Exception if we have a problem with initialization.
     */
    @BeforeClass
    public static void setUpClass() throws Exception {
        logger.log(Level.INFO, "Starting test class");
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
        logger.log(Level.INFO, "Ending test class");
    }

    /**
     * Set up the things common to all the tests.
     */
    @Before
    public void setUp() {
        logger.log(Level.INFO, "Starting test");
        emptyTestDirectory();
    }

    /**
     * We tear this down after each test.
     */
    @After
    public void tearDown() {
        emptyTestDirectory();
        logger.log(Level.INFO, "Ending test");
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
        qvcsAntTask.setWorkfileLocation(getTestDirectory());
        qvcsAntTask.setProject(new Project());
        qvcsAntTask.init();
        return qvcsAntTask;
    }

    /**
     * Test of execute method, of class QVCSAntTask.
     */
    @Test
    public void testCheckOutAndCheckIn() {
        final String COMPARE_TEST_LABEL = "Compare Test Label";
        try {
            File file1 = new File(getTestDirectory() + File.separator + "Server.java");
            File file2 = new File(getTestDirectory() + File.separator + "OriginalServer.java");
            String userDir = System.getProperty("user.dir");
            File file3 = new File(userDir + File.separator + "Serverb.java");

            QVCSAntTask labelAntTask = initQVCSAntTask();
            labelAntTask.setOperation("label");
            labelAntTask.setLabel(COMPARE_TEST_LABEL);
            labelAntTask.setFileName("Server.java");
            labelAntTask.setRecurseFlag(false);
            labelAntTask.execute();

            QVCSAntTask checkoutAntTask = initQVCSAntTask();
            checkoutAntTask.setOverWriteFlag(true);
            checkoutAntTask.setLabel(COMPARE_TEST_LABEL);
            checkoutAntTask.setOperation("checkout");
            checkoutAntTask.setFileName("Server.java");
            checkoutAntTask.setRecurseFlag(false);
            checkoutAntTask.execute();

            ServerUtility.copyFile(file1, file2);
            ServerUtility.copyFile(file3, file1);

            QVCSAntTask checkInAntTask = initQVCSAntTask();
            checkInAntTask.setOperation("checkin");
            checkInAntTask.setCheckInComment("Test checkin");
            checkInAntTask.setFileName("Server.java");
            checkInAntTask.setRecurseFlag(false);
            checkInAntTask.execute();

            QVCSAntTask getAntTask = initQVCSAntTask();
            getAntTask.setLabel(COMPARE_TEST_LABEL);
            getAntTask.setFileName("Server.java");
            getAntTask.setOverWriteFlag(true);
            getAntTask.setOperation("get");
            getAntTask.setRecurseFlag(false);
            getAntTask.execute();

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
     * Clean out the test directory. This is not fully recursive, since we don't want a run-away delete to wipe out all the contents of the disk by mistake.
     */
    private void emptyTestDirectory() {
        // Delete the files in the /tmp/QVCSTestFiles directory.
        File tempDirectory = new File(getTestDirectory());
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

    private String getTestDirectory() {
        String testDirectoryName = "C:/tmp/QVCSTestFiles";
        if (Utility.isMacintosh() || Utility.isLinux()) {
            testDirectoryName = "/tmp/QVCSTestFiles";
        }
        return testDirectoryName;
    }
}
