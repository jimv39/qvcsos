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
//     $Date: Wednesday, March 21, 2012 10:31:05 PM $
//   $Header: LogFileTest.java Revision:1.3 Wednesday, March 21, 2012 10:31:05 PM JimVoris $
// $Copyright © 2011-2012 Define this string in the qvcs.keywords.properties property file $

package com.qumasoft.qvcslib;

import com.qumasoft.TestHelper;
import com.qumasoft.server.AbstractLogFileOperation;
import com.qumasoft.server.FileIDManager;
import com.qumasoft.server.LogFile;
import java.io.File;
import java.util.Date;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author $Author: JimVoris $
 */
public class LogFileTest {

    /**
     * Execute once before running the tests.
     *
     * @throws Exception if there is something wrong.
     */
    @BeforeClass
    public static void setUpClass() throws Exception {
        try {
            System.out.println("Test setup called for LogFileTest");

            String archive1FileName = System.getProperty("user.dir") + File.separator + "TestCheckOutArchive.kbwb";
            File oldArchive1 = new File(archive1FileName);
            if (oldArchive1.exists()) {
                oldArchive1.delete();
            }

            String archive2FileName = System.getProperty("user.dir") + File.separator + "TestLockArchive.kbwb";
            File oldArchive2 = new File(archive2FileName);
            if (oldArchive2.exists()) {
                oldArchive2.delete();
            }

            String archive3FileName = System.getProperty("user.dir") + File.separator + "TestCreateArchive.kbwb";
            File oldArchive3 = new File(archive3FileName);
            if (oldArchive3.exists()) {
                oldArchive3.delete();
            }

            String archive4FileName = System.getProperty("user.dir") + File.separator + "BranchingTestFile.uyu";
            File oldArchive4 = new File(archive4FileName);
            if (oldArchive4.exists()) {
                oldArchive4.delete();
            }

            // Create copies of the starting state archive files.
            File orgArchive1 = new File(archive1FileName + ".org");
            boolean result = AbstractLogFileOperation.copyFile(orgArchive1, oldArchive1);

            File orgArchive2 = new File(archive2FileName + ".org");
            result = AbstractLogFileOperation.copyFile(orgArchive2, oldArchive2);

            File orgArchive4 = new File(archive4FileName + ".org");
            result = AbstractLogFileOperation.copyFile(orgArchive4, oldArchive4);

            // Delete the tip revision file that we create.
            String tipWorkfileFileName = System.getProperty("user.dir") + File.separator + "TestLockArchive_TipRevision.java";
            File tipWorkfileFile = new File(tipWorkfileFileName);
            if (tipWorkfileFile.exists()) {
                tipWorkfileFile.delete();
            }

            // Delete the get revision file that we create.
            String revisionWorkfileFileName = System.getProperty("user.dir") + File.separator + "TestGetRevisionArchive.java";
            File revisionWorkfileFile = new File(revisionWorkfileFileName);
            if (revisionWorkfileFile.exists()) {
                revisionWorkfileFile.delete();
            }

            FileIDManager.getInstance().initialize();
        } catch (Exception e) {
            System.out.println(Utility.expandStackTraceToString(e));
        }
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        try {
            String archive1FileName = System.getProperty("user.dir") + File.separator + "TestCheckOutArchive.kbwb";
            File oldArchive1 = new File(archive1FileName);
            if (oldArchive1.exists()) {
                oldArchive1.delete();
            }

            String archive2FileName = System.getProperty("user.dir") + File.separator + "TestLockArchive.kbwb";
            File oldArchive2 = new File(archive2FileName);
            if (oldArchive2.exists()) {
                oldArchive2.delete();
            }

            String archive3FileName = System.getProperty("user.dir") + File.separator + "TestCreateArchive.kbwb";
            File oldArchive3 = new File(archive3FileName);
            if (oldArchive3.exists()) {
                oldArchive3.delete();
            }

            String archive4FileName = System.getProperty("user.dir") + File.separator + "BranchingTestFile.uyu";
            File oldArchive4 = new File(archive4FileName);
            if (oldArchive4.exists()) {
                oldArchive4.delete();
            }

            String workFileName = System.getProperty("user.dir") + File.separator + "TestCheckOutArchive.java";
            File workFile = new File(workFileName);
            if (workFile.exists()) {
                workFile.delete();
            }

            String branchingWorkfileFileName = System.getProperty("user.dir") + File.separator + "BranchingTestFile.txt";
            File branchingWorkFile = new File(branchingWorkfileFileName);
            if (branchingWorkFile.exists()) {
                branchingWorkFile.delete();
            }
        } catch (Exception e) {
            System.out.println(Utility.expandStackTraceToString(e));
        }
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of convertArchiveNameToShortWorkfileName method, of class com.qumasoft.qvcslib.LogFile.
     */
    @Test
    public void testConvertArchiveNameToShortWorkfileName() {
        System.out.println("testConvertArchiveNameToShortWorkfileName");

        String testResult1 = Utility.convertArchiveNameToShortWorkfileName("test.bcd");
        if (!testResult1.equals("test.abc")) {
            fail("testConvertArchiveNameToShortWorkfileName test 1 failed");
        }

        String testResult2 = Utility.convertArchiveNameToShortWorkfileName("test.yza");
        if (!testResult2.equals("test.xyz")) {
            fail("testConvertArchiveNameToShortWorkfileName test 2 failed");
        }
    }

    /**
     * Test of convertWorkfileNameToShortArchiveName method, of class com.qumasoft.qvcslib.LogFile.
     */
    @Test
    public void testConvertWorkfileNameToShortArchiveName() {
        System.out.println("testConvertWorkfileNameToShortArchiveName");

        String testResult1 = Utility.convertWorkfileNameToShortArchiveName("test.abc");
        if (!testResult1.equals("test.bcd")) {
            fail("testConvertWorkfileNameToShortArchiveName test 1 failed");
        }

        String testResult2 = Utility.convertWorkfileNameToShortArchiveName("test.xyz");
        if (!testResult2.equals("test.yza")) {
            fail("testConvertWorkfileNameToShortArchiveName test 2 failed");
        }
    }

    /**
     * Test of getRevision method, of class com.qumasoft.qvcslib.LogFile.
     */
    @Test
    public void testGetRevision() {
        System.out.println("testGetRevision");

        // Create the archive file object.
        LogFile testArchive = new LogFile(System.getProperty("user.dir") + File.separator + "TestLockArchive.kbwb");

        try {
            // Create the command line object
            LogFileOperationGetRevisionCommandArgs commandArgs = new LogFileOperationGetRevisionCommandArgs();

            // Set the elements of the command args
            commandArgs.setUserName("JimVoris");
            commandArgs.setRevisionString("1.3");
            commandArgs.setOutputFileName(System.getProperty("user.dir") + File.separator + "TestGetRevisionArchive.java");
            commandArgs.setFullWorkfileName(System.getProperty("user.dir") + File.separator + "TestGetRevisionArchive.java");
            commandArgs.setShortWorkfileName("TestGetRevisionArchive.java");

            if (!testArchive.getRevision(commandArgs, System.getProperty("user.dir") + File.separator + "TestGetRevisionArchive.java")) {
                fail("Failed get revision test");
            } else {
                System.out.println("testGetRevision passed.");

                // Delete the get revision file that we create.
                String revisionWorkfileFileName = System.getProperty("user.dir") + File.separator + "TestGetRevisionArchive.java";
                File revisionWorkfileFile = new File(revisionWorkfileFileName);
                if (revisionWorkfileFile.exists()) {
                    revisionWorkfileFile.delete();
                }
            }
            verifyArchiveFile(testArchive);
        } catch (QVCSException e) {
            fail("Caught QVCSException: " + e.getLocalizedMessage() + " in testGetRevision()");
        }
    }

    /**
     * Test of readInformation method, of class com.qumasoft.qvcslib.LogFile.
     */
    @Test
    public void testReadInformation() {
        System.out.println("testReadInformation");

        // Create the archive file object.
        LogFile testArchive = new LogFile(System.getProperty("user.dir") + File.separator + "TestLockArchive.kbwb");

        try {
            boolean flag = testArchive.readInformation();
            if (!flag) {
                fail("testReadInformation failed!");
            }
            verifyArchiveFile(testArchive);
        } catch (Exception e) {
            fail("testReadInformation failed with exception: " + e.getClass().toString() + " : " + e.getLocalizedMessage());
        }
    }

    /**
     * Test of getShortWorkfileName method, of class com.qumasoft.qvcslib.LogFile.
     */
    @Test
    public void testGetShortWorkfileName() {
        System.out.println("testGetShortWorkfileName");

        // Create the archive file object.
        LogFile testArchive = new LogFile(System.getProperty("user.dir") + File.separator + "TestLockArchive.kbwb");

        try {
            String shortWorkfileName = testArchive.getShortWorkfileName();
            System.out.println("Short workfile name: " + shortWorkfileName);
        } catch (Exception e) {
            fail("testGetShortWorkfileName failed with exception: " + e.getClass().toString() + " : " + e.getLocalizedMessage());
        }
    }

    /**
     * Test of getLockedByUser method, of class com.qumasoft.qvcslib.LogFile.
     */
    @Test
    public void testGetLockedByUser() {
        System.out.println("testGetLockedByUser");

        // Create the archive file object.
        LogFile testArchive = new LogFile(System.getProperty("user.dir") + File.separator + "TestLockArchive.kbwb");

        try {
            String lockedByUser = testArchive.getLockedByUser();
            System.out.println("Locked by user: " + lockedByUser);
        } catch (Exception e) {
            fail("testGetLockedByUser failed with exception: " + e.getClass().toString() + " : " + e.getLocalizedMessage());
        }
    }

    /**
     * Test of getLastCheckInDate method, of class com.qumasoft.qvcslib.LogFile.
     */
    @Test
    public void testGetLastCheckInDate() {
        System.out.println("testGetLastCheckInDate");

        // Create the archive file object.
        LogFile testArchive = new LogFile(System.getProperty("user.dir") + File.separator + "TestLockArchive.kbwb");

        try {
            Date lastCheckInDate = testArchive.getLastCheckInDate();
            System.out.println("Last checkin date: " + lastCheckInDate.toString());
        } catch (Exception e) {
            fail("testGetLastCheckInDate failed with exception: " + e.getClass().toString() + " : " + e.getLocalizedMessage());
        }
    }

    /**
     * Test of getLastEditBy method, of class com.qumasoft.qvcslib.LogFile.
     */
    @Test
    public void testGetLastEditBy() {
        System.out.println("testGetLastEditBy");

        // Create the archive file object.
        LogFile testArchive = new LogFile(System.getProperty("user.dir") + File.separator + "TestLockArchive.kbwb");

        try {
            String lastEditBy = testArchive.getLastEditBy();
            System.out.println("Last edit by: " + lastEditBy);
        } catch (Exception e) {
            fail("testGetLastEditBy failed with exception: " + e.getClass().toString() + " : " + e.getLocalizedMessage());
        }
    }

    /**
     * Test of getWorkfileInLocation method, of class com.qumasoft.qvcslib.LogFile.
     */
    @Test
    public void testGetWorkfileInLocation() {
        System.out.println("testGetWorkfileInLocation");

        // Create the archive file object.
        LogFile testArchive = new LogFile(System.getProperty("user.dir") + File.separator + "TestLockArchive.kbwb");

        try {
            String workfileInLocation = testArchive.getWorkfileInLocation();
            System.out.println("Workfile in location: " + workfileInLocation);
        } catch (Exception e) {
            fail("testGetLastEditBy failed with exception: " + e.getClass().toString() + " : " + e.getLocalizedMessage());
        }
    }

    /**
     * Test of getLockCount method, of class com.qumasoft.qvcslib.LogFile.
     */
    @Test
    public void testGetLockCount() {
        System.out.println("testGetLockCount");

        // Create the archive file object.
        LogFile testArchive = new LogFile(System.getProperty("user.dir") + File.separator + "TestLockArchive.kbwb");

        try {
            int lockCount = testArchive.getLockCount();
            System.out.println("Lock count: " + lockCount);
        } catch (Exception e) {
            fail("testGetLockCount failed with exception: " + e.getClass().toString() + " : " + e.getLocalizedMessage());
        }
    }

    /**
     * Test of testGetDefaultRevisionString method, of class com.qumasoft.qvcslib.LogFile.
     */
    @Test
    public void testGetDefaultRevisionString() {
        System.out.println("testGetDefaultRevisionString");

        // Create the archive file object.
        LogFile testArchive = new LogFile(System.getProperty("user.dir") + File.separator + "TestLockArchive.kbwb");

        try {
            String defaultRevisionString = testArchive.getDefaultRevisionString();
            System.out.println("Default revision string: " + defaultRevisionString);
        } catch (Exception e) {
            fail("testGetTipRevisionString failed with exception: " + e.getClass().toString() + " : " + e.getLocalizedMessage());
        }
    }

    /**
     * Test of getHeaderInfo method, of class com.qumasoft.qvcslib.LogFile.
     */
    @Test
    public void testGetHeaderInfo() {
        System.out.println("testGetHeaderInfo");

        // Create the archive file object.
        LogFile testArchive = new LogFile(System.getProperty("user.dir") + File.separator + "TestLockArchive.kbwb");

        try {
            LogFileHeaderInfo headerInfo = testArchive.getLogFileHeaderInfo();
            System.out.println("Some header info (module description): " + headerInfo.getModuleDescription());
        } catch (Exception e) {
            fail("testGetHeaderInfo failed with exception: " + e.getClass().toString() + " : " + e.getLocalizedMessage());
        }
    }

    /**
     * Test of getRevisionCount method, of class com.qumasoft.qvcslib.LogFile.
     */
    @Test
    public void testGetRevisionCount() {
        System.out.println("testGetRevisionCount");

        // Create the archive file object.
        LogFile testArchive = new LogFile(System.getProperty("user.dir") + File.separator + "TestLockArchive.kbwb");

        try {
            int revisionCount = testArchive.getRevisionCount();
            System.out.println("Revision count: " + revisionCount);
        } catch (Exception e) {
            fail("testGetRevisionCount failed with exception: " + e.getClass().toString() + " : " + e.getLocalizedMessage());
        }
    }

    /**
     * Test of getRevisionInformation method, of class com.qumasoft.qvcslib.LogFile.
     */
    @Test
    public void testGetRevisionInformation() {
        System.out.println("testGetRevisionInformation");

        // Create the archive file object.
        LogFile testArchive = new LogFile(System.getProperty("user.dir") + File.separator + "TestLockArchive.kbwb");

        try {
            RevisionInformation revisionInformation = testArchive.getRevisionInformation();
            for (int i = 0; i < testArchive.getRevisionCount(); i++) {
                RevisionHeader revisionHeader = revisionInformation.getRevisionHeader(i);
                System.out.println("Revision header: " + revisionHeader.toString());
            }
        } catch (Exception e) {
            fail("testGetRevisionInformation failed with exception: " + e.getClass().toString() + " : " + e.getLocalizedMessage());
        }
    }

    /**
     * Test of lockRevision method, of class com.qumasoft.qvcslib.LogFile.
     */
    @Test
    public void testLockRevision() {
        System.out.println("testLockRevision");

        // Create the archive file object.
        LogFile testArchive = new LogFile(System.getProperty("user.dir") + File.separator + "TestLockArchive.kbwb");

        try {
            // Create the command line object
            LogFileOperationLockRevisionCommandArgs commandArgs = new LogFileOperationLockRevisionCommandArgs();

            // Set the elements of the command args
            commandArgs.setUserName("JimVoris");
            commandArgs.setRevisionString("1.3");
            commandArgs.setOutputFileName(System.getProperty("user.dir") + File.separator + "TestLockArchive.java");
            commandArgs.setFullWorkfileName(System.getProperty("user.dir") + File.separator + "TestLockArchive.java");
            commandArgs.setShortWorkfileName("TestLockArchive.java");

            if (!testArchive.lockRevision(commandArgs)) {
                fail("Failed lock revision test");
            } else {
                System.out.println("testLockRevision passed.");
            }
            verifyArchiveFile(testArchive);
        } catch (QVCSException e) {
            fail("Caught QVCSException: " + e.getLocalizedMessage() + " in testLockRevision()");
        }
    }

    /**
     * Test of unlockRevision method, of class com.qumasoft.qvcslib.LogFile.
     */
    @Test
    public void testUnlockRevision() {
        System.out.println("testUnlockRevision");

        // Add your test code below by replacing the default call to fail.
        LogFile testArchive = new LogFile(System.getProperty("user.dir") + File.separator + "TestLockArchive.kbwb");

        try {
            // Create the command line object
            LogFileOperationUnlockRevisionCommandArgs commandArgs = new LogFileOperationUnlockRevisionCommandArgs();

            // Set the elements of the command args
            commandArgs.setUserName("JimVoris");
            commandArgs.setRevisionString("1.3");
            commandArgs.setOutputFileName(System.getProperty("user.dir") + File.separator + "TestLockArchive.java");
            commandArgs.setFullWorkfileName(System.getProperty("user.dir") + File.separator + "TestLockArchive.java");
            commandArgs.setShortWorkfileName("TestLockArchive.java");

            if (!testArchive.unlockRevision(commandArgs)) {
                fail("Failed unlock revision test");
            } else {
                System.out.println("testUnlockRevision passed.");
            }
            verifyArchiveFile(testArchive);
        } catch (QVCSException e) {
            fail("Caught QVCSException: " + e.getLocalizedMessage() + " in testUnlockRevision()");
        }
    }

    /**
     * Test of lockRevision method, of class com.qumasoft.qvcslib.LogFile.
     */
    @Test
    public void testLockRevisionDifferentUser() {
        System.out.println("testLockRevisionDifferentUser");

        // Create the archive file object.
        LogFile testArchive = new LogFile(System.getProperty("user.dir") + File.separator + "TestLockArchive.kbwb");

        try {
            // Create the command line object
            LogFileOperationLockRevisionCommandArgs commandArgs = new LogFileOperationLockRevisionCommandArgs();

            // Set the elements of the command args
            commandArgs.setUserName("BruceVoris");
            commandArgs.setRevisionString("1.3");
            commandArgs.setOutputFileName(System.getProperty("user.dir") + File.separator + "TestLockArchive.java");
            commandArgs.setFullWorkfileName(System.getProperty("user.dir") + File.separator + "TestLockArchive.java");
            commandArgs.setShortWorkfileName("TestLockArchive.java");

            if (!testArchive.lockRevision(commandArgs)) {
                fail("Failed lock revision test");
            } else {
                System.out.println("testLockRevisionDifferentUser passed.");
            }
            verifyArchiveFile(testArchive);
        } catch (QVCSException e) {
            fail("Caught QVCSException: " + e.getLocalizedMessage() + " in testLockRevisionDifferentUser()");
        }
    }

    /**
     * Test of unlockRevision method, of class com.qumasoft.qvcslib.LogFile.
     */
    @Test
    public void testUnlockRevisionDifferentUser() {
        System.out.println("testUnlockRevisionDifferentUser");

        // Add your test code below by replacing the default call to fail.
        LogFile testArchive = new LogFile(System.getProperty("user.dir") + File.separator + "TestLockArchive.kbwb");

        try {
            // Create the command line object
            LogFileOperationUnlockRevisionCommandArgs commandArgs = new LogFileOperationUnlockRevisionCommandArgs();

            // Set the elements of the command args
            commandArgs.setUserName("BruceVoris");
            commandArgs.setRevisionString("1.3");
            commandArgs.setOutputFileName(System.getProperty("user.dir") + File.separator + "TestLockArchive.java");
            commandArgs.setFullWorkfileName(System.getProperty("user.dir") + File.separator + "TestLockArchive.java");
            commandArgs.setShortWorkfileName("TestLockArchive.java");

            if (!testArchive.unlockRevision(commandArgs)) {
                fail("Failed unlock revision test");
            } else {
                System.out.println("testUnlockRevision passed.");
            }
            verifyArchiveFile(testArchive);
        } catch (QVCSException e) {
            fail("Caught QVCSException: " + e.getLocalizedMessage() + " in testUnlockRevisionDifferentUser()");
        }
    }

    /**
     * Test of labelRevision method, of class com.qumasoft.qvcslib.LogFile.
     */
    @Test
    public void testLabelRevision() {
        System.out.println("testLabelRevision");

        LogFile testArchive = new LogFile(System.getProperty("user.dir") + File.separator + "TestLockArchive.kbwb");

        try {
            // Create the command line object
            LogFileOperationLabelRevisionCommandArgs commandArgs = new LogFileOperationLabelRevisionCommandArgs();

            // Set the elements of the command args.
            commandArgs.setUserName("JimVoris");
            commandArgs.setRevisionString(QVCSConstants.QVCS_DEFAULT_REVISION);
            commandArgs.setLabelString("This is the test label string");

            if (!testArchive.labelRevision(commandArgs)) {
                fail("Failed 1st label revision test");
            } else {
                System.out.println("testLabelRevision passed 1st test.");
            }

            // Set the elements of the command args.
            commandArgs = new LogFileOperationLabelRevisionCommandArgs();
            commandArgs.setUserName("JimVoris");
            commandArgs.setDuplicateFlag(true);
            commandArgs.setDuplicateLabelString("This is the test label string");
            commandArgs.setLabelString("This is the duplicate label");

            if (!testArchive.labelRevision(commandArgs)) {
                fail("Failed 2nd label revision test");
            } else {
                System.out.println("testLabelRevision passed 2nd test.");
            }

            // Set the elements of the command args.
            commandArgs = new LogFileOperationLabelRevisionCommandArgs();
            commandArgs.setUserName("JimVoris");
            commandArgs.setRevisionString("1.2");
            commandArgs.setLabelString("This is the test label string");
            commandArgs.setReuseLabelFlag(true);

            if (!testArchive.labelRevision(commandArgs)) {
                fail("Failed 3rd label revision test");
            } else {
                System.out.println("testLabelRevision passed 3rd test.");
            }

            // Set the elements of the command args.
            commandArgs = new LogFileOperationLabelRevisionCommandArgs();
            commandArgs.setUserName("JimVoris");
            commandArgs.setRevisionString("1.2");
            commandArgs.setLabelString("This is the test label string");

            if (testArchive.labelRevision(commandArgs)) {
                fail("Failed 4th label revision test");
            } else {
                System.out.println("testLabelRevision passed 4th test.");
            }

            // Set the elements of the command args.
            commandArgs = new LogFileOperationLabelRevisionCommandArgs();
            commandArgs.setUserName("JimVoris");
            commandArgs.setRevisionString("1.2");
            commandArgs.setLabelString("This is the floating label string");
            commandArgs.setFloatingFlag(true);

            if (!testArchive.labelRevision(commandArgs)) {
                fail("Failed 5th label revision test");
            } else {
                System.out.println("testLabelRevision passed 5th test.");
            }

            // Set the elements of the command args.
            commandArgs = new LogFileOperationLabelRevisionCommandArgs();
            commandArgs.setUserName("JimVoris");
            commandArgs.setRevisionString("1.2000");
            commandArgs.setLabelString("This is a label we won't apply");

            if (testArchive.labelRevision(commandArgs)) {
                fail("Failed 6th label revision test");
            } else {
                System.out.println("testLabelRevision passed 6th test.");

            }
            // Set the elements of the command args.
            commandArgs = new LogFileOperationLabelRevisionCommandArgs();
            commandArgs.setUserName("JimVoris");
            commandArgs.setLabelString("This is a label we won't apply");
            commandArgs.setDuplicateFlag(true);
            commandArgs.setDuplicateLabelString("This is a label that does not exist");

            if (!testArchive.labelRevision(commandArgs)) {
                fail("Failed 7th label revision test");
            } else {
                System.out.println("testLabelRevision passed 7th test.");
            }
            verifyArchiveFile(testArchive);
        } catch (QVCSException e) {
            fail("Caught QVCSException: " + e.getLocalizedMessage() + " in testLabelRevision()");
        }
    }

    /**
     * Test of labelRevision method, of class com.qumasoft.qvcslib.LogFile.
     */
    @Test
    public void testUnLabelRevision() {
        System.out.println("testUnLabelRevision");

        LogFile testArchive = new LogFile(System.getProperty("user.dir") + File.separator + "TestLockArchive.kbwb");

        try {
            // Create the command line object
            LogFileOperationUnLabelRevisionCommandArgs commandArgs = new LogFileOperationUnLabelRevisionCommandArgs();

            // Set the elements of the command args.
            commandArgs.setUserName("JimVoris");
            commandArgs.setLabelString("This is the test label string");

            if (!testArchive.unLabelRevision(commandArgs)) {
                fail("Failed 1st unlabel test");
            } else {
                System.out.println("testUnLabelRevision passed 1st test.");
            }

            // Set the elements of the command args.
            commandArgs = new LogFileOperationUnLabelRevisionCommandArgs();
            commandArgs.setUserName("JimVoris");
            commandArgs.setLabelString("This is the duplicate label");

            if (!testArchive.unLabelRevision(commandArgs)) {
                fail("Failed 2nd unlabel test");
            } else {
                System.out.println("testUnLabelRevision passed 2nd test.");
            }

            // Set the elements of the command args.
            commandArgs = new LogFileOperationUnLabelRevisionCommandArgs();
            commandArgs.setUserName("JimVoris");
            commandArgs.setLabelString("This is the test label string");

            if (testArchive.unLabelRevision(commandArgs)) {
                fail("Failed 3rd unlabel revision test");
            } else {
                System.out.println("testUnLabelRevision passed 3rd test.");
            }

            // Set the elements of the command args.
            commandArgs = new LogFileOperationUnLabelRevisionCommandArgs();
            commandArgs.setUserName("JimVoris");
            commandArgs.setLabelString("This is the floating label string");

            if (!testArchive.unLabelRevision(commandArgs)) {
                fail("Failed 5th unlabel revision test");
            } else {
                System.out.println("testUnLabelRevision passed 4th test.");
            }

            // Set the elements of the command args.
            commandArgs = new LogFileOperationUnLabelRevisionCommandArgs();
            commandArgs.setUserName("JimVoris");
            commandArgs.setLabelString("This is a label we won't apply");

            if (testArchive.unLabelRevision(commandArgs)) {
                fail("Failed 6th label revision test");
            } else {
                System.out.println("testUnLabelRevision passed 5th test.");

            }
            verifyArchiveFile(testArchive);
        } catch (QVCSException e) {
            fail("Caught QVCSException: " + e.getLocalizedMessage() + " in testLabelRevision()");
        }
    }

    /**
     * Test of setModuleDescription method, of class com.qumasoft.qvcslib.LogFile.
     */
    @Test
    public void testSetModuleDescription() {
        System.out.println("testSetModuleDescription");

        LogFile testArchive = new LogFile(System.getProperty("user.dir") + File.separator + "TestLockArchive.kbwb");

        try {
            String newModuleDescription = "This is the cool new module description dude.";
            if (!testArchive.setModuleDescription("JimVoris", newModuleDescription)) {
                fail("Failed set module description test");
            } else {
                System.out.println("testSetModuleDescription passed 1st test.");
            }

            if (!testArchive.getModuleDescription().equals(newModuleDescription)) {
                fail("Failed to confirm that the module description has changed.");
            }

            verifyArchiveFile(testArchive);
        } catch (QVCSException e) {
            fail("Caught QVCSException: " + e.getLocalizedMessage() + " in testSetRevisionDescription()");
        }
    }

    /**
     * Test of setRevisionDescription method, of class com.qumasoft.qvcslib.LogFile.
     */
    @Test
    public void testSetRevisionDescription() {
        System.out.println("testSetRevisionDescription");

        LogFile testArchive = new LogFile(System.getProperty("user.dir") + File.separator + "TestLockArchive.kbwb");

        try {
            // Create the command line object
            LogFileOperationSetRevisionDescriptionCommandArgs commandArgs = new LogFileOperationSetRevisionDescriptionCommandArgs();

            // Set the elements of the command args.
            String newDescription = "This is the new revision description for revision 1.2";
            commandArgs.setUserName("JimVoris");
            commandArgs.setRevisionString("1.2");
            commandArgs.setShortWorkfileName(testArchive.getShortWorkfileName());
            commandArgs.setRevisionDescription("This is the new revision description for revision 1.2");

            if (!testArchive.setRevisionDescription(commandArgs)) {
                fail("Failed set revision description test");
            } else {
                System.out.println("testSetRevisionDescription passed 1st test.");
            }

            if (!testArchive.getRevisionDescription("1.2").equals(newDescription)) {
                fail("Failed to confirm that the revision description has changed.");
            }

            verifyArchiveFile(testArchive);
        } catch (QVCSException e) {
            fail("Caught QVCSException: " + e.getLocalizedMessage() + " in testSetRevisionDescription()");
        }
    }

    @Test
    public void testCreateArchive() {
        System.out.println("testCreateArchive");

        // Add your test code below by replacing the default call to fail.
        LogFile testArchive = new LogFile(System.getProperty("user.dir") + File.separator + "TestCreateArchive.kbwb");

        try {
            LogFileOperationCreateArchiveCommandArgs commandArgs = new LogFileOperationCreateArchiveCommandArgs();
            commandArgs.setUserName("JimVoris");
            commandArgs.setArchiveDescription("This is the archive description.");
            commandArgs.setWorkfileName(System.getProperty("user.dir") + File.separator + "TestCreateArchive.java");
            File inputFile = new File(System.getProperty("user.dir") + File.separator + "TestCreateArchive.java");
            commandArgs.setInputfileTimeStamp(new Date(inputFile.lastModified()));
            AbstractProjectProperties projectProperties = ProjectPropertiesFactory.getProjectPropertiesFactory().
                    buildProjectProperties(TestHelper.getTestProjectName(), QVCSConstants.QVCS_SERVED_PROJECT_TYPE);
            if (!testArchive.createArchive(commandArgs, projectProperties, inputFile.getAbsolutePath())) {
                fail("Failed create archive test");
            } else {
                System.out.println("testCreateArchive passed.");
            }
            verifyArchiveFile(testArchive);
        } catch (Exception e) {
            fail("Caught Exception: " + e.getLocalizedMessage() + " in testCreateArchive()");
        }
    }

    /**
     * Test of checkOutRevision method, of class com.qumasoft.qvcslib.LogFile.
     */
    @Test
    public void testCheckOutCheckInRevision() {
        System.out.println("testCheckOutCheckInRevision");

        // This tests check-out and check-in of a new branch.  This automatically creates a new branch.
        LogFile testArchive = new LogFile(System.getProperty("user.dir") + File.separator + "TestCheckOutArchive.kbwb");

        try {
            LogFileOperationCheckOutCommandArgs checkOutCommandArgs = new LogFileOperationCheckOutCommandArgs();
            checkOutCommandArgs.setUserName("JimVoris");
            checkOutCommandArgs.setRevisionString("1.3");
            checkOutCommandArgs.setOutputFileName(System.getProperty("user.dir") + File.separator + "TestCheckOutArchive.java");
            checkOutCommandArgs.setFullWorkfileName(System.getProperty("user.dir") + File.separator + "TestCheckOutArchive.java");
            checkOutCommandArgs.setShortWorkfileName("TestCheckOutArchive.java");

            if (!testArchive.checkOutRevision(checkOutCommandArgs,
                    System.getProperty("user.dir") + File.separator + "TestCheckOutArchive.java")) {
                fail("Failed first checkout test");
            } else {
                System.out.println("Checked out revision: " + checkOutCommandArgs.getRevisionString());
                System.out.println("Passed first checkout test");
            }

            LogFileOperationCheckInCommandArgs checkInCommandArgs = new LogFileOperationCheckInCommandArgs();
            checkInCommandArgs.setUserName("JimVoris");
            checkInCommandArgs.setLockedRevisionString("1.3");
            checkInCommandArgs.setCheckInComment("This is the check in comment for a new branch revision");
            File inputFile = new File(System.getProperty("user.dir") + File.separator + "TestCheckInArchive.java");
            checkInCommandArgs.setInputfileTimeStamp(new Date(inputFile.lastModified()));

            if (!testArchive.checkInRevision(checkInCommandArgs,
                    System.getProperty("user.dir") + File.separator + "TestCheckInArchive.java", false)) {
                fail("Failed first check-in revision test");
            } else {
                System.out.println("Checked in revision: " + checkInCommandArgs.getNewRevisionString());
                System.out.println("Passed first check-in revision test");
            }

            checkOutCommandArgs.setRevisionString("1.7");
            if (!testArchive.checkOutRevision(checkOutCommandArgs,
                    System.getProperty("user.dir") + File.separator + "TestCheckOutArchive.java")) {
                fail("Failed 2nd checkout test");
            } else {
                System.out.println("Checked out revision: " + checkOutCommandArgs.getRevisionString());
                System.out.println("Passed 2nd checkout test");
            }

            checkInCommandArgs.setLockedRevisionString("1.7");
            checkInCommandArgs.setCheckInComment("This is the check in comment for a new tip TRUNK revision.");
            checkInCommandArgs.setFullWorkfileName(System.getProperty("user.dir") + File.separator + "TestCheckInArchive.java");
            File input2File = new File(System.getProperty("user.dir") + File.separator + "TestCheckInArchive.java");
            checkInCommandArgs.setInputfileTimeStamp(new Date(input2File.lastModified()));
            if (!testArchive.checkInRevision(checkInCommandArgs,
                    System.getProperty("user.dir") + File.separator + "TestCheckInArchive.java", false)) {
                fail("Failed 2nd check-in revision test");
            } else {
                System.out.println("Checked in revision: " + checkInCommandArgs.getNewRevisionString());
                System.out.println("Passed 2nd check-in revision test");
            }

            checkOutCommandArgs.setRevisionString("1.3.1.1");
            if (!testArchive.checkOutRevision(checkOutCommandArgs,
                    System.getProperty("user.dir") + File.separator + "TestCheckOutArchive.java")) {
                fail("Failed 3rd checkout test");
            } else {
                System.out.println("Checked out revision: " + checkOutCommandArgs.getRevisionString());
            }

            checkInCommandArgs.setLockedRevisionString("1.3.1.1");
            checkInCommandArgs.setCheckInComment("This is the check in comment for a new tip branch revision.");
            File input3File = new File(System.getProperty("user.dir") + File.separator + "TestCheckInAgainArchive.java");
            checkInCommandArgs.setInputfileTimeStamp(new Date(input3File.lastModified()));
            if (!testArchive.checkInRevision(checkInCommandArgs,
                    System.getProperty("user.dir") + File.separator + "TestCheckInAgainArchive.java", false)) {
                fail("Failed 3rd check-in revision test");
            } else {
                System.out.println("Checked in revision: " + checkInCommandArgs.getNewRevisionString());
            }

            checkOutCommandArgs.setRevisionString("1.3.1.2");
            if (!testArchive.checkOutRevision(checkOutCommandArgs,
                    System.getProperty("user.dir") + File.separator + "TestCheckOutArchive.java")) {
                fail("Failed 3rd checkout test");
            } else {
                System.out.println("Checked out revision: " + checkOutCommandArgs.getRevisionString());
            }

            checkInCommandArgs.setLockedRevisionString("1.3.1.2");
            checkInCommandArgs.setCheckInComment("This is the check in comment for a new tip branch revision.");
            checkInCommandArgs.setForceBranchFlag(true);
            checkInCommandArgs.setCreateNewRevisionIfEqual(true);
            input3File = new File(System.getProperty("user.dir") + File.separator + "TestCheckInAgainArchive.java");
            checkInCommandArgs.setInputfileTimeStamp(new Date(input3File.lastModified()));
            if (!testArchive.checkInRevision(checkInCommandArgs,
                    System.getProperty("user.dir") + File.separator + "TestCheckInAgainArchive.java", false)) {
                fail("Failed 3rd check-in revision test");
            } else {
                System.out.println("Checked in revision: " + checkInCommandArgs.getNewRevisionString());
            }

            // Test the case where we don't change the file, but we do create a new revision.
            checkOutCommandArgs.setRevisionString(QVCSConstants.QVCS_DEFAULT_REVISION);
            checkOutCommandArgs.setOutputFileName(System.getProperty("user.dir") + File.separator + "TestCheckOutArchive.java");
            checkOutCommandArgs.setFullWorkfileName(System.getProperty("user.dir") + File.separator + "TestCheckOutArchive.java");
            checkOutCommandArgs.setShortWorkfileName("TestCheckOutArchive.java");

            if (!testArchive.checkOutRevision(checkOutCommandArgs,
                    System.getProperty("user.dir") + File.separator + "TestCheckOutArchive.java")) {
                fail("Failed last checkout test");
            } else {
                System.out.println("Checked out revision: " + checkOutCommandArgs.getRevisionString());
                System.out.println("Passed last checkout test");
            }

            System.out.println("Before check-in the default revision is now: " + testArchive.getDefaultRevisionString());
            LogFileOperationCheckInCommandArgs lastCheckInCommandArgs = new LogFileOperationCheckInCommandArgs();
            lastCheckInCommandArgs.setUserName("JimVoris");
            lastCheckInCommandArgs.setLockedRevisionString(QVCSConstants.QVCS_DEFAULT_REVISION);
            lastCheckInCommandArgs.setCreateNewRevisionIfEqual(true);
            lastCheckInCommandArgs.setFullWorkfileName(System.getProperty("user.dir") + File.separator + "TestCheckOutArchive.java");
            lastCheckInCommandArgs.setCheckInComment("This is the check in comment for a new equal tip revision");
            File lastInputFile = new File(System.getProperty("user.dir") + File.separator + "TestCheckOutArchive.java");
            lastCheckInCommandArgs.setInputfileTimeStamp(new Date(inputFile.lastModified()));

            if (!testArchive.checkInRevision(lastCheckInCommandArgs,
                    System.getProperty("user.dir") + File.separator + "TestCheckOutArchive.java", false)) {
                fail("Failed last check-in revision test");
            } else {
                System.out.println("Checked in revision: " + lastCheckInCommandArgs.getNewRevisionString());
                System.out.println("Passed last check-in revision test");
            }

            // Test the case where we don't change the file, but we do create a new revision.
            checkOutCommandArgs.setRevisionString(QVCSConstants.QVCS_DEFAULT_REVISION);
            checkOutCommandArgs.setOutputFileName(System.getProperty("user.dir") + File.separator + "TestCheckOutArchive.java");
            checkOutCommandArgs.setFullWorkfileName(System.getProperty("user.dir") + File.separator + "TestCheckOutArchive.java");
            checkOutCommandArgs.setShortWorkfileName("TestCheckOutArchive.java");

            if (!testArchive.checkOutRevision(checkOutCommandArgs,
                    System.getProperty("user.dir") + File.separator + "TestCheckOutArchive.java")) {
                fail("Failed last checkout test");
            } else {
                System.out.println("Checked out revision: " + checkOutCommandArgs.getRevisionString());
                System.out.println("Passed last checkout test");
            }

            System.out.println("Before check-in the default revision is now: " + testArchive.getDefaultRevisionString());
            lastCheckInCommandArgs = new LogFileOperationCheckInCommandArgs();
            lastCheckInCommandArgs.setUserName("JimVoris");
            lastCheckInCommandArgs.setLockedRevisionString(QVCSConstants.QVCS_DEFAULT_REVISION);
            lastCheckInCommandArgs.setCreateNewRevisionIfEqual(true);
            lastCheckInCommandArgs.setForceBranchFlag(true);
            lastCheckInCommandArgs.setFullWorkfileName(System.getProperty("user.dir") + File.separator + "TestCheckOutArchive.java");
            lastCheckInCommandArgs.setCheckInComment("This is the check in comment for a forced branch off of the tip of the TRUNK.");
            lastInputFile = new File(System.getProperty("user.dir") + File.separator + "TestCheckOutArchive.java");
            lastCheckInCommandArgs.setInputfileTimeStamp(new Date(inputFile.lastModified()));

            if (!testArchive.checkInRevision(lastCheckInCommandArgs,
                    System.getProperty("user.dir") + File.separator + "TestCheckOutArchive.java", false)) {
                fail("Failed last check-in revision test");
            } else {
                System.out.println("Checked in revision: " + lastCheckInCommandArgs.getNewRevisionString());
                System.out.println("Passed last check-in revision test");
            }
            verifyArchiveFile(testArchive);
        } catch (QVCSException e) {
            fail("Caught QVCSException: " + e.getLocalizedMessage() + " in testCheckOutCheckInRevision()");
        }
    }

    @Test
    public void testCheckOutCheckInRevisionWithLabels() {
        System.out.println("testCheckOutCheckInRevisionWithLabels");

        LogFile testArchive = new LogFile(System.getProperty("user.dir") + File.separator + "TestCheckOutArchive.kbwb");

        try {
            LogFileOperationCheckOutCommandArgs checkOutCommandArgs = new LogFileOperationCheckOutCommandArgs();
            checkOutCommandArgs.setUserName("JimVoris");
            checkOutCommandArgs.setRevisionString("1.10");
            checkOutCommandArgs.setOutputFileName(System.getProperty("user.dir") + File.separator + "TestCheckOutArchive.java");
            checkOutCommandArgs.setFullWorkfileName(System.getProperty("user.dir") + File.separator + "TestCheckOutArchive.java");
            checkOutCommandArgs.setShortWorkfileName("TestCheckOutArchive.java");

            if (!testArchive.checkOutRevision(checkOutCommandArgs,
                    System.getProperty("user.dir") + File.separator + "TestCheckOutArchive.java")) {
                fail("Failed first checkout test");
            } else {
                System.out.println("Checked out revision: " + checkOutCommandArgs.getRevisionString());
                System.out.println("Passed first checkout test");
            }

            LogFileOperationCheckInCommandArgs checkInCommandArgs = new LogFileOperationCheckInCommandArgs();
            checkInCommandArgs.setUserName("JimVoris");
            checkInCommandArgs.setLockedRevisionString("1.10");
            checkInCommandArgs.setCheckInComment("This is the check in comment for a new labeled branch revision");
            checkInCommandArgs.setApplyLabelFlag(true);
            checkInCommandArgs.setLabel("This label goes on the 1.10.1.1 revision");
            File inputFile = new File(System.getProperty("user.dir") + File.separator + "TestCheckInArchive.java");
            checkInCommandArgs.setInputfileTimeStamp(new Date(inputFile.lastModified()));

            if (!testArchive.checkInRevision(checkInCommandArgs,
                    System.getProperty("user.dir") + File.separator + "TestCheckInArchive.java", false)) {
                fail("Failed first check-in revision test");
            } else {
                System.out.println("Checked in revision: " + checkInCommandArgs.getNewRevisionString());
            }

            checkOutCommandArgs.setRevisionString(QVCSConstants.QVCS_DEFAULT_REVISION);
            if (!testArchive.checkOutRevision(checkOutCommandArgs,
                    System.getProperty("user.dir") + File.separator + "TestCheckOutArchive.java")) {
                fail("Failed 2nd checkout test");
            } else {
                System.out.println("Checked out revision: " + checkOutCommandArgs.getRevisionString());
            }

            checkInCommandArgs.setLockedRevisionString(checkOutCommandArgs.getRevisionString());
            checkInCommandArgs.setCheckInComment("This is the check in comment for a new tip TRUNK revision.");
            checkInCommandArgs.setApplyLabelFlag(true);
            checkInCommandArgs.setLabel("This label goes on the new TRUNK revision.");
            checkInCommandArgs.setFullWorkfileName(System.getProperty("user.dir") + File.separator + "TestCheckInArchive.java");
            File input2File = new File(System.getProperty("user.dir") + File.separator + "TestCheckInArchive.java");
            checkInCommandArgs.setInputfileTimeStamp(new Date(input2File.lastModified()));
            if (!testArchive.checkInRevision(checkInCommandArgs,
                    System.getProperty("user.dir") + File.separator + "TestCheckInArchive.java", false)) {
                fail("Failed 2nd check-in revision test");
            } else {
                System.out.println("Checked in revision: " + checkInCommandArgs.getNewRevisionString());
            }

            checkOutCommandArgs.setRevisionString("1.10.1.1");
            if (!testArchive.checkOutRevision(checkOutCommandArgs,
                    System.getProperty("user.dir") + File.separator + "TestCheckOutArchive.java")) {
                fail("Failed 3rd checkout test");
            } else {
                System.out.println("Checked out revision: " + checkOutCommandArgs.getRevisionString());
            }

            checkInCommandArgs.setLockedRevisionString("1.10.1.1");
            checkInCommandArgs.setCheckInComment("This is the check in comment for a new tip branch revision.");
            checkInCommandArgs.setApplyLabelFlag(true);
            checkInCommandArgs.setLabel("This label goes on the new branch tip revision.");
            File input3File = new File(System.getProperty("user.dir") + File.separator + "TestCheckInAgainArchive.java");
            checkInCommandArgs.setInputfileTimeStamp(new Date(input3File.lastModified()));
            if (!testArchive.checkInRevision(checkInCommandArgs,
                    System.getProperty("user.dir") + File.separator + "TestCheckInAgainArchive.java", false)) {
                fail("Failed 3rd check-in revision test");
            } else {
                System.out.println("Checked in revision: " + checkInCommandArgs.getNewRevisionString());
            }

            // Test the case where we don't change the file, but we do create a new revision.
            checkOutCommandArgs.setRevisionString(QVCSConstants.QVCS_DEFAULT_REVISION);
            checkOutCommandArgs.setOutputFileName(System.getProperty("user.dir") + File.separator + "TestCheckOutArchive.java");
            checkOutCommandArgs.setFullWorkfileName(System.getProperty("user.dir") + File.separator + "TestCheckOutArchive.java");
            checkOutCommandArgs.setShortWorkfileName("TestCheckOutArchive.java");

            if (!testArchive.checkOutRevision(checkOutCommandArgs,
                    System.getProperty("user.dir") + File.separator + "TestCheckOutArchive.java")) {
                fail("Failed last checkout test");
            } else {
                System.out.println("Checked out revision: " + checkOutCommandArgs.getRevisionString());
                System.out.println("Passed last checkout test");
            }

            System.out.println("Before last check-in the default revision is now: " + testArchive.getDefaultRevisionString());
            LogFileOperationCheckInCommandArgs lastCheckInCommandArgs = new LogFileOperationCheckInCommandArgs();
            String labelString = "The is a cool label applied to this new revision";
            lastCheckInCommandArgs.setUserName("JimVoris");
            lastCheckInCommandArgs.setLockedRevisionString(QVCSConstants.QVCS_DEFAULT_REVISION);
            lastCheckInCommandArgs.setCreateNewRevisionIfEqual(true);
            lastCheckInCommandArgs.setFullWorkfileName(System.getProperty("user.dir") + File.separator + "TestCheckOutArchive.java");
            lastCheckInCommandArgs.setCheckInComment("This is the check in comment for a new equal tip revision");
            lastCheckInCommandArgs.setApplyLabelFlag(true);
            lastCheckInCommandArgs.setLabel(labelString);
            File lastInputFile = new File(System.getProperty("user.dir") + File.separator + "TestCheckOutArchive.java");
            lastCheckInCommandArgs.setInputfileTimeStamp(new Date(inputFile.lastModified()));

            if (!testArchive.checkInRevision(lastCheckInCommandArgs,
                    System.getProperty("user.dir") + File.separator + "TestCheckOutArchive.java", false)) {
                fail("Failed last check-in revision test");
            } else {
                System.out.println("Checked in revision: " + lastCheckInCommandArgs.getNewRevisionString());
                System.out.println("Passed last check-in revision test");
            }

            // Test the case where we don't change the file, but we apply a label at checkin.
            checkOutCommandArgs.setRevisionString(QVCSConstants.QVCS_DEFAULT_REVISION);
            checkOutCommandArgs.setOutputFileName(System.getProperty("user.dir") + File.separator + "TestCheckOutArchive.java");
            checkOutCommandArgs.setFullWorkfileName(System.getProperty("user.dir") + File.separator + "TestCheckOutArchive.java");
            checkOutCommandArgs.setShortWorkfileName("TestCheckOutArchive.java");

            if (!testArchive.checkOutRevision(checkOutCommandArgs,
                    System.getProperty("user.dir") + File.separator + "TestCheckOutArchive.java")) {
                fail("Failed last checkout test");
            } else {
                System.out.println("Checked out revision: " + checkOutCommandArgs.getRevisionString());
                System.out.println("Passed last checkout test");
            }

            lastCheckInCommandArgs = new LogFileOperationCheckInCommandArgs();
            labelString = "The is a another cool label applied to the tip revision";
            lastCheckInCommandArgs.setUserName("JimVoris");
            lastCheckInCommandArgs.setLockedRevisionString(QVCSConstants.QVCS_DEFAULT_REVISION);
            lastCheckInCommandArgs.setCreateNewRevisionIfEqual(false);
            lastCheckInCommandArgs.setFullWorkfileName(System.getProperty("user.dir") + File.separator + "TestCheckOutArchive.java");
            lastCheckInCommandArgs.setCheckInComment("This is the check in comment for a new equal tip revision");
            lastCheckInCommandArgs.setApplyLabelFlag(true);
            lastCheckInCommandArgs.setLabel(labelString);
            lastInputFile = new File(System.getProperty("user.dir") + File.separator + "TestCheckOutArchive.java");
            lastCheckInCommandArgs.setInputfileTimeStamp(new Date(inputFile.lastModified()));

            if (!testArchive.checkInRevision(lastCheckInCommandArgs,
                    System.getProperty("user.dir") + File.separator + "TestCheckOutArchive.java", false)) {
                fail("Failed last check-in revision test");
            } else {
                System.out.println("Checked in revision: " + lastCheckInCommandArgs.getNewRevisionString());
                System.out.println("Passed last check-in revision test");
            }

            // Test the case where we don't change the file, but we apply a floating label at checkin.
            checkOutCommandArgs.setRevisionString(QVCSConstants.QVCS_DEFAULT_REVISION);
            checkOutCommandArgs.setOutputFileName(System.getProperty("user.dir") + File.separator + "TestCheckOutArchive.java");
            checkOutCommandArgs.setFullWorkfileName(System.getProperty("user.dir") + File.separator + "TestCheckOutArchive.java");
            checkOutCommandArgs.setShortWorkfileName("TestCheckOutArchive.java");

            if (!testArchive.checkOutRevision(checkOutCommandArgs,
                    System.getProperty("user.dir") + File.separator + "TestCheckOutArchive.java")) {
                fail("Failed last checkout test");
            } else {
                System.out.println("Checked out revision: " + checkOutCommandArgs.getRevisionString());
                System.out.println("Passed last checkout test");
            }

            lastCheckInCommandArgs = new LogFileOperationCheckInCommandArgs();
            labelString = "The is a cool floating label applied to the TRUNK.";
            lastCheckInCommandArgs.setUserName("JimVoris");
            lastCheckInCommandArgs.setLockedRevisionString(QVCSConstants.QVCS_DEFAULT_REVISION);
            lastCheckInCommandArgs.setCreateNewRevisionIfEqual(false);
            lastCheckInCommandArgs.setFullWorkfileName(System.getProperty("user.dir") + File.separator + "TestCheckOutArchive.java");
            lastCheckInCommandArgs.setCheckInComment("This is the check in comment for a new equal tip revision");
            lastCheckInCommandArgs.setApplyLabelFlag(true);
            lastCheckInCommandArgs.setLabel(labelString);
            lastCheckInCommandArgs.setFloatLabelFlag(true);
            lastInputFile = new File(System.getProperty("user.dir") + File.separator + "TestCheckOutArchive.java");
            lastCheckInCommandArgs.setInputfileTimeStamp(new Date(inputFile.lastModified()));

            if (!testArchive.checkInRevision(lastCheckInCommandArgs,
                    System.getProperty("user.dir") + File.separator + "TestCheckOutArchive.java", false)) {
                fail("Failed last check-in revision test");
            } else {
                System.out.println("Checked in revision: " + lastCheckInCommandArgs.getNewRevisionString());
                System.out.println("Passed last check-in revision test");
            }

            // Make sure the archive has no locks.
            if (testArchive.getLogFileHeaderInfo().getLogFileHeader().lockCount() != 0) {
                fail("Failed to remove lock!!");
            }

            // Test the case where we don't change the file, but we apply a label at checkin, and keep the file locked.
            checkOutCommandArgs.setRevisionString(QVCSConstants.QVCS_DEFAULT_REVISION);
            checkOutCommandArgs.setOutputFileName(System.getProperty("user.dir") + File.separator + "TestCheckOutArchive.java");
            checkOutCommandArgs.setFullWorkfileName(System.getProperty("user.dir") + File.separator + "TestCheckOutArchive.java");
            checkOutCommandArgs.setShortWorkfileName("TestCheckOutArchive.java");

            if (!testArchive.checkOutRevision(checkOutCommandArgs,
                    System.getProperty("user.dir") + File.separator + "TestCheckOutArchive.java")) {
                fail("Failed last checkout test");
            } else {
                System.out.println("Checked out revision: " + checkOutCommandArgs.getRevisionString());
                System.out.println("Passed last checkout test");
            }

            lastCheckInCommandArgs = new LogFileOperationCheckInCommandArgs();
            labelString = "The is a non-floating label applied to the TRUNK.";
            lastCheckInCommandArgs.setUserName("JimVoris");
            lastCheckInCommandArgs.setLockedRevisionString(QVCSConstants.QVCS_DEFAULT_REVISION);
            lastCheckInCommandArgs.setCreateNewRevisionIfEqual(false);
            lastCheckInCommandArgs.setLockFlag(true);
            lastCheckInCommandArgs.setFullWorkfileName(System.getProperty("user.dir") + File.separator + "TestCheckOutArchive.java");
            lastCheckInCommandArgs.setCheckInComment("This is the check in comment for a new equal tip revision");
            lastCheckInCommandArgs.setApplyLabelFlag(true);
            lastCheckInCommandArgs.setLabel(labelString);
            lastInputFile = new File(System.getProperty("user.dir") + File.separator + "TestCheckOutArchive.java");
            lastCheckInCommandArgs.setInputfileTimeStamp(new Date(inputFile.lastModified()));

            if (!testArchive.checkInRevision(lastCheckInCommandArgs,
                    System.getProperty("user.dir") + File.separator + "TestCheckOutArchive.java", false)) {
                fail("Failed last check-in revision test");
            } else {
                System.out.println("Checked in revision: " + lastCheckInCommandArgs.getNewRevisionString());
                System.out.println("Passed last check-in revision test");
            }

            // Make sure the archive has one lock.
            if (testArchive.getLogFileHeaderInfo().getLogFileHeader().lockCount() != 1) {
                fail("Failed to retain lock!!");
            }

            // Report on the labels in the archive file.
            LabelInfo[] labels = testArchive.getLogFileHeaderInfo().getLabelInfo();
            boolean foundLabel = false;
            for (int i = 0; i < labels.length; i++) {
                System.out.println("Label '" + labels[i].getLabelString() + "' points to: " + labels[i].getLabelRevisionString());
            }
            System.out.println("Last revision is now: " + testArchive.getDefaultRevisionString());
            verifyArchiveFile(testArchive);
        } catch (QVCSException e) {
            fail("Caught QVCSException: " + e.getLocalizedMessage() + " in testCheckOutCheckInRevision()");
        }
    }

    @Test
    public void testSetIsObsolete() {
        System.out.println("testSetIsObsolete");

        // This tests check-out and check-in of a new branch.  This automatically creates a new branch.
        LogFile testArchive = new LogFile(System.getProperty("user.dir") + File.separator + "TestLockArchive.kbwb");

        try {
            testArchive.setIsObsolete("JimVoris", true);

            if (!testArchive.getIsObsolete()) {
                fail("Failed to mark archive as obsolete");
            }

            testArchive.setIsObsolete("JimVoris", false);
            if (testArchive.getIsObsolete()) {
                fail("Failed to mark obsolete archive as not obsolete");
            }
            verifyArchiveFile(testArchive);
        } catch (QVCSException e) {
            fail("Caught QVCSException: " + e.getLocalizedMessage() + " in testSetIsObsolete()");
        }
    }

    /**
     * This test is specifically meant to test the scenario/defect where we create first an archive with the following revisions:<br> 1.0<br> 1.0.1.1<br> 1.0.1.1.1.1<br> 1.0.1.1.1.2<br> We then check
     * lock the trunk, checkin a new revision so that we have:<br> 1.1<br> 1.0<br> 1.0.1.1<br> 1.0.1.1.1.1<br> 1.0.1.1.1.2<br> We then lock the 1.0.1.1 revision and checkin a new revision so that we
     * <i>should</i> have:<br> 1.1<br> 1.0<br> 1.0.1.1<br> 1.0.1.1.1.1<br> 1.0.1.1.1.2<br> 1.0.1.2<br> See http://qumasoft.ipbhost.com/index.php?showtopic=3668 for the forum topic on this problem.
     */
    @Test
    public void testBranching() {
        System.out.println("testBranching");

        // Create the archive file object.
        LogFile testArchive = new LogFile(System.getProperty("user.dir") + File.separator + "BranchingTestFile.uyu");

        try {
            LogFileOperationCheckOutCommandArgs checkOutCommandArgs = new LogFileOperationCheckOutCommandArgs();
            checkOutCommandArgs.setUserName("JimVoris");
            checkOutCommandArgs.setRevisionString("1.0");
            checkOutCommandArgs.setOutputFileName(System.getProperty("user.dir") + File.separator + "BranchingTestFile.txt");
            checkOutCommandArgs.setFullWorkfileName(System.getProperty("user.dir") + File.separator + "BranchingTestFile.txt");
            checkOutCommandArgs.setShortWorkfileName("BranchingTestFile.txt");

            if (!testArchive.checkOutRevision(checkOutCommandArgs, System.getProperty("user.dir") + File.separator + "BranchingTestFile.txt")) {
                fail("Failed 1st branching test checkout");
            }

            LogFileOperationCheckInCommandArgs checkInCommandArgs = new LogFileOperationCheckInCommandArgs();
            checkInCommandArgs.setUserName("JimVoris");
            checkInCommandArgs.setLockedRevisionString("1.0");
            checkInCommandArgs.setCreateNewRevisionIfEqual(true);
            checkInCommandArgs.setFullWorkfileName(System.getProperty("user.dir") + File.separator + "TestCheckInArchive.java");
            checkInCommandArgs.setCheckInComment("This is the check in comment for revision 1.1");
            File inputFile = new File(System.getProperty("user.dir") + File.separator + "BranchingTestFile.txt");
            checkInCommandArgs.setInputfileTimeStamp(new Date(inputFile.lastModified()));

            if (!testArchive.checkInRevision(checkInCommandArgs, System.getProperty("user.dir") + File.separator + "BranchingTestFile.txt", false)) {
                fail("Failed 1st branching test check-in.");
            }
            assertEquals("Invalid revision count", 5, testArchive.getRevisionCount());
            assertEquals("Invalid value for revision 0", testArchive.getRevisionInformation().getRevisionHeader(0).getRevisionString(), "1.1");
            assertEquals("Invalid value for revision 1", testArchive.getRevisionInformation().getRevisionHeader(1).getRevisionString(), "1.0");
            assertEquals("Invalid value for revision 2", testArchive.getRevisionInformation().getRevisionHeader(2).getRevisionString(), "1.0.1.1");
            assertEquals("Invalid value for revision 3", testArchive.getRevisionInformation().getRevisionHeader(3).getRevisionString(), "1.0.1.1.1.1");
            assertEquals("Invalid value for revision 4", testArchive.getRevisionInformation().getRevisionHeader(4).getRevisionString(), "1.0.1.1.1.2");

            checkOutCommandArgs = new LogFileOperationCheckOutCommandArgs();
            checkOutCommandArgs.setUserName("JimVoris");
            checkOutCommandArgs.setRevisionString("1.0.1.1");
            checkOutCommandArgs.setOutputFileName(System.getProperty("user.dir") + File.separator + "BranchingTestFile.txt");
            checkOutCommandArgs.setFullWorkfileName(System.getProperty("user.dir") + File.separator + "BranchingTestFile.txt");
            checkOutCommandArgs.setShortWorkfileName("BranchingTestFile.txt");

            if (!testArchive.checkOutRevision(checkOutCommandArgs, System.getProperty("user.dir") + File.separator + "BranchingTestFile.txt")) {
                fail("Failed 2nd branching test checkout");
            }

            checkInCommandArgs = new LogFileOperationCheckInCommandArgs();
            checkInCommandArgs.setUserName("JimVoris");
            checkInCommandArgs.setLockedRevisionString("1.0.1.1");
            checkInCommandArgs.setCreateNewRevisionIfEqual(true);
            checkInCommandArgs.setFullWorkfileName(System.getProperty("user.dir") + File.separator + "TestCheckInArchive.java");
            checkInCommandArgs.setCheckInComment("This is the check in comment for revision 1.0.1.2");
            inputFile = new File(System.getProperty("user.dir") + File.separator + "BranchingTestFile.txt");
            checkInCommandArgs.setInputfileTimeStamp(new Date(inputFile.lastModified()));

            if (!testArchive.checkInRevision(checkInCommandArgs, System.getProperty("user.dir") + File.separator + "BranchingTestFile.txt", false)) {
                fail("Failed first check-in revision test");
            }
            assertEquals("Invalid revision count", 6, testArchive.getRevisionCount());
            assertEquals("Invalid value for revision 0", testArchive.getRevisionInformation().getRevisionHeader(0).getRevisionString(), "1.1");
            assertEquals("Invalid value for revision 1", testArchive.getRevisionInformation().getRevisionHeader(1).getRevisionString(), "1.0");
            assertEquals("Invalid value for revision 2", testArchive.getRevisionInformation().getRevisionHeader(2).getRevisionString(), "1.0.1.1");
            assertEquals("Invalid value for revision 3", testArchive.getRevisionInformation().getRevisionHeader(3).getRevisionString(), "1.0.1.1.1.1");
            assertEquals("Invalid value for revision 4", testArchive.getRevisionInformation().getRevisionHeader(4).getRevisionString(), "1.0.1.1.1.2");
            assertEquals("Invalid value for revision 5", testArchive.getRevisionInformation().getRevisionHeader(5).getRevisionString(), "1.0.1.2");

            verifyArchiveFile(testArchive);
        } catch (QVCSException e) {
            fail("Caught QVCSException: " + e.getLocalizedMessage() + " in testBranching()");
        }
    }

    private void verifyArchiveFile(LogFile logFile) throws QVCSException {
        java.io.RandomAccessFile raf = null;

        try {
            raf = new java.io.RandomAccessFile(logFile.getFullArchiveFilename(), "r");
            RevisionInformation revisionInformation = logFile.getRevisionInformation();
            int revisionCount = logFile.getRevisionCount();
            RevisionHeader revHeader = revisionInformation.getRevisionHeader(revisionCount - 1);
            long startOfRevisionData = revHeader.getRevisionDataStartPosition();
            long computedEndOfFile = startOfRevisionData + revHeader.getRevisionSize();
            long actualEndOfFile = raf.length();
            if (computedEndOfFile != actualEndOfFile) {
                System.out.println("Computed size: " + computedEndOfFile + "; Actual end of file: " + actualEndOfFile);
                throw new QVCSException("Archive file size mismatch!!");
            }
        } catch (Exception e) {
        } finally {
            if (raf != null) {
                try {
                    raf.close();
                } catch (Exception e) {
                }
            }
        }
    }
}
