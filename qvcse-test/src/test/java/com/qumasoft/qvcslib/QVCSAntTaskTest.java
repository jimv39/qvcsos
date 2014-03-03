//   Copyright 2004-2014 Jim Voris
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.
//
// $FilePath$
//     $Date: Friday, April 06, 2012 10:11:01 PM $
//   $Header: QVCSAntTaskTest.java Revision:1.7 Friday, April 06, 2012 10:11:01 PM JimVoris $
// $Copyright © 2011-2012 Define this string in the qvcs.keywords.properties property file $

package com.qumasoft.qvcslib;

import com.qumasoft.TestHelper;
import com.qumasoft.clientapi.ClientAPI;
import com.qumasoft.clientapi.ClientAPIContext;
import com.qumasoft.clientapi.ClientAPIException;
import com.qumasoft.clientapi.ClientAPIFactory;
import com.qumasoft.server.ServerUtility;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test the QVCS customer ant task.
 *
 * @author $Author: JimVoris $
 */
public class QVCSAntTaskTest {

    private QVCSAntTask qvcsAntTask = null;
//    private static final String TEST_DIRECTORY = "/temp/QVCSTestFiles";

    /**
     * Default constructor.
     */
    public QVCSAntTaskTest() {
    }

    /**
     * Execute this stuff once when the class is loaded.
     *
     * @throws Exception if we have a problem with initialization.
     */
    @BeforeClass
    public static void setUpClass() throws Exception {
        TestHelper.stopServerImmediately();
        TestHelper.removeArchiveFiles();
        TestHelper.deleteViewStore();
        TestHelper.initializeArchiveFiles();
        TestHelper.startServer();
    }

    /**
     * Execute this just once after we complete all the tests defined in this class.
     *
     * @throws Exception if we have a problem tearing things down.
     */
    @AfterClass
    public static void tearDownClass() throws Exception {
        TestHelper.stopServer();
        TestHelper.deleteViewStore();
        TestHelper.removeArchiveFiles();
    }

    /**
     * Set up the things common to all the tests.
     */
    @Before
    public void setUp() {
        emptyTestDirectory();
        qvcsAntTask = new QVCSAntTask();
        qvcsAntTask.setUserName(TestHelper.USER_NAME);
        qvcsAntTask.setPassword(TestHelper.PASSWORD);
        qvcsAntTask.setUserDirectory(System.getProperty("user.dir"));
        qvcsAntTask.setProjectName(TestHelper.getTestProjectName());
        qvcsAntTask.setServerName(TestHelper.SERVER_NAME);
        qvcsAntTask.setAppendedPath("");
        qvcsAntTask.setWorkfileLocation(getTestDirectory());
        qvcsAntTask.setProject(new Project());
        qvcsAntTask.init();
    }

    /**
     * We tear this down after each test.
     */
    @After
    public void tearDown() {
        emptyTestDirectory();
    }

    /**
     * Test of execute method, of class QVCSAntTask.
     */
    @Test
    public void testGet() {
        try {
            qvcsAntTask.setOperation("get");
            qvcsAntTask.execute();
            File testDirectory = new File(getTestDirectory());
            File[] files = testDirectory.listFiles();
            assertTrue("Nothing was fetched!", files.length > 0);
        } catch (BuildException e) {
            fail("Caught unexpected exception.");
        }
    }

    /**
     * Test of execute method, of class QVCSAntTask.
     */
    @Test
    public void testGetByFileExtension() {
        try {
            qvcsAntTask.setOperation("get");
            qvcsAntTask.setFileExtension("java");
            qvcsAntTask.execute();
            File testDirectory = new File(getTestDirectory());
            File[] files = testDirectory.listFiles();
            assertTrue("Nothing was fetched!", files.length > 0);
        } catch (BuildException e) {
            fail("Caught unexpected exception.");
        }
    }

    /**
     * Test of execute method, of class QVCSAntTask.
     */
    @Test
    public void testLabel() {
        try {
            qvcsAntTask.setOperation("label");
            qvcsAntTask.setLabel("Test label");
            qvcsAntTask.execute();
        } catch (BuildException e) {
            fail("Caught unexpected exception.");
        }
    }

    /**
     * Test of execute method, of class QVCSAntTask.
     */
    @Test
    public void testLockAndUnlock() {
        try {
            qvcsAntTask.setOperation("lock");
            qvcsAntTask.execute();

            qvcsAntTask.setOperation("unlock");
            qvcsAntTask.execute();
        } catch (BuildException e) {
            fail("Caught unexpected exception.");
        }
    }

    /**
     * Test of execute method, of class QVCSAntTask.
     */
    @Test
    public void testLockGetCheckInAndGet() {
        final String COMPARE_TEST_LABEL = "Compare Test Label";
        try {
            File file1 = new File(getTestDirectory() + File.separator + "Server.java");
            File file2 = new File(getTestDirectory() + File.separator + "OriginalServer.java");
            String userDir = System.getProperty("user.dir");
            File[] filesHere = new File(userDir).listFiles();
            File file3 = new File(userDir + File.separator + "Serverb.java");

            qvcsAntTask.setRecurseFlag(false);
            qvcsAntTask.setFileName("Server.java");

            qvcsAntTask.setOperation("report");
            qvcsAntTask.execute();

            qvcsAntTask.setOperation("label");
            qvcsAntTask.setLabel(COMPARE_TEST_LABEL);
            qvcsAntTask.execute();

            qvcsAntTask.setOverWriteFlag(true);
            qvcsAntTask.setOperation("get");
            qvcsAntTask.execute();
            ServerUtility.copyFile(file1, file2);
            ServerUtility.copyFile(file3, file1);

            qvcsAntTask.setLabel(null);
            qvcsAntTask.setOperation("lock");
            qvcsAntTask.execute();

            qvcsAntTask.setOperation("checkin");
            qvcsAntTask.setCheckInComment("Test checkin");
            qvcsAntTask.execute();

            qvcsAntTask.setOperation("report");
            qvcsAntTask.execute();

            qvcsAntTask.setLabel(COMPARE_TEST_LABEL);
            qvcsAntTask.setOperation("get");
            qvcsAntTask.execute();

            // Compare fetched file with file that was checked in to verify that it matches byte for byte
            assertTrue(TestHelper.compareFilesByteForByte(file1, file2));
        } catch (FileNotFoundException e) {
            fail("File not found exception:" + Utility.expandStackTraceToString(e));
        } catch (IOException e) {
            fail("IO exception:" + Utility.expandStackTraceToString(e));
        } catch (BuildException e) {
            fail("Caught unexpected exception:" + Utility.expandStackTraceToString(e));
        }
    }

    /**
     * Test of execute method, of class QVCSAntTask.
     */
    @Test
    public void testGetByLabel() {
        try {
            // Make sure the label is applied... in case the tests are not run in the order they appear here.
            qvcsAntTask.setOperation("label");
            qvcsAntTask.setLabel("Test label");
            qvcsAntTask.execute();

            qvcsAntTask.setOperation("get");
            qvcsAntTask.setLabel("Test label");
            qvcsAntTask.execute();
            File testDirectory = new File(getTestDirectory());
            File[] files = testDirectory.listFiles();
            assertTrue("Nothing was fetched!", files.length > 0);
        } catch (BuildException e) {
            fail("Caught unexpected exception.");
        }
    }

    /**
     * Test of execute method, of class QVCSAntTask.
     */
    @Test
    public void testReport() {
        try {
            qvcsAntTask.setOperation("report");
            qvcsAntTask.execute();
        } catch (BuildException e) {
            fail("Caught unexpected exception.");
        }
    }

    /**
     * Test of execute method, of class QVCSAntTask.
     */
    @Test
    public void testReportWithCurrentStatus() {
        try {
            qvcsAntTask.setOperation("get");
            qvcsAntTask.execute();
            qvcsAntTask.setReportFilesWithStatus("Current");
            qvcsAntTask.setOperation("report");
            qvcsAntTask.execute();
        } catch (BuildException e) {
            fail("Caught unexpected exception.");
        }
    }

    @Test
    public void testClientAPIGetMostRecentActivity() throws ClientAPIException {
        ClientAPIContext clientAPIContext = ClientAPIFactory.createClientAPIContext();
        ClientAPI instance = ClientAPIFactory.createClientAPI();
        clientAPIContext.setUserName(TestHelper.USER_NAME);
        clientAPIContext.setPassword(TestHelper.PASSWORD);
        clientAPIContext.setServerIPAddress("localhost");
        clientAPIContext.setPort(Integer.valueOf(29889));
        clientAPIContext.setProjectName(TestHelper.getTestProjectName());
        clientAPIContext.setViewName(QVCSConstants.QVCS_TRUNK_VIEW);
        clientAPIContext.setAppendedPath("");
        Date mostRecentActivity = instance.getMostRecentActivity(clientAPIContext);
        assertNotNull(mostRecentActivity);
    }

    /**
     * Clean out the test directory. This is not fully recursive, since we don't want a run-away delete to wipe out all the contents of the disk by mistake.
     */
    private void emptyTestDirectory() {
        // Delete the files in the /temp/QVCSTestFiles directory.
        File tempDirectory = new File(getTestDirectory());
        File[] files = tempDirectory.listFiles();
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

    private String getTestDirectory() {
        if (Utility.isMacintosh()) {
            return "/temp/QVCSTestFiles";
        } else {
            return "C:/temp/QVCSTestFiles";
        }
    }
}
