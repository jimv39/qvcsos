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
package com.qumasoft.qvcslib;

import com.qumasoft.TestHelper;
import com.qumasoft.server.ArchiveDigestManager;
import com.qumasoft.server.FileIDManager;
import com.qumasoft.server.LogFile;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test the LogFile class.
 *
 * @author Jim Voris
 */
public class LogFileServerTest {

    private static final Logger logger = Logger.getLogger("com.qumasoft.qvcslib");

    private static final String USER_DIR = "user.dir";
    private static final String TEST_LABEL_STRING = "This is the test label string";
    private static final String TEST_CHECKOUT_ARCHIVE_FILENAME = "TestCheckOutArchive.kbwb";
    private static final String TEST_LOCK_ARCHIVE_FILENAME = "TestLockArchive.kbwb";
    private static final String TEST_RENAMED_ARCHIVE_FILENAME = "TestLockArchive.kbwbofx";
    private static final String TEST_CREATE_ARCHIVE_FILENAME = "TestCreateArchive.kbwb";
    private static final String TEST_BRANCHING_ARCHIVE_FILENAME = "BranchingTestFile.uyu";
    private static final String TEST_LOCK_TIP_REVISION_WORKFILENAME = "TestLockArchive_TipRevision.java";
    private static final String TEST_GET_REVISION_WORKFILENAME = "TestGetRevisionArchive.java";
    private static final String TEST_CHECKOUT_WORKFILENAME = "TestCheckOutArchive.java";
    private static final String TEST_BRANCH_WORKFILENAME = "BranchingTestFile.txt";
    private static final String TEST_USER_NAME = "JimVoris";

    /**
     * Execute once before running the tests.
     *
     * @throws Exception if there is something wrong.
     */
    @BeforeClass
    public static void setUpClass() throws Exception {
        try {
            String archive1FileName = System.getProperty(USER_DIR) + File.separator + TEST_CHECKOUT_ARCHIVE_FILENAME;
            File oldArchive1 = new File(archive1FileName);
            if (oldArchive1.exists()) {
                oldArchive1.delete();
            }

            String archive2FileName = System.getProperty(USER_DIR) + File.separator + TEST_LOCK_ARCHIVE_FILENAME;
            File oldArchive2 = new File(archive2FileName);
            if (oldArchive2.exists()) {
                oldArchive2.delete();
            }

            String archive3FileName = System.getProperty(USER_DIR) + File.separator + TEST_CREATE_ARCHIVE_FILENAME;
            File oldArchive3 = new File(archive3FileName);
            if (oldArchive3.exists()) {
                oldArchive3.delete();
            }

            String archive4FileName = System.getProperty(USER_DIR) + File.separator + TEST_BRANCHING_ARCHIVE_FILENAME;
            File oldArchive4 = new File(archive4FileName);
            if (oldArchive4.exists()) {
                oldArchive4.delete();
            }

            // Create copies of the starting state archive files.
            File orgArchive1 = new File(archive1FileName + ".org");
            boolean result = TestHelper.copyFile(orgArchive1, oldArchive1);

            File orgArchive2 = new File(archive2FileName + ".org");
            result = TestHelper.copyFile(orgArchive2, oldArchive2);

            File orgArchive4 = new File(archive4FileName + ".org");
            result = TestHelper.copyFile(orgArchive4, oldArchive4);

            // Delete the tip revision file that we create.
            String tipWorkfileFileName = System.getProperty(USER_DIR) + File.separator + TEST_LOCK_TIP_REVISION_WORKFILENAME;
            File tipWorkfileFile = new File(tipWorkfileFileName);
            if (tipWorkfileFile.exists()) {
                tipWorkfileFile.delete();
            }

            // Delete the get revision file that we create.
            String revisionWorkfileFileName = System.getProperty(USER_DIR) + File.separator + TEST_GET_REVISION_WORKFILENAME;
            File revisionWorkfileFile = new File(revisionWorkfileFileName);
            if (revisionWorkfileFile.exists()) {
                revisionWorkfileFile.delete();
            }

            FileIDManager.getInstance().initialize();
            ArchiveDigestManager.getInstance().initialize(QVCSConstants.QVCS_SERVED_PROJECT_TYPE);
        }
        catch (Exception e) {
            System.out.println(Utility.expandStackTraceToString(e));
        }
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        try {
            String archive1FileName = System.getProperty(USER_DIR) + File.separator + TEST_CHECKOUT_ARCHIVE_FILENAME;
            File oldArchive1 = new File(archive1FileName);
            if (oldArchive1.exists()) {
                oldArchive1.delete();
            }

            String archive2FileName = System.getProperty(USER_DIR) + File.separator + TEST_LOCK_ARCHIVE_FILENAME;
            File oldArchive2 = new File(archive2FileName);
            if (oldArchive2.exists()) {
                oldArchive2.delete();
            }

            String archive3FileName = System.getProperty(USER_DIR) + File.separator + TEST_CREATE_ARCHIVE_FILENAME;
            File oldArchive3 = new File(archive3FileName);
            if (oldArchive3.exists()) {
                oldArchive3.delete();
            }

            String archive4FileName = System.getProperty(USER_DIR) + File.separator + TEST_BRANCHING_ARCHIVE_FILENAME;
            File oldArchive4 = new File(archive4FileName);
            if (oldArchive4.exists()) {
                oldArchive4.delete();
            }

            String workFileName = System.getProperty(USER_DIR) + File.separator + TEST_CHECKOUT_WORKFILENAME;
            File workFile = new File(workFileName);
            if (workFile.exists()) {
                workFile.delete();
            }

            String branchingWorkfileFileName = System.getProperty(USER_DIR) + File.separator + TEST_BRANCH_WORKFILENAME;
            File branchingWorkFile = new File(branchingWorkfileFileName);
            if (branchingWorkFile.exists()) {
                branchingWorkFile.delete();
            }
        }
        catch (Exception e) {
            System.out.println(Utility.expandStackTraceToString(e));
        }
        logger.log(Level.INFO, "Ending test class");
    }

    /**
     * These need to execute in order.
     */
    @Test
    public void testLogFile() {
        testConvertArchiveNameToShortWorkfileName();
        testConvertWorkfileNameToShortArchiveName();
        testGetRevision();
        testReadInformation();
        testGetShortWorkfileName();
        testGetLockedByUser();
        testGetLastCheckInDate();
        testGetLastEditBy();
        testGetWorkfileInLocation();
        testGetLockCount();
        testGetDefaultRevisionString();
        testGetHeaderInfo();
        testGetRevisionCount();
        testGetRevisionInformation();
        testLockRevision();
        testUnlockRevision();
        testLockRevisionDifferentUser();
        testUnlockRevisionDifferentUser();
        testLabelRevision();
        testUnLabelRevision();
        testSetModuleDescription();
        testSetAttributes();
        testCommentPrefix();
        testSetRevisionDescription();
        testCreateArchive();
        testRenameArchive();
        testCheckOutCheckInRevision();
        testCheckOutCheckInRevisionWithLabels();
        testSetIsObsolete();
        testBranching();
    }

    /**
     * Test of convertArchiveNameToShortWorkfileName method, of class com.qumasoft.qvcslib.LogFile.
     */
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
    public void testGetRevision() {
        System.out.println("testGetRevision");

        // Create the archive file object.
        LogFile testArchive = new LogFile(System.getProperty(USER_DIR) + File.separator + TEST_LOCK_ARCHIVE_FILENAME);

        try {
            // Create the command line object
            LogFileOperationGetRevisionCommandArgs commandArgs = new LogFileOperationGetRevisionCommandArgs();

            // Set the elements of the command args
            commandArgs.setUserName(TEST_USER_NAME);
            commandArgs.setRevisionString("1.3");
            commandArgs.setOutputFileName(System.getProperty(USER_DIR) + File.separator + TEST_GET_REVISION_WORKFILENAME);
            commandArgs.setFullWorkfileName(System.getProperty(USER_DIR) + File.separator + TEST_GET_REVISION_WORKFILENAME);
            commandArgs.setShortWorkfileName(TEST_GET_REVISION_WORKFILENAME);

            if (!testArchive.getRevision(commandArgs, System.getProperty(USER_DIR) + File.separator + TEST_GET_REVISION_WORKFILENAME)) {
                fail("Failed get revision test");
            } else {
                System.out.println("testGetRevision passed.");

                // Delete the get revision file that we create.
                String revisionWorkfileFileName = System.getProperty(USER_DIR) + File.separator + TEST_GET_REVISION_WORKFILENAME;
                File revisionWorkfileFile = new File(revisionWorkfileFileName);
                if (revisionWorkfileFile.exists()) {
                    revisionWorkfileFile.delete();
                }
            }
            verifyArchiveFile(testArchive);
        }
        catch (QVCSException e) {
            fail("Caught QVCSException: " + e.getLocalizedMessage() + " in testGetRevision()");
        }
    }

    /**
     * Test of readInformation method, of class com.qumasoft.qvcslib.LogFile.
     */
    public void testReadInformation() {
        System.out.println("testReadInformation");

        // Create the archive file object.
        LogFile testArchive = new LogFile(System.getProperty(USER_DIR) + File.separator + TEST_LOCK_ARCHIVE_FILENAME);

        try {
            boolean flag = testArchive.readInformation();
            if (!flag) {
                fail("testReadInformation failed!");
            }
            verifyArchiveFile(testArchive);
        }
        catch (Exception e) {
            fail("testReadInformation failed with exception: " + e.getClass().toString() + " : " + e.getLocalizedMessage());
        }
    }

    /**
     * Test of getShortWorkfileName method, of class com.qumasoft.qvcslib.LogFile.
     */
    public void testGetShortWorkfileName() {
        System.out.println("testGetShortWorkfileName");

        // Create the archive file object.
        LogFile testArchive = new LogFile(System.getProperty(USER_DIR) + File.separator + TEST_LOCK_ARCHIVE_FILENAME);

        try {
            String shortWorkfileName = testArchive.getShortWorkfileName();
            System.out.println("Short workfile name: " + shortWorkfileName);
        }
        catch (Exception e) {
            fail("testGetShortWorkfileName failed with exception: " + e.getClass().toString() + " : " + e.getLocalizedMessage());
        }
    }

    /**
     * Test of getLockedByUser method, of class com.qumasoft.qvcslib.LogFile.
     */
    public void testGetLockedByUser() {
        System.out.println("testGetLockedByUser");

        // Create the archive file object.
        LogFile testArchive = new LogFile(System.getProperty(USER_DIR) + File.separator + TEST_LOCK_ARCHIVE_FILENAME);

        try {
            String lockedByUser = testArchive.getLockedByUser();
            System.out.println("Locked by user: " + lockedByUser);
        }
        catch (Exception e) {
            fail("testGetLockedByUser failed with exception: " + e.getClass().toString() + " : " + e.getLocalizedMessage());
        }
    }

    /**
     * Test of getLastCheckInDate method, of class com.qumasoft.qvcslib.LogFile.
     */
    public void testGetLastCheckInDate() {
        System.out.println("testGetLastCheckInDate");

        // Create the archive file object.
        LogFile testArchive = new LogFile(System.getProperty(USER_DIR) + File.separator + TEST_LOCK_ARCHIVE_FILENAME);

        try {
            Date lastCheckInDate = testArchive.getLastCheckInDate();
            System.out.println("Last checkin date: " + lastCheckInDate.toString());
        }
        catch (Exception e) {
            fail("testGetLastCheckInDate failed with exception: " + e.getClass().toString() + " : " + e.getLocalizedMessage());
        }
    }

    /**
     * Test of getLastEditBy method, of class com.qumasoft.qvcslib.LogFile.
     */
    public void testGetLastEditBy() {
        System.out.println("testGetLastEditBy");

        // Create the archive file object.
        LogFile testArchive = new LogFile(System.getProperty(USER_DIR) + File.separator + TEST_LOCK_ARCHIVE_FILENAME);

        try {
            String lastEditBy = testArchive.getLastEditBy();
            System.out.println("Last edit by: " + lastEditBy);
        }
        catch (Exception e) {
            fail("testGetLastEditBy failed with exception: " + e.getClass().toString() + " : " + e.getLocalizedMessage());
        }
    }

    /**
     * Test of getWorkfileInLocation method, of class com.qumasoft.qvcslib.LogFile.
     */
    public void testGetWorkfileInLocation() {
        System.out.println("testGetWorkfileInLocation");

        // Create the archive file object.
        LogFile testArchive = new LogFile(System.getProperty(USER_DIR) + File.separator + TEST_LOCK_ARCHIVE_FILENAME);

        try {
            String workfileInLocation = testArchive.getWorkfileInLocation();
            System.out.println("Workfile in location: " + workfileInLocation);
        }
        catch (Exception e) {
            fail("testGetLastEditBy failed with exception: " + e.getClass().toString() + " : " + e.getLocalizedMessage());
        }
    }

    /**
     * Test of getLockCount method, of class com.qumasoft.qvcslib.LogFile.
     */
    public void testGetLockCount() {
        System.out.println("testGetLockCount");

        // Create the archive file object.
        LogFile testArchive = new LogFile(System.getProperty(USER_DIR) + File.separator + TEST_LOCK_ARCHIVE_FILENAME);

        try {
            int lockCount = testArchive.getLockCount();
            System.out.println("Lock count: " + lockCount);
        }
        catch (Exception e) {
            fail("testGetLockCount failed with exception: " + e.getClass().toString() + " : " + e.getLocalizedMessage());
        }
    }

    /**
     * Test of testGetDefaultRevisionString method, of class com.qumasoft.qvcslib.LogFile.
     */
    public void testGetDefaultRevisionString() {
        System.out.println("testGetDefaultRevisionString");

        // Create the archive file object.
        LogFile testArchive = new LogFile(System.getProperty(USER_DIR) + File.separator + TEST_LOCK_ARCHIVE_FILENAME);

        try {
            String defaultRevisionString = testArchive.getDefaultRevisionString();
            System.out.println("Default revision string: " + defaultRevisionString);
            assertEquals("Unexpected default revision string.", "1.58", defaultRevisionString);
        }
        catch (Exception e) {
            fail("testGetTipRevisionString failed with exception: " + e.getClass().toString() + " : " + e.getLocalizedMessage());
        }
    }

    /**
     * Test of getHeaderInfo method, of class com.qumasoft.qvcslib.LogFile.
     */
    public void testGetHeaderInfo() {
        System.out.println("testGetHeaderInfo");

        // Create the archive file object.
        LogFile testArchive = new LogFile(System.getProperty(USER_DIR) + File.separator + TEST_LOCK_ARCHIVE_FILENAME);

        try {
            LogFileHeaderInfo headerInfo = testArchive.getLogFileHeaderInfo();
            System.out.println("Some header info (module description): " + headerInfo.getModuleDescription());
        }
        catch (Exception e) {
            fail("testGetHeaderInfo failed with exception: " + e.getClass().toString() + " : " + e.getLocalizedMessage());
        }
    }

    /**
     * Test of getRevisionCount method, of class com.qumasoft.qvcslib.LogFile.
     */
    public void testGetRevisionCount() {
        System.out.println("testGetRevisionCount");

        // Create the archive file object.
        LogFile testArchive = new LogFile(System.getProperty(USER_DIR) + File.separator + TEST_LOCK_ARCHIVE_FILENAME);

        try {
            int revisionCount = testArchive.getRevisionCount();
            System.out.println("Revision count: " + revisionCount);
        }
        catch (Exception e) {
            fail("testGetRevisionCount failed with exception: " + e.getClass().toString() + " : " + e.getLocalizedMessage());
        }
    }

    /**
     * Test of getRevisionInformation method, of class com.qumasoft.qvcslib.LogFile.
     */
    public void testGetRevisionInformation() {
        System.out.println("testGetRevisionInformation");

        // Create the archive file object.
        LogFile testArchive = new LogFile(System.getProperty(USER_DIR) + File.separator + TEST_LOCK_ARCHIVE_FILENAME);

        try {
            RevisionInformation revisionInformation = testArchive.getRevisionInformation();
            for (int i = 0; i < testArchive.getRevisionCount(); i++) {
                RevisionHeader revisionHeader = revisionInformation.getRevisionHeader(i);
                System.out.println("Revision header: " + revisionHeader.toString());
            }
        }
        catch (Exception e) {
            fail("testGetRevisionInformation failed with exception: " + e.getClass().toString() + " : " + e.getLocalizedMessage());
        }
    }

    /**
     * Test of lockRevision method, of class com.qumasoft.qvcslib.LogFile.
     */
    public void testLockRevision() {
        System.out.println("testLockRevision");

        // Create the archive file object.
        LogFile testArchive = new LogFile(System.getProperty(USER_DIR) + File.separator + TEST_LOCK_ARCHIVE_FILENAME);

        try {
            // Create the command line object
            LogFileOperationLockRevisionCommandArgs commandArgs = new LogFileOperationLockRevisionCommandArgs();

            // Set the elements of the command args
            commandArgs.setUserName(TEST_USER_NAME);
            commandArgs.setRevisionString("1.3");
            commandArgs.setOutputFileName(System.getProperty(USER_DIR) + File.separator + "TestLockArchive.java");
            commandArgs.setFullWorkfileName(System.getProperty(USER_DIR) + File.separator + "TestLockArchive.java");
            commandArgs.setShortWorkfileName("TestLockArchive.java");

            if (!testArchive.lockRevision(commandArgs)) {
                fail("Failed lock revision test");
            } else {
                System.out.println("testLockRevision passed.");
            }
            verifyArchiveFile(testArchive);
        }
        catch (QVCSException e) {
            fail("Caught QVCSException: " + e.getLocalizedMessage() + " in testLockRevision()");
        }
    }

    /**
     * Test of unlockRevision method, of class com.qumasoft.qvcslib.LogFile.
     */
    public void testUnlockRevision() {
        System.out.println("testUnlockRevision");

        // Add your test code below by replacing the default call to fail.
        LogFile testArchive = new LogFile(System.getProperty(USER_DIR) + File.separator + TEST_LOCK_ARCHIVE_FILENAME);

        try {
            // Create the command line object
            LogFileOperationUnlockRevisionCommandArgs commandArgs = new LogFileOperationUnlockRevisionCommandArgs();

            // Set the elements of the command args
            commandArgs.setUserName(TEST_USER_NAME);
            commandArgs.setRevisionString("1.3");
            commandArgs.setOutputFileName(System.getProperty(USER_DIR) + File.separator + "TestLockArchive.java");
            commandArgs.setFullWorkfileName(System.getProperty(USER_DIR) + File.separator + "TestLockArchive.java");
            commandArgs.setShortWorkfileName("TestLockArchive.java");

            if (!testArchive.unlockRevision(commandArgs)) {
                fail("Failed unlock revision test");
            } else {
                System.out.println("testUnlockRevision passed.");
            }
            verifyArchiveFile(testArchive);
        }
        catch (QVCSException e) {
            fail("Caught QVCSException: " + e.getLocalizedMessage() + " in testUnlockRevision()");
        }
    }

    /**
     * Test of lockRevision method, of class com.qumasoft.qvcslib.LogFile.
     */
    public void testLockRevisionDifferentUser() {
        System.out.println("testLockRevisionDifferentUser");

        // Create the archive file object.
        LogFile testArchive = new LogFile(System.getProperty(USER_DIR) + File.separator + TEST_LOCK_ARCHIVE_FILENAME);

        try {
            // Create the command line object
            LogFileOperationLockRevisionCommandArgs commandArgs = new LogFileOperationLockRevisionCommandArgs();

            // Set the elements of the command args
            commandArgs.setUserName("BruceVoris");
            commandArgs.setRevisionString("1.3");
            commandArgs.setOutputFileName(System.getProperty(USER_DIR) + File.separator + "TestLockArchive.java");
            commandArgs.setFullWorkfileName(System.getProperty(USER_DIR) + File.separator + "TestLockArchive.java");
            commandArgs.setShortWorkfileName("TestLockArchive.java");

            if (!testArchive.lockRevision(commandArgs)) {
                fail("Failed lock revision test");
            } else {
                System.out.println("testLockRevisionDifferentUser passed.");
            }
            verifyArchiveFile(testArchive);
        }
        catch (QVCSException e) {
            fail("Caught QVCSException: " + e.getLocalizedMessage() + " in testLockRevisionDifferentUser()");
        }
    }

    /**
     * Test of unlockRevision method, of class com.qumasoft.qvcslib.LogFile.
     */
    public void testUnlockRevisionDifferentUser() {
        System.out.println("testUnlockRevisionDifferentUser");

        // Add your test code below by replacing the default call to fail.
        LogFile testArchive = new LogFile(System.getProperty(USER_DIR) + File.separator + TEST_LOCK_ARCHIVE_FILENAME);

        try {
            // Create the command line object
            LogFileOperationUnlockRevisionCommandArgs commandArgs = new LogFileOperationUnlockRevisionCommandArgs();

            // Set the elements of the command args
            commandArgs.setUserName("BruceVoris");
            commandArgs.setRevisionString("1.3");
            commandArgs.setOutputFileName(System.getProperty(USER_DIR) + File.separator + "TestLockArchive.java");
            commandArgs.setFullWorkfileName(System.getProperty(USER_DIR) + File.separator + "TestLockArchive.java");
            commandArgs.setShortWorkfileName("TestLockArchive.java");

            if (!testArchive.unlockRevision(commandArgs)) {
                fail("Failed unlock revision test");
            } else {
                System.out.println("testUnlockRevision passed.");
            }
            verifyArchiveFile(testArchive);
        }
        catch (QVCSException e) {
            fail("Caught QVCSException: " + e.getLocalizedMessage() + " in testUnlockRevisionDifferentUser()");
        }
    }

    /**
     * Test of labelRevision method, of class com.qumasoft.qvcslib.LogFile.
     */
    public void testLabelRevision() {
        System.out.println("testLabelRevision");

        LogFile testArchive = new LogFile(System.getProperty(USER_DIR) + File.separator + TEST_LOCK_ARCHIVE_FILENAME);

        try {
            // Create the command line object
            LogFileOperationLabelRevisionCommandArgs commandArgs = new LogFileOperationLabelRevisionCommandArgs();

            // Set the elements of the command args.
            commandArgs.setUserName(TEST_USER_NAME);
            commandArgs.setRevisionString(QVCSConstants.QVCS_DEFAULT_REVISION);
            commandArgs.setLabelString(TEST_LABEL_STRING);

            if (!testArchive.labelRevision(commandArgs)) {
                fail("Failed 1st label revision test");
            } else {
                System.out.println("testLabelRevision passed 1st test.");
            }

            // Set the elements of the command args.
            commandArgs = new LogFileOperationLabelRevisionCommandArgs();
            commandArgs.setUserName(TEST_USER_NAME);
            commandArgs.setDuplicateFlag(true);
            commandArgs.setDuplicateLabelString(TEST_LABEL_STRING);
            commandArgs.setLabelString("This is the duplicate label");

            if (!testArchive.labelRevision(commandArgs)) {
                fail("Failed 2nd label revision test");
            } else {
                System.out.println("testLabelRevision passed 2nd test.");
            }

            // Set the elements of the command args.
            commandArgs = new LogFileOperationLabelRevisionCommandArgs();
            commandArgs.setUserName(TEST_USER_NAME);
            commandArgs.setRevisionString("1.2");
            commandArgs.setLabelString(TEST_LABEL_STRING);
            commandArgs.setReuseLabelFlag(true);

            if (!testArchive.labelRevision(commandArgs)) {
                fail("Failed 3rd label revision test");
            } else {
                System.out.println("testLabelRevision passed 3rd test.");
            }

            // Set the elements of the command args.
            commandArgs = new LogFileOperationLabelRevisionCommandArgs();
            commandArgs.setUserName(TEST_USER_NAME);
            commandArgs.setRevisionString("1.2");
            commandArgs.setLabelString(TEST_LABEL_STRING);

            if (testArchive.labelRevision(commandArgs)) {
                fail("Failed 4th label revision test");
            } else {
                System.out.println("testLabelRevision passed 4th test.");
            }

            // Set the elements of the command args.
            commandArgs = new LogFileOperationLabelRevisionCommandArgs();
            commandArgs.setUserName(TEST_USER_NAME);
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
            commandArgs.setUserName(TEST_USER_NAME);
            commandArgs.setRevisionString("1.2000");
            commandArgs.setLabelString("This is a label we won't apply");

            if (testArchive.labelRevision(commandArgs)) {
                fail("Failed 6th label revision test");
            } else {
                System.out.println("testLabelRevision passed 6th test.");

            }
            // Set the elements of the command args.
            commandArgs = new LogFileOperationLabelRevisionCommandArgs();
            commandArgs.setUserName(TEST_USER_NAME);
            commandArgs.setLabelString("This is a label we won't apply");
            commandArgs.setDuplicateFlag(true);
            commandArgs.setDuplicateLabelString("This is a label that does not exist");

            if (!testArchive.labelRevision(commandArgs)) {
                fail("Failed 7th label revision test");
            } else {
                System.out.println("testLabelRevision passed 7th test.");
            }
            verifyArchiveFile(testArchive);
        }
        catch (QVCSException e) {
            fail("Caught QVCSException: " + e.getLocalizedMessage() + " in testLabelRevision()");
        }
    }

    /**
     * Test of labelRevision method, of class com.qumasoft.qvcslib.LogFile.
     */
    public void testUnLabelRevision() {
        System.out.println("testUnLabelRevision");

        LogFile testArchive = new LogFile(System.getProperty(USER_DIR) + File.separator + TEST_LOCK_ARCHIVE_FILENAME);

        try {
            // Create the command line object
            LogFileOperationUnLabelRevisionCommandArgs commandArgs = new LogFileOperationUnLabelRevisionCommandArgs();

            // Set the elements of the command args.
            commandArgs.setUserName(TEST_USER_NAME);
            commandArgs.setLabelString(TEST_LABEL_STRING);

            if (!testArchive.unLabelRevision(commandArgs)) {
                fail("Failed 1st unlabel test");
            } else {
                System.out.println("testUnLabelRevision passed 1st test.");
            }

            // Set the elements of the command args.
            commandArgs = new LogFileOperationUnLabelRevisionCommandArgs();
            commandArgs.setUserName(TEST_USER_NAME);
            commandArgs.setLabelString("This is the duplicate label");

            if (!testArchive.unLabelRevision(commandArgs)) {
                fail("Failed 2nd unlabel test");
            } else {
                System.out.println("testUnLabelRevision passed 2nd test.");
            }

            // Set the elements of the command args.
            commandArgs = new LogFileOperationUnLabelRevisionCommandArgs();
            commandArgs.setUserName(TEST_USER_NAME);
            commandArgs.setLabelString(TEST_LABEL_STRING);

            if (testArchive.unLabelRevision(commandArgs)) {
                fail("Failed 3rd unlabel revision test");
            } else {
                System.out.println("testUnLabelRevision passed 3rd test.");
            }

            // Set the elements of the command args.
            commandArgs = new LogFileOperationUnLabelRevisionCommandArgs();
            commandArgs.setUserName(TEST_USER_NAME);
            commandArgs.setLabelString("This is the floating label string");

            if (!testArchive.unLabelRevision(commandArgs)) {
                fail("Failed 5th unlabel revision test");
            } else {
                System.out.println("testUnLabelRevision passed 4th test.");
            }

            // Set the elements of the command args.
            commandArgs = new LogFileOperationUnLabelRevisionCommandArgs();
            commandArgs.setUserName(TEST_USER_NAME);
            commandArgs.setLabelString("This is a label we won't apply");

            if (testArchive.unLabelRevision(commandArgs)) {
                fail("Failed 6th label revision test");
            } else {
                System.out.println("testUnLabelRevision passed 5th test.");

            }
            verifyArchiveFile(testArchive);
        }
        catch (QVCSException e) {
            fail("Caught QVCSException: " + e.getLocalizedMessage() + " in testLabelRevision()");
        }
    }

    /**
     * Test of setModuleDescription method, of class com.qumasoft.qvcslib.LogFile.
     */
    public void testSetModuleDescription() {
        System.out.println("testSetModuleDescription");

        LogFile testArchive = new LogFile(System.getProperty(USER_DIR) + File.separator + TEST_LOCK_ARCHIVE_FILENAME);
        String beforeModuleDescription = testArchive.getModuleDescription();

        try {
            String newModuleDescription = "This is the cool new module description dude.";
            if (!testArchive.setModuleDescription(TEST_USER_NAME, newModuleDescription)) {
                fail("Failed set module description test");
            }

            if (!testArchive.getModuleDescription().equals(newModuleDescription)) {
                fail("Failed to confirm that the module description has changed.");
            }

            verifyArchiveFile(testArchive);
        }
        catch (QVCSException e) {
            fail("Caught QVCSException: " + e.getLocalizedMessage() + " in testSetRevisionDescription()");
        }
        finally {
            try {
                testArchive.setModuleDescription(TEST_USER_NAME, beforeModuleDescription);
            }
            catch (QVCSException e) {
                fail("Caught QVCSException: " + e.getLocalizedMessage() + " in testSetModuleDescription()");
            }
        }
    }

    /**
     * Test setAttributes. We need to save the attributes first so we can restore them to their initial state.
     */
    public void testSetAttributes() {
        System.out.println("testSetAttributes");
        LogFile testArchive = new LogFile(System.getProperty(USER_DIR) + File.separator + TEST_LOCK_ARCHIVE_FILENAME);
        ArchiveAttributes beforeAttributes = testArchive.getAttributes();

        try {
            ArchiveAttributes changeToAttributes = new ArchiveAttributes(beforeAttributes);
            assertTrue("Lock checking is not enabled", beforeAttributes.getIsCheckLock());
            if (changeToAttributes.getIsCheckLock()) {
                changeToAttributes.setIsCheckLock(false);
            }
            boolean result = testArchive.setAttributes(TEST_USER_NAME, changeToAttributes);
            assertTrue("Failed to set attributes", result);

            ArchiveAttributes afterAttributes = testArchive.getAttributes();
            assertTrue("Lock checking is enabled.", !afterAttributes.getIsCheckLock());

            verifyArchiveFile(testArchive);
        }
        catch (QVCSException e) {
            fail("Caught QVCSException: " + e.getLocalizedMessage() + " in testSetAttributes()");
        }
        finally {
            try {
                testArchive.setAttributes(TEST_USER_NAME, beforeAttributes);
            }
            catch (QVCSException e) {
                fail("Caught QVCSException: " + e.getLocalizedMessage() + " in testSetAttributes()");
            }
        }
    }

    /**
     * Test get/set comment prefix.
     */
    public void testCommentPrefix() {
        System.out.println("testCommentPrefix");
        LogFile testArchive = new LogFile(System.getProperty(USER_DIR) + File.separator + TEST_LOCK_ARCHIVE_FILENAME);
        String beforeCommentPrefix = testArchive.getCommentPrefix();
        try {
            String changeToCommentPrefix = "// ** - ";
            testArchive.setCommentPrefix(TEST_USER_NAME, changeToCommentPrefix);
            String afterCommentPrefix = testArchive.getCommentPrefix();
            assertEquals("Comment prefix mismatch", changeToCommentPrefix, afterCommentPrefix);
        }
        catch (QVCSException e) {
            fail("Caught QVCSException: " + e.getLocalizedMessage() + " in testCommentPrefix()");
        }
        finally {
            try {
                testArchive.setCommentPrefix(TEST_USER_NAME, beforeCommentPrefix);
            }
            catch (QVCSException e) {
                fail("Caught QVCSException: " + e.getLocalizedMessage() + " in testCommentPrefix()");
            }
        }
    }

    /**
     * Test of setRevisionDescription method, of class com.qumasoft.qvcslib.LogFile.
     */
    public void testSetRevisionDescription() {
        System.out.println("testSetRevisionDescription");

        LogFile testArchive = new LogFile(System.getProperty(USER_DIR) + File.separator + TEST_LOCK_ARCHIVE_FILENAME);

        try {
            // Create the command line object
            LogFileOperationSetRevisionDescriptionCommandArgs commandArgs = new LogFileOperationSetRevisionDescriptionCommandArgs();

            // Set the elements of the command args.
            String newDescription = "This is the new revision description for revision 1.2";
            commandArgs.setUserName(TEST_USER_NAME);
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
        }
        catch (QVCSException e) {
            fail("Caught QVCSException: " + e.getLocalizedMessage() + " in testSetRevisionDescription()");
        }
    }

    public void testCreateArchive() {
        System.out.println("testCreateArchive");

        // Add your test code below by replacing the default call to fail.
        LogFile testArchive = new LogFile(System.getProperty(USER_DIR) + File.separator + TEST_CREATE_ARCHIVE_FILENAME);
        LogFileOperationCreateArchiveCommandArgs commandArgs = new LogFileOperationCreateArchiveCommandArgs();
        commandArgs.setUserName(TEST_USER_NAME);
        commandArgs.setArchiveDescription("This is the archive description.");
        commandArgs.setWorkfileName(System.getProperty(USER_DIR) + File.separator + "TestCreateArchive.java");
        File inputFile = new File(System.getProperty(USER_DIR) + File.separator + "TestCreateArchive.java");
        commandArgs.setInputfileTimeStamp(new Date(inputFile.lastModified()));
        AbstractProjectProperties projectProperties = ProjectPropertiesFactory.getProjectPropertiesFactory().
                buildProjectProperties(TestHelper.getTestProjectName(), QVCSConstants.QVCS_SERVED_PROJECT_TYPE);
        if (!testArchive.createArchive(commandArgs, projectProperties, inputFile.getAbsolutePath())) {
            fail("Failed create archive test");
        } else {
            System.out.println("testCreateArchive passed.");
        }
        verifyArchiveFile(testArchive);
    }

    public void testRenameArchive() {
        System.out.println("testRenameArchive");
        LogFile testArchive = new LogFile(System.getProperty(USER_DIR) + File.separator + TEST_LOCK_ARCHIVE_FILENAME);
        String beforeShortWorkfileName = testArchive.getShortWorkfileName();
        int revisionCount = testArchive.getRevisionCount();
        Date now = new Date();
        try {
            assertTrue("Rename failed", testArchive.renameArchive(TEST_USER_NAME, "", beforeShortWorkfileName, beforeShortWorkfileName + "new", now));
        }
        catch (Exception e) {
            System.out.println(Utility.expandStackTraceToString(e));
        }
        finally {
            LogFile renamedArchive = new LogFile(System.getProperty(USER_DIR) + File.separator + TEST_RENAMED_ARCHIVE_FILENAME);
            int newRevisionCount = renamedArchive.getRevisionCount();
            String tipRevisionComment = renamedArchive.getRevisionDescription(renamedArchive.getDefaultRevisionString());
            renamedArchive.renameArchive(TEST_USER_NAME, "", beforeShortWorkfileName + "-new", beforeShortWorkfileName, now);
            assertTrue("Did not add rename revisions.", newRevisionCount > revisionCount);
            assertTrue("Unexpected revision comment", tipRevisionComment.startsWith(QVCSConstants.QVCS_INTERNAL_REV_COMMENT_PREFIX));
        }
    }

    /**
     * Test of checkOutRevision method, of class com.qumasoft.qvcslib.LogFile.
     */
    public void testCheckOutCheckInRevision() {
        System.out.println("testCheckOutCheckInRevision");

        // This tests check-out and check-in of a new branch.  This automatically creates a new branch.
        LogFile testArchive = new LogFile(System.getProperty(USER_DIR) + File.separator + TEST_CHECKOUT_ARCHIVE_FILENAME);

        try {
            LogFileOperationCheckOutCommandArgs checkOutCommandArgs = new LogFileOperationCheckOutCommandArgs();
            checkOutCommandArgs.setUserName(TEST_USER_NAME);
            checkOutCommandArgs.setRevisionString("1.3");
            checkOutCommandArgs.setOutputFileName(System.getProperty(USER_DIR) + File.separator + TEST_CHECKOUT_WORKFILENAME);
            checkOutCommandArgs.setFullWorkfileName(System.getProperty(USER_DIR) + File.separator + TEST_CHECKOUT_WORKFILENAME);
            checkOutCommandArgs.setShortWorkfileName(TEST_CHECKOUT_WORKFILENAME);

            if (!testArchive.checkOutRevision(checkOutCommandArgs,
                    System.getProperty(USER_DIR) + File.separator + TEST_CHECKOUT_WORKFILENAME)) {
                fail("Failed first checkout test");
            } else {
                System.out.println("Checked out revision: " + checkOutCommandArgs.getRevisionString());
                System.out.println("Passed first checkout test");
            }

            LogFileOperationCheckInCommandArgs checkInCommandArgs = new LogFileOperationCheckInCommandArgs();
            checkInCommandArgs.setUserName(TEST_USER_NAME);
            checkInCommandArgs.setLockedRevisionString("1.3");
            checkInCommandArgs.setCheckInComment("This is the check in comment for a new branch revision");
            File inputFile = new File(System.getProperty(USER_DIR) + File.separator + "TestCheckInArchive.java");
            checkInCommandArgs.setInputfileTimeStamp(new Date(inputFile.lastModified()));

            if (!testArchive.checkInRevision(checkInCommandArgs,
                    System.getProperty(USER_DIR) + File.separator + "TestCheckInArchive.java", false)) {
                fail("Failed first check-in revision test");
            } else {
                System.out.println("Checked in revision: " + checkInCommandArgs.getNewRevisionString());
                System.out.println("Passed first check-in revision test");
            }

            checkOutCommandArgs.setRevisionString("1.7");
            if (!testArchive.checkOutRevision(checkOutCommandArgs,
                    System.getProperty(USER_DIR) + File.separator + TEST_CHECKOUT_WORKFILENAME)) {
                fail("Failed 2nd checkout test");
            } else {
                System.out.println("Checked out revision: " + checkOutCommandArgs.getRevisionString());
                System.out.println("Passed 2nd checkout test");
            }

            checkInCommandArgs.setLockedRevisionString("1.7");
            checkInCommandArgs.setCheckInComment("This is the check in comment for a new tip TRUNK revision.");
            checkInCommandArgs.setFullWorkfileName(System.getProperty(USER_DIR) + File.separator + "TestCheckInArchive.java");
            File input2File = new File(System.getProperty(USER_DIR) + File.separator + "TestCheckInArchive.java");
            checkInCommandArgs.setInputfileTimeStamp(new Date(input2File.lastModified()));
            if (!testArchive.checkInRevision(checkInCommandArgs,
                    System.getProperty(USER_DIR) + File.separator + "TestCheckInArchive.java", false)) {
                fail("Failed 2nd check-in revision test");
            } else {
                System.out.println("Checked in revision: " + checkInCommandArgs.getNewRevisionString());
                System.out.println("Passed 2nd check-in revision test");
            }

            checkOutCommandArgs.setRevisionString("1.3.1.1");
            if (!testArchive.checkOutRevision(checkOutCommandArgs,
                    System.getProperty(USER_DIR) + File.separator + TEST_CHECKOUT_WORKFILENAME)) {
                fail("Failed 3rd checkout test");
            } else {
                System.out.println("Checked out revision: " + checkOutCommandArgs.getRevisionString());
            }

            checkInCommandArgs.setLockedRevisionString("1.3.1.1");
            checkInCommandArgs.setCheckInComment("This is the check in comment for a new tip branch revision.");
            File input3File = new File(System.getProperty(USER_DIR) + File.separator + "TestCheckInAgainArchive.java");
            checkInCommandArgs.setInputfileTimeStamp(new Date(input3File.lastModified()));
            if (!testArchive.checkInRevision(checkInCommandArgs,
                    System.getProperty(USER_DIR) + File.separator + "TestCheckInAgainArchive.java", false)) {
                fail("Failed 3rd check-in revision test");
            } else {
                System.out.println("Checked in revision: " + checkInCommandArgs.getNewRevisionString());
            }

            checkOutCommandArgs.setRevisionString("1.3.1.2");
            if (!testArchive.checkOutRevision(checkOutCommandArgs,
                    System.getProperty(USER_DIR) + File.separator + TEST_CHECKOUT_WORKFILENAME)) {
                fail("Failed 3rd checkout test");
            } else {
                System.out.println("Checked out revision: " + checkOutCommandArgs.getRevisionString());
            }

            checkInCommandArgs.setLockedRevisionString("1.3.1.2");
            checkInCommandArgs.setCheckInComment("This is the check in comment for a new tip branch revision.");
            checkInCommandArgs.setForceBranchFlag(true);
            checkInCommandArgs.setCreateNewRevisionIfEqual(true);
            input3File = new File(System.getProperty(USER_DIR) + File.separator + "TestCheckInAgainArchive.java");
            checkInCommandArgs.setInputfileTimeStamp(new Date(input3File.lastModified()));
            if (!testArchive.checkInRevision(checkInCommandArgs,
                    System.getProperty(USER_DIR) + File.separator + "TestCheckInAgainArchive.java", false)) {
                fail("Failed 3rd check-in revision test");
            } else {
                System.out.println("Checked in revision: " + checkInCommandArgs.getNewRevisionString());
            }

            // Test the case where we don't change the file, but we do create a new revision.
            checkOutCommandArgs.setRevisionString(QVCSConstants.QVCS_DEFAULT_REVISION);
            checkOutCommandArgs.setOutputFileName(System.getProperty(USER_DIR) + File.separator + TEST_CHECKOUT_WORKFILENAME);
            checkOutCommandArgs.setFullWorkfileName(System.getProperty(USER_DIR) + File.separator + TEST_CHECKOUT_WORKFILENAME);
            checkOutCommandArgs.setShortWorkfileName(TEST_CHECKOUT_WORKFILENAME);

            if (!testArchive.checkOutRevision(checkOutCommandArgs,
                    System.getProperty(USER_DIR) + File.separator + TEST_CHECKOUT_WORKFILENAME)) {
                fail("Failed last checkout test");
            } else {
                System.out.println("Checked out revision: " + checkOutCommandArgs.getRevisionString());
                System.out.println("Passed last checkout test");
            }

            System.out.println("Before check-in the default revision is now: " + testArchive.getDefaultRevisionString());
            LogFileOperationCheckInCommandArgs lastCheckInCommandArgs = new LogFileOperationCheckInCommandArgs();
            lastCheckInCommandArgs.setUserName(TEST_USER_NAME);
            lastCheckInCommandArgs.setLockedRevisionString(QVCSConstants.QVCS_DEFAULT_REVISION);
            lastCheckInCommandArgs.setCreateNewRevisionIfEqual(true);
            lastCheckInCommandArgs.setFullWorkfileName(System.getProperty(USER_DIR) + File.separator + TEST_CHECKOUT_WORKFILENAME);
            lastCheckInCommandArgs.setCheckInComment("This is the check in comment for a new equal tip revision");
            File lastInputFile = new File(System.getProperty(USER_DIR) + File.separator + TEST_CHECKOUT_WORKFILENAME);
            lastCheckInCommandArgs.setInputfileTimeStamp(new Date(inputFile.lastModified()));

            if (!testArchive.checkInRevision(lastCheckInCommandArgs,
                    System.getProperty(USER_DIR) + File.separator + TEST_CHECKOUT_WORKFILENAME, false)) {
                fail("Failed last check-in revision test");
            } else {
                System.out.println("Checked in revision: " + lastCheckInCommandArgs.getNewRevisionString());
                System.out.println("Passed last check-in revision test");
            }

            // Test the case where we don't change the file, but we do create a new revision.
            checkOutCommandArgs.setRevisionString(QVCSConstants.QVCS_DEFAULT_REVISION);
            checkOutCommandArgs.setOutputFileName(System.getProperty(USER_DIR) + File.separator + TEST_CHECKOUT_WORKFILENAME);
            checkOutCommandArgs.setFullWorkfileName(System.getProperty(USER_DIR) + File.separator + TEST_CHECKOUT_WORKFILENAME);
            checkOutCommandArgs.setShortWorkfileName(TEST_CHECKOUT_WORKFILENAME);

            if (!testArchive.checkOutRevision(checkOutCommandArgs,
                    System.getProperty(USER_DIR) + File.separator + TEST_CHECKOUT_WORKFILENAME)) {
                fail("Failed last checkout test");
            } else {
                System.out.println("Checked out revision: " + checkOutCommandArgs.getRevisionString());
                System.out.println("Passed last checkout test");
            }

            System.out.println("Before check-in the default revision is now: " + testArchive.getDefaultRevisionString());
            lastCheckInCommandArgs = new LogFileOperationCheckInCommandArgs();
            lastCheckInCommandArgs.setUserName(TEST_USER_NAME);
            lastCheckInCommandArgs.setLockedRevisionString(QVCSConstants.QVCS_DEFAULT_REVISION);
            lastCheckInCommandArgs.setCreateNewRevisionIfEqual(true);
            lastCheckInCommandArgs.setForceBranchFlag(true);
            lastCheckInCommandArgs.setFullWorkfileName(System.getProperty(USER_DIR) + File.separator + TEST_CHECKOUT_WORKFILENAME);
            lastCheckInCommandArgs.setCheckInComment("This is the check in comment for a forced branch off of the tip of the TRUNK.");
            lastInputFile = new File(System.getProperty(USER_DIR) + File.separator + TEST_CHECKOUT_WORKFILENAME);
            lastCheckInCommandArgs.setInputfileTimeStamp(new Date(inputFile.lastModified()));

            if (!testArchive.checkInRevision(lastCheckInCommandArgs,
                    System.getProperty(USER_DIR) + File.separator + TEST_CHECKOUT_WORKFILENAME, false)) {
                fail("Failed last check-in revision test");
            } else {
                System.out.println("Checked in revision: " + lastCheckInCommandArgs.getNewRevisionString());
                System.out.println("Passed last check-in revision test");
            }
            verifyArchiveFile(testArchive);
        }
        catch (QVCSException e) {
            fail("Caught QVCSException: " + e.getLocalizedMessage() + " in testCheckOutCheckInRevision()");
        }
    }

    public void testCheckOutCheckInRevisionWithLabels() {
        System.out.println("testCheckOutCheckInRevisionWithLabels");

        LogFile testArchive = new LogFile(System.getProperty(USER_DIR) + File.separator + TEST_CHECKOUT_ARCHIVE_FILENAME);

        try {
            LogFileOperationCheckOutCommandArgs checkOutCommandArgs = new LogFileOperationCheckOutCommandArgs();
            checkOutCommandArgs.setUserName(TEST_USER_NAME);
            checkOutCommandArgs.setRevisionString("1.10");
            checkOutCommandArgs.setOutputFileName(System.getProperty(USER_DIR) + File.separator + TEST_CHECKOUT_WORKFILENAME);
            checkOutCommandArgs.setFullWorkfileName(System.getProperty(USER_DIR) + File.separator + TEST_CHECKOUT_WORKFILENAME);
            checkOutCommandArgs.setShortWorkfileName(TEST_CHECKOUT_WORKFILENAME);

            if (!testArchive.checkOutRevision(checkOutCommandArgs,
                    System.getProperty(USER_DIR) + File.separator + TEST_CHECKOUT_WORKFILENAME)) {
                fail("Failed first checkout test");
            } else {
                System.out.println("Checked out revision: " + checkOutCommandArgs.getRevisionString());
                System.out.println("Passed first checkout test");
            }

            LogFileOperationCheckInCommandArgs checkInCommandArgs = new LogFileOperationCheckInCommandArgs();
            checkInCommandArgs.setUserName(TEST_USER_NAME);
            checkInCommandArgs.setLockedRevisionString("1.10");
            checkInCommandArgs.setCheckInComment("This is the check in comment for a new labeled branch revision");
            checkInCommandArgs.setApplyLabelFlag(true);
            checkInCommandArgs.setLabel("This label goes on the 1.10.1.1 revision");
            File inputFile = new File(System.getProperty(USER_DIR) + File.separator + "TestCheckInArchive.java");
            checkInCommandArgs.setInputfileTimeStamp(new Date(inputFile.lastModified()));

            if (!testArchive.checkInRevision(checkInCommandArgs,
                    System.getProperty(USER_DIR) + File.separator + "TestCheckInArchive.java", false)) {
                fail("Failed first check-in revision test");
            } else {
                System.out.println("Checked in revision: " + checkInCommandArgs.getNewRevisionString());
            }

            checkOutCommandArgs.setRevisionString(QVCSConstants.QVCS_DEFAULT_REVISION);
            if (!testArchive.checkOutRevision(checkOutCommandArgs,
                    System.getProperty(USER_DIR) + File.separator + TEST_CHECKOUT_WORKFILENAME)) {
                fail("Failed 2nd checkout test");
            } else {
                System.out.println("Checked out revision: " + checkOutCommandArgs.getRevisionString());
            }

            checkInCommandArgs.setLockedRevisionString(checkOutCommandArgs.getRevisionString());
            checkInCommandArgs.setCheckInComment("This is the check in comment for a new tip TRUNK revision.");
            checkInCommandArgs.setApplyLabelFlag(true);
            checkInCommandArgs.setLabel("This label goes on the new TRUNK revision.");
            checkInCommandArgs.setFullWorkfileName(System.getProperty(USER_DIR) + File.separator + "TestCheckInArchive.java");
            File input2File = new File(System.getProperty(USER_DIR) + File.separator + "TestCheckInArchive.java");
            checkInCommandArgs.setInputfileTimeStamp(new Date(input2File.lastModified()));
            if (!testArchive.checkInRevision(checkInCommandArgs,
                    System.getProperty(USER_DIR) + File.separator + "TestCheckInArchive.java", false)) {
                fail("Failed 2nd check-in revision test");
            } else {
                System.out.println("Checked in revision: " + checkInCommandArgs.getNewRevisionString());
            }

            checkOutCommandArgs.setRevisionString("1.10.1.1");
            if (!testArchive.checkOutRevision(checkOutCommandArgs,
                    System.getProperty(USER_DIR) + File.separator + TEST_CHECKOUT_WORKFILENAME)) {
                fail("Failed 3rd checkout test");
            } else {
                System.out.println("Checked out revision: " + checkOutCommandArgs.getRevisionString());
            }

            checkInCommandArgs.setLockedRevisionString("1.10.1.1");
            checkInCommandArgs.setCheckInComment("This is the check in comment for a new tip branch revision.");
            checkInCommandArgs.setApplyLabelFlag(true);
            checkInCommandArgs.setLabel("This label goes on the new branch tip revision.");
            File input3File = new File(System.getProperty(USER_DIR) + File.separator + "TestCheckInAgainArchive.java");
            checkInCommandArgs.setInputfileTimeStamp(new Date(input3File.lastModified()));
            if (!testArchive.checkInRevision(checkInCommandArgs,
                    System.getProperty(USER_DIR) + File.separator + "TestCheckInAgainArchive.java", false)) {
                fail("Failed 3rd check-in revision test");
            } else {
                System.out.println("Checked in revision: " + checkInCommandArgs.getNewRevisionString());
            }

            // Test the case where we don't change the file, but we do create a new revision.
            checkOutCommandArgs.setRevisionString(QVCSConstants.QVCS_DEFAULT_REVISION);
            checkOutCommandArgs.setOutputFileName(System.getProperty(USER_DIR) + File.separator + TEST_CHECKOUT_WORKFILENAME);
            checkOutCommandArgs.setFullWorkfileName(System.getProperty(USER_DIR) + File.separator + TEST_CHECKOUT_WORKFILENAME);
            checkOutCommandArgs.setShortWorkfileName(TEST_CHECKOUT_WORKFILENAME);

            if (!testArchive.checkOutRevision(checkOutCommandArgs,
                    System.getProperty(USER_DIR) + File.separator + TEST_CHECKOUT_WORKFILENAME)) {
                fail("Failed last checkout test");
            } else {
                System.out.println("Checked out revision: " + checkOutCommandArgs.getRevisionString());
                System.out.println("Passed last checkout test");
            }

            System.out.println("Before last check-in the default revision is now: " + testArchive.getDefaultRevisionString());
            LogFileOperationCheckInCommandArgs lastCheckInCommandArgs = new LogFileOperationCheckInCommandArgs();
            String labelString = "The is a cool label applied to this new revision";
            lastCheckInCommandArgs.setUserName(TEST_USER_NAME);
            lastCheckInCommandArgs.setLockedRevisionString(QVCSConstants.QVCS_DEFAULT_REVISION);
            lastCheckInCommandArgs.setCreateNewRevisionIfEqual(true);
            lastCheckInCommandArgs.setFullWorkfileName(System.getProperty(USER_DIR) + File.separator + TEST_CHECKOUT_WORKFILENAME);
            lastCheckInCommandArgs.setCheckInComment("This is the check in comment for a new equal tip revision");
            lastCheckInCommandArgs.setApplyLabelFlag(true);
            lastCheckInCommandArgs.setLabel(labelString);
            File lastInputFile = new File(System.getProperty(USER_DIR) + File.separator + TEST_CHECKOUT_WORKFILENAME);
            lastCheckInCommandArgs.setInputfileTimeStamp(new Date(inputFile.lastModified()));

            if (!testArchive.checkInRevision(lastCheckInCommandArgs,
                    System.getProperty(USER_DIR) + File.separator + TEST_CHECKOUT_WORKFILENAME, false)) {
                fail("Failed last check-in revision test");
            } else {
                System.out.println("Checked in revision: " + lastCheckInCommandArgs.getNewRevisionString());
                System.out.println("Passed last check-in revision test");
            }

            // Test the case where we don't change the file, but we apply a label at checkin.
            checkOutCommandArgs.setRevisionString(QVCSConstants.QVCS_DEFAULT_REVISION);
            checkOutCommandArgs.setOutputFileName(System.getProperty(USER_DIR) + File.separator + TEST_CHECKOUT_WORKFILENAME);
            checkOutCommandArgs.setFullWorkfileName(System.getProperty(USER_DIR) + File.separator + TEST_CHECKOUT_WORKFILENAME);
            checkOutCommandArgs.setShortWorkfileName(TEST_CHECKOUT_WORKFILENAME);

            if (!testArchive.checkOutRevision(checkOutCommandArgs,
                    System.getProperty(USER_DIR) + File.separator + TEST_CHECKOUT_WORKFILENAME)) {
                fail("Failed last checkout test");
            } else {
                System.out.println("Checked out revision: " + checkOutCommandArgs.getRevisionString());
                System.out.println("Passed last checkout test");
            }

            lastCheckInCommandArgs = new LogFileOperationCheckInCommandArgs();
            labelString = "The is a another cool label applied to the tip revision";
            lastCheckInCommandArgs.setUserName(TEST_USER_NAME);
            lastCheckInCommandArgs.setLockedRevisionString(QVCSConstants.QVCS_DEFAULT_REVISION);
            lastCheckInCommandArgs.setCreateNewRevisionIfEqual(false);
            lastCheckInCommandArgs.setFullWorkfileName(System.getProperty(USER_DIR) + File.separator + TEST_CHECKOUT_WORKFILENAME);
            lastCheckInCommandArgs.setCheckInComment("This is the check in comment for a new equal tip revision");
            lastCheckInCommandArgs.setApplyLabelFlag(true);
            lastCheckInCommandArgs.setLabel(labelString);
            lastInputFile = new File(System.getProperty(USER_DIR) + File.separator + TEST_CHECKOUT_WORKFILENAME);
            lastCheckInCommandArgs.setInputfileTimeStamp(new Date(inputFile.lastModified()));

            if (!testArchive.checkInRevision(lastCheckInCommandArgs,
                    System.getProperty(USER_DIR) + File.separator + TEST_CHECKOUT_WORKFILENAME, false)) {
                fail("Failed last check-in revision test");
            } else {
                System.out.println("Checked in revision: " + lastCheckInCommandArgs.getNewRevisionString());
                System.out.println("Passed last check-in revision test");
            }

            // Test the case where we don't change the file, but we apply a floating label at checkin.
            checkOutCommandArgs.setRevisionString(QVCSConstants.QVCS_DEFAULT_REVISION);
            checkOutCommandArgs.setOutputFileName(System.getProperty(USER_DIR) + File.separator + TEST_CHECKOUT_WORKFILENAME);
            checkOutCommandArgs.setFullWorkfileName(System.getProperty(USER_DIR) + File.separator + TEST_CHECKOUT_WORKFILENAME);
            checkOutCommandArgs.setShortWorkfileName(TEST_CHECKOUT_WORKFILENAME);

            if (!testArchive.checkOutRevision(checkOutCommandArgs,
                    System.getProperty(USER_DIR) + File.separator + TEST_CHECKOUT_WORKFILENAME)) {
                fail("Failed last checkout test");
            } else {
                System.out.println("Checked out revision: " + checkOutCommandArgs.getRevisionString());
                System.out.println("Passed last checkout test");
            }

            lastCheckInCommandArgs = new LogFileOperationCheckInCommandArgs();
            labelString = "The is a cool floating label applied to the TRUNK.";
            lastCheckInCommandArgs.setUserName(TEST_USER_NAME);
            lastCheckInCommandArgs.setLockedRevisionString(QVCSConstants.QVCS_DEFAULT_REVISION);
            lastCheckInCommandArgs.setCreateNewRevisionIfEqual(false);
            lastCheckInCommandArgs.setFullWorkfileName(System.getProperty(USER_DIR) + File.separator + TEST_CHECKOUT_WORKFILENAME);
            lastCheckInCommandArgs.setCheckInComment("This is the check in comment for a new equal tip revision");
            lastCheckInCommandArgs.setApplyLabelFlag(true);
            lastCheckInCommandArgs.setLabel(labelString);
            lastCheckInCommandArgs.setFloatLabelFlag(true);
            lastInputFile = new File(System.getProperty(USER_DIR) + File.separator + TEST_CHECKOUT_WORKFILENAME);
            lastCheckInCommandArgs.setInputfileTimeStamp(new Date(inputFile.lastModified()));

            if (!testArchive.checkInRevision(lastCheckInCommandArgs,
                    System.getProperty(USER_DIR) + File.separator + TEST_CHECKOUT_WORKFILENAME, false)) {
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
            checkOutCommandArgs.setOutputFileName(System.getProperty(USER_DIR) + File.separator + TEST_CHECKOUT_WORKFILENAME);
            checkOutCommandArgs.setFullWorkfileName(System.getProperty(USER_DIR) + File.separator + TEST_CHECKOUT_WORKFILENAME);
            checkOutCommandArgs.setShortWorkfileName(TEST_CHECKOUT_WORKFILENAME);

            if (!testArchive.checkOutRevision(checkOutCommandArgs,
                    System.getProperty(USER_DIR) + File.separator + TEST_CHECKOUT_WORKFILENAME)) {
                fail("Failed last checkout test");
            } else {
                System.out.println("Checked out revision: " + checkOutCommandArgs.getRevisionString());
                System.out.println("Passed last checkout test");
            }

            lastCheckInCommandArgs = new LogFileOperationCheckInCommandArgs();
            labelString = "The is a non-floating label applied to the TRUNK.";
            lastCheckInCommandArgs.setUserName(TEST_USER_NAME);
            lastCheckInCommandArgs.setLockedRevisionString(QVCSConstants.QVCS_DEFAULT_REVISION);
            lastCheckInCommandArgs.setCreateNewRevisionIfEqual(false);
            lastCheckInCommandArgs.setLockFlag(true);
            lastCheckInCommandArgs.setFullWorkfileName(System.getProperty(USER_DIR) + File.separator + TEST_CHECKOUT_WORKFILENAME);
            lastCheckInCommandArgs.setCheckInComment("This is the check in comment for a new equal tip revision");
            lastCheckInCommandArgs.setApplyLabelFlag(true);
            lastCheckInCommandArgs.setLabel(labelString);
            lastInputFile = new File(System.getProperty(USER_DIR) + File.separator + TEST_CHECKOUT_WORKFILENAME);
            lastCheckInCommandArgs.setInputfileTimeStamp(new Date(inputFile.lastModified()));

            if (!testArchive.checkInRevision(lastCheckInCommandArgs,
                    System.getProperty(USER_DIR) + File.separator + TEST_CHECKOUT_WORKFILENAME, false)) {
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
            for (LabelInfo label : labels) {
                System.out.println("Label '" + label.getLabelString() + "' points to: " + label.getLabelRevisionString());
            }
            System.out.println("Last revision is now: " + testArchive.getDefaultRevisionString());
            verifyArchiveFile(testArchive);
        }
        catch (QVCSException e) {
            fail("Caught QVCSException: " + e.getLocalizedMessage() + " in testCheckOutCheckInRevision()");
        }
    }

    public void testSetIsObsolete() {
        System.out.println("testSetIsObsolete");

        // This tests check-out and check-in of a new branch.  This automatically creates a new branch.
        LogFile testArchive = new LogFile(System.getProperty(USER_DIR) + File.separator + TEST_LOCK_ARCHIVE_FILENAME);

        try {
            testArchive.setIsObsolete(TEST_USER_NAME, true);

            if (!testArchive.getIsObsolete()) {
                fail("Failed to mark archive as obsolete");
            }

            testArchive.setIsObsolete(TEST_USER_NAME, false);
            if (testArchive.getIsObsolete()) {
                fail("Failed to mark obsolete archive as not obsolete");
            }
            verifyArchiveFile(testArchive);
        }
        catch (QVCSException e) {
            fail("Caught QVCSException: " + e.getLocalizedMessage() + " in testSetIsObsolete()");
        }
    }

    /**
     * This test is specifically meant to test the scenario/defect where we create first an archive with the following revisions:<br> 1.0<br> 1.0.1.1<br> 1.0.1.1.1.1<br>
     * 1.0.1.1.1.2<br> We then check lock the trunk, checkin a new revision so that we have:<br> 1.1<br> 1.0<br> 1.0.1.1<br> 1.0.1.1.1.1<br> 1.0.1.1.1.2<br> We then lock the
     * 1.0.1.1 revision and checkin a new revision so that we
     * <i>should</i> have:<br> 1.1<br> 1.0<br> 1.0.1.1<br> 1.0.1.1.1.1<br> 1.0.1.1.1.2<br> 1.0.1.2<br> See http://qumasoft.ipbhost.com/index.php?showtopic=3668 for the forum topic
     * on this problem.
     */
    public void testBranching() {
        System.out.println("testBranching");

        // Create the archive file object.
        LogFile testArchive = new LogFile(System.getProperty(USER_DIR) + File.separator + TEST_BRANCHING_ARCHIVE_FILENAME);

        try {
            LogFileOperationCheckOutCommandArgs checkOutCommandArgs = new LogFileOperationCheckOutCommandArgs();
            checkOutCommandArgs.setUserName(TEST_USER_NAME);
            checkOutCommandArgs.setRevisionString("1.0");
            checkOutCommandArgs.setOutputFileName(System.getProperty(USER_DIR) + File.separator + TEST_BRANCH_WORKFILENAME);
            checkOutCommandArgs.setFullWorkfileName(System.getProperty(USER_DIR) + File.separator + TEST_BRANCH_WORKFILENAME);
            checkOutCommandArgs.setShortWorkfileName(TEST_BRANCH_WORKFILENAME);

            if (!testArchive.checkOutRevision(checkOutCommandArgs, System.getProperty(USER_DIR) + File.separator + TEST_BRANCH_WORKFILENAME)) {
                fail("Failed 1st branching test checkout");
            }

            LogFileOperationCheckInCommandArgs checkInCommandArgs = new LogFileOperationCheckInCommandArgs();
            checkInCommandArgs.setUserName(TEST_USER_NAME);
            checkInCommandArgs.setLockedRevisionString("1.0");
            checkInCommandArgs.setCreateNewRevisionIfEqual(true);
            checkInCommandArgs.setFullWorkfileName(System.getProperty(USER_DIR) + File.separator + "TestCheckInArchive.java");
            checkInCommandArgs.setCheckInComment("This is the check in comment for revision 1.1");
            File inputFile = new File(System.getProperty(USER_DIR) + File.separator + TEST_BRANCH_WORKFILENAME);
            checkInCommandArgs.setInputfileTimeStamp(new Date(inputFile.lastModified()));

            if (!testArchive.checkInRevision(checkInCommandArgs, System.getProperty(USER_DIR) + File.separator + TEST_BRANCH_WORKFILENAME, false)) {
                fail("Failed 1st branching test check-in.");
            }
            assertEquals("Invalid revision count", 5, testArchive.getRevisionCount());
            assertEquals("Invalid value for revision 0", testArchive.getRevisionInformation().getRevisionHeader(0).getRevisionString(), "1.1");
            assertEquals("Invalid value for revision 1", testArchive.getRevisionInformation().getRevisionHeader(1).getRevisionString(), "1.0");
            assertEquals("Invalid value for revision 2", testArchive.getRevisionInformation().getRevisionHeader(2).getRevisionString(), "1.0.1.1");
            assertEquals("Invalid value for revision 3", testArchive.getRevisionInformation().getRevisionHeader(3).getRevisionString(), "1.0.1.1.1.1");
            assertEquals("Invalid value for revision 4", testArchive.getRevisionInformation().getRevisionHeader(4).getRevisionString(), "1.0.1.1.1.2");

            checkOutCommandArgs = new LogFileOperationCheckOutCommandArgs();
            checkOutCommandArgs.setUserName(TEST_USER_NAME);
            checkOutCommandArgs.setRevisionString("1.0.1.1");
            checkOutCommandArgs.setOutputFileName(System.getProperty(USER_DIR) + File.separator + TEST_BRANCH_WORKFILENAME);
            checkOutCommandArgs.setFullWorkfileName(System.getProperty(USER_DIR) + File.separator + TEST_BRANCH_WORKFILENAME);
            checkOutCommandArgs.setShortWorkfileName(TEST_BRANCH_WORKFILENAME);

            if (!testArchive.checkOutRevision(checkOutCommandArgs, System.getProperty(USER_DIR) + File.separator + TEST_BRANCH_WORKFILENAME)) {
                fail("Failed 2nd branching test checkout");
            }

            checkInCommandArgs = new LogFileOperationCheckInCommandArgs();
            checkInCommandArgs.setUserName(TEST_USER_NAME);
            checkInCommandArgs.setLockedRevisionString("1.0.1.1");
            checkInCommandArgs.setCreateNewRevisionIfEqual(true);
            checkInCommandArgs.setFullWorkfileName(System.getProperty(USER_DIR) + File.separator + "TestCheckInArchive.java");
            checkInCommandArgs.setCheckInComment("This is the check in comment for revision 1.0.1.2");
            inputFile = new File(System.getProperty(USER_DIR) + File.separator + TEST_BRANCH_WORKFILENAME);
            checkInCommandArgs.setInputfileTimeStamp(new Date(inputFile.lastModified()));

            if (!testArchive.checkInRevision(checkInCommandArgs, System.getProperty(USER_DIR) + File.separator + TEST_BRANCH_WORKFILENAME, false)) {
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
        }
        catch (QVCSException e) {
            fail("Caught QVCSException: " + e.getLocalizedMessage() + " in testBranching()");
        }
    }

    private void verifyArchiveFile(LogFile logFile) {
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
        }
        catch (IOException | QVCSException e) {
        }
        finally {
            if (raf != null) {
                try {
                    raf.close();
                }
                catch (IOException e) {
                }
            }
        }
    }
}
