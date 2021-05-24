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
package com.qumasoft.server;

import com.qumasoft.TestHelper;
import com.qumasoft.qvcslib.ArchiveAttributes;
import com.qumasoft.qvcslib.ArchiveDirManagerInterface;
import com.qumasoft.qvcslib.DirectoryCoordinate;
import com.qumasoft.qvcslib.LogfileInfo;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.QVCSException;
import com.qumasoft.qvcslib.RemoteBranchProperties;
import com.qumasoft.qvcslib.RevisionHeader;
import com.qumasoft.qvcslib.RevisionInformation;
import com.qumasoft.qvcslib.ServerResponseFactoryInterface;
import com.qumasoft.qvcslib.Utility;
import com.qumasoft.qvcslib.commandargs.CheckInCommandArgs;
import com.qumasoft.qvcslib.commandargs.CheckOutCommandArgs;
import com.qumasoft.qvcslib.commandargs.GetRevisionCommandArgs;
import com.qumasoft.qvcslib.commandargs.LabelRevisionCommandArgs;
import com.qumasoft.qvcslib.commandargs.LockRevisionCommandArgs;
import com.qumasoft.qvcslib.commandargs.SetRevisionDescriptionCommandArgs;
import com.qumasoft.qvcslib.commandargs.UnLabelRevisionCommandArgs;
import com.qumasoft.qvcslib.commandargs.UnlockRevisionCommandArgs;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Archive info for feature branch server test.
 * @author Jim Voris
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ArchiveInfoForFeatureBranchServerTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ArchiveInfoForFeatureBranchServerTest.class);

    private static ProjectBranch projectBranch = null;
    private static ProjectBranch childProjectBranch = null;
    private static RemoteBranchProperties featureBranchProperties = null;
    private static RemoteBranchProperties featureChildBranchProperties = null;
    private ArchiveInfoForFeatureBranch archiveInfoForFeatureBranch = null;
    private LogFile testArchive = null;
    private static Object serverSyncObject = null;

    /**
     * Default ctor.
     */
    public ArchiveInfoForFeatureBranchServerTest() {
    }

    /**
     * Execute once when the class is loaded.
     *
     * @throws Exception if there is a problem.
     */
    @BeforeClass
    public static void setUpClass() throws Exception {
        TestHelper.deleteBranchStore();
        initializeArchiveFiles();
        serverSyncObject = TestHelper.startServer();
        Properties projectProperties = new Properties();
        featureBranchProperties = new RemoteBranchProperties(getProjectName(), getBranchName(), projectProperties);
        featureBranchProperties.setIsReadOnlyBranchFlag(false);
        featureBranchProperties.setIsDateBasedBranchFlag(false);
        featureBranchProperties.setIsFeatureBranchFlag(true);
        featureBranchProperties.setIsOpaqueBranchFlag(false);
        featureBranchProperties.setBranchParent(QVCSConstants.QVCS_TRUNK_BRANCH);
        featureBranchProperties.setBranchDate(new Date());
        projectBranch = new ProjectBranch();
        projectBranch.setProjectName(getProjectName());
        projectBranch.setBranchName(getBranchName());
        projectBranch.setRemoteBranchProperties(featureBranchProperties);
        BranchManager.getInstance().addBranch(projectBranch, "qvcsetest");

        Properties childProjectProperties = new Properties();
        featureChildBranchProperties = new RemoteBranchProperties(getProjectName(), getChildBranchName(), childProjectProperties);
        featureChildBranchProperties.setIsReadOnlyBranchFlag(false);
        featureChildBranchProperties.setIsDateBasedBranchFlag(false);
        featureChildBranchProperties.setIsFeatureBranchFlag(true);
        featureChildBranchProperties.setIsOpaqueBranchFlag(false);
        featureChildBranchProperties.setBranchParent(getBranchName());
        featureChildBranchProperties.setBranchDate(new Date());
        childProjectBranch = new ProjectBranch();
        childProjectBranch.setProjectName(getProjectName());
        childProjectBranch.setBranchName(getChildBranchName());
        childProjectBranch.setRemoteBranchProperties(featureChildBranchProperties);
        BranchManager.getInstance().addBranch(childProjectBranch, "qvcsetest");
    }

    static private void initializeArchiveFiles() {
        File sourceFile = new File(System.getProperty("user.dir") + File.separator + "QVCSEnterpriseServer.kbwb");
        String destinationDirName = System.getProperty("user.dir")
                + File.separator
                + QVCSConstants.QVCS_PROJECTS_DIRECTORY
                + File.separator
                + getProjectName();
        File destinationDirectory = new File(destinationDirName);
        destinationDirectory.mkdirs();
        File destinationFile = new File(destinationDirName + File.separator + "QVCSEnterpriseServer.kbwb");
        try {
            ServerUtility.copyFile(sourceFile, destinationFile);
        }
        catch (IOException e) {
            LOGGER.error(e.getLocalizedMessage(), e);
        }
    }

    static private String getProjectName() {
        return TestHelper.getTestProjectName();
    }

    static private String getBranchName() {
        return "2.2.3";
    }

    static private String getChildBranchName() {
        return "2.2.3.1";
    }

    static private String getBranchFileName() {
        return "TestFeatureBranch.java";
    }

    static private String getShortWorkfileName() {
        return "QVCSEnterpriseServer.java";
    }

    /**
     * Execute once when all the tests are finished.
     *
     * @throws Exception if there is a problem.
     */
    @AfterClass
    public static void tearDownClass() throws Exception {
        TestHelper.stopServer(serverSyncObject);
        TestHelper.deleteBranchStore();
    }

    /**
     * Execute before each test.
     */
    @Before
    public void setUp() {
        // Create the archive file object.
        testArchive = new LogFile(System.getProperty("user.dir") + File.separator + "QVCSEnterpriseServer.kbwb");
        archiveInfoForFeatureBranch = new ArchiveInfoForFeatureBranch(getBranchFileName(), testArchive, featureBranchProperties);
        testArchive.addListener(archiveInfoForFeatureBranch);
    }

    /**
     * Test of getShortWorkfileName method, of class ArchiveInfoForFeatureBranch.
     */
    @Test
    public void test01GetShortWorkfileName() {
        System.out.println("getShortWorkfileName");
        ArchiveInfoForFeatureBranch instance = archiveInfoForFeatureBranch;
        String expResult = getBranchFileName();
        String result = instance.getShortWorkfileName();
        assertEquals("Unexpected shortworkfile name.", expResult, result);
    }

    /**
     * Test of getLockCount method, of class ArchiveInfoForFeatureBranch.
     */
    @Test
    public void test02GetLockCount() {
        System.out.println("getLockCount");
        ArchiveInfoForFeatureBranch instance = archiveInfoForFeatureBranch;
        int expResult = 0;
        int result = instance.getLockCount();
        assertEquals("Unexpected lock count.", expResult, result);
    }

    /**
     * Test of getLockedByString method, of class ArchiveInfoForFeatureBranch.
     */
    @Test
    public void test03GetLockedByString() {
        System.out.println("getLockedByString");
        ArchiveInfoForFeatureBranch instance = archiveInfoForFeatureBranch;
        String expResult = "";
        String result = instance.getLockedByString();
        assertEquals("Unexpected locked by string.", expResult, result);
    }

    /**
     * Test of getLastCheckInDate method, of class ArchiveInfoForFeatureBranch.
     */
    @Test
    public void test04GetLastCheckInDate() {
        System.out.println("getLastCheckInDate");
        ArchiveInfoForFeatureBranch instance = archiveInfoForFeatureBranch;
        Date expResult = testArchive.getLastCheckInDate();
        Date result = instance.getLastCheckInDate();
        assertEquals("Unexpected last checkin date", expResult, result);
    }

    /**
     * Test of getWorkfileInLocation method, of class ArchiveInfoForFeatureBranch.
     */
    @Test
    public void test05GetWorkfileInLocation() {
        System.out.println("getWorkfileInLocation");
        ArchiveInfoForFeatureBranch instance = archiveInfoForFeatureBranch;
        String expResult = "";
        String result = instance.getWorkfileInLocation();
        assertEquals("Unexpected workfile in location.", expResult, result);
    }

    /**
     * Test of getLastEditBy method, of class ArchiveInfoForFeatureBranch.
     */
    @Test
    public void test06GetLastEditBy() {
        System.out.println("getLastEditBy");
        ArchiveInfoForFeatureBranch instance = archiveInfoForFeatureBranch;
        String expResult = testArchive.getLastEditBy();
        String result = instance.getLastEditBy();
        assertEquals("Unexpected get last edit by.", expResult, result);
    }

    /**
     * Test of getDefaultRevisionDigest method, of class ArchiveInfoForFeatureBranch.
     */
    @Test
    public void test07GetDefaultRevisionDigest() {
        System.out.println("getDefaultRevisionDigest");
        ArchiveInfoForFeatureBranch instance = archiveInfoForFeatureBranch;
        byte[] expResult = testArchive.getDefaultRevisionDigest();
        byte[] result = instance.getDefaultRevisionDigest();
        assertEquals("Unexpected digest size", expResult.length, result.length);
        for (int i = 0; i < expResult.length; i++) {
            assertEquals("Unexpected digest element", expResult[i], result[i]);
        }
    }

    /**
     * Test of getAttributes method, of class ArchiveInfoForFeatureBranch.
     */
    @Test
    public void test08GetAttributes() {
        System.out.println("getAttributes");
        ArchiveInfoForFeatureBranch instance = archiveInfoForFeatureBranch;
        ArchiveAttributes expResult = testArchive.getAttributes();
        expResult.setIsCheckLock(false);
        ArchiveAttributes result = instance.getAttributes();
        assertEquals("Unexpected attributes", expResult.getAttributesAsInt(), result.getAttributesAsInt());
    }

    /**
     * Test of getLogfileInfo method, of class ArchiveInfoForFeatureBranch.
     */
    @Test
    public void test09GetLogfileInfo() {
        System.out.println("getLogfileInfo");
        ArchiveInfoForFeatureBranch instance = archiveInfoForFeatureBranch;
        LogfileInfo result = instance.getLogfileInfo();
        RevisionInformation revisionInformation = result.getRevisionInformation();
        for (int i = 0; i < instance.getRevisionCount(); i++) {
            RevisionHeader revHeader = revisionInformation.getRevisionHeader(i);
            if (revHeader.getDepth() > 0) {
                fail("Found non-trunk revision in logfile info");
            }
        }
        assertTrue(result.getLogFileHeaderInfo().getLogFileHeader().getAttributes().getIsCheckLock() == false);
    }

    /**
     * Test of getRevisionDescription method, of class ArchiveInfoForFeatureBranch.
     */
    @Test
    public void test10GetRevisionDescription() {
        System.out.println("getRevisionDescription");
        String revisionString = "1.0";
        ArchiveInfoForFeatureBranch instance = archiveInfoForFeatureBranch;
        String expResult = "Initial Revision";
        String result = instance.getRevisionDescription(revisionString);
        assertEquals("Unexpected revision description", expResult, result);
    }

    /**
     * Test of getRevisionAsByteArray method, of class ArchiveInfoForFeatureBranch.
     *
     * @throws Exception if there is a problem.
     */
    @Test
    public void test11GetRevisionAsByteArray() throws Exception {
        System.out.println("getRevisionAsByteArray");
        String revisionString = "1.0";
        // Wrap this work in a server transaction so the DirectoryContents
        // stuff will behave in a useful way...
        ServerResponseFactoryInterface bogusResponseObject = new BogusResponseObject();
        // Keep track that we're in a transaction.
        ServerTransactionManager.getInstance().clientBeginTransaction(bogusResponseObject);
        DirectoryCoordinate directoryCoordinate = new DirectoryCoordinate(getProjectName(), getBranchName(), "");
        ArchiveDirManagerInterface archiveDirManager = ArchiveDirManagerFactoryForServer.getInstance().getDirectoryManager(QVCSConstants.QVCS_SERVER_SERVER_NAME,
                directoryCoordinate, QVCSConstants.QVCS_SERVED_PROJECT_TYPE, QVCSConstants.QVCS_SERVER_USER, bogusResponseObject);
        ArchiveInfoForFeatureBranch instance = (ArchiveInfoForFeatureBranch) archiveDirManager.getArchiveInfo(getShortWorkfileName());
        byte[] result = instance.getRevisionAsByteArray(revisionString);
        ServerTransactionManager.getInstance().clientEndTransaction(bogusResponseObject);
        assertNotNull(result);
    }

    /**
     * Test of getLockedRevisionString method, of class ArchiveInfoForFeatureBranch.
     */
    @Test
    public void test12GetLockedRevisionString() {
        System.out.println("getLockedRevisionString");
        String userName = "JimVoris";
        ArchiveInfoForFeatureBranch instance = archiveInfoForFeatureBranch;
        String expResult = null;
        String result = instance.getLockedRevisionString(userName);
        assertEquals("Unexpected locked revision string", expResult, result);
    }

    /**
     * Test of getDefaultRevisionString method, of class ArchiveInfoForFeatureBranch.
     */
    @Test
    public void test13GetDefaultRevisionString() {
        System.out.println("getDefaultRevisionString");
        ArchiveInfoForFeatureBranch instance = archiveInfoForFeatureBranch;
        String expResult = "1.91";
        String result = instance.getDefaultRevisionString();
        assertEquals("Unexpected default revision string", expResult, result);
    }

    /**
     * Test of getRevision method, of class ArchiveInfoForFeatureBranch.
     *
     * @throws Exception if there is a problem.
     */
    @Test
    public void test14GetRevision() throws Exception {
        System.out.println("getRevision");
        String fetchToFileName = "GetRevisionTestFile.test";
        File fetchedFile = new File(fetchToFileName);
        GetRevisionCommandArgs commandLineArgs = new GetRevisionCommandArgs();
        commandLineArgs.setByDateFlag(false);
        commandLineArgs.setByLabelFlag(false);
        commandLineArgs.setFullWorkfileName(fetchToFileName);
        commandLineArgs.setRevisionString("1.0");
        commandLineArgs.setShortWorkfileName(fetchToFileName);
        commandLineArgs.setUserName("JimVoris");
        // Wrap this work in a server transaction so the DirectoryContents
        // stuff will behave in a useful way...
        ServerResponseFactoryInterface bogusResponseObject = new BogusResponseObject();
        // Keep track that we're in a transaction.
        ServerTransactionManager.getInstance().clientBeginTransaction(bogusResponseObject);
        DirectoryCoordinate directoryCoordinate = new DirectoryCoordinate(getProjectName(), getBranchName(), "");
        ArchiveDirManagerInterface archiveDirManager = ArchiveDirManagerFactoryForServer.getInstance().getDirectoryManager(QVCSConstants.QVCS_SERVER_SERVER_NAME,
                directoryCoordinate, QVCSConstants.QVCS_SERVED_PROJECT_TYPE, QVCSConstants.QVCS_SERVER_USER, bogusResponseObject);
        ArchiveInfoForFeatureBranch instance = (ArchiveInfoForFeatureBranch) archiveDirManager.getArchiveInfo(getShortWorkfileName());
        boolean expResult = true;
        boolean result = instance.getRevision(commandLineArgs, fetchToFileName);
        ServerTransactionManager.getInstance().clientEndTransaction(bogusResponseObject);
        assertEquals(expResult, result);
        fetchedFile.delete();
    }

    /**
     * Test of getForVisualCompare method, of class ArchiveInfoForFeatureBranch.
     *
     * @throws Exception if there is a problem.
     */
    @Test
    public void test15GetForVisualCompare() throws Exception {
        System.out.println("getForVisualCompare");
        String fetchToFileName = "GetRevisionTestFile.test";
        File fetchedFile = new File(fetchToFileName);
        GetRevisionCommandArgs commandLineArgs = new GetRevisionCommandArgs();
        commandLineArgs.setByDateFlag(false);
        commandLineArgs.setByLabelFlag(false);
        commandLineArgs.setFullWorkfileName(fetchToFileName);
        commandLineArgs.setRevisionString("1.0");
        commandLineArgs.setShortWorkfileName(fetchToFileName);
        commandLineArgs.setUserName("JimVoris");
        // Wrap this work in a server transaction so the DirectoryContents
        // stuff will behave in a useful way...
        ServerResponseFactoryInterface bogusResponseObject = new BogusResponseObject();
        // Keep track that we're in a transaction.
        ServerTransactionManager.getInstance().clientBeginTransaction(bogusResponseObject);
        DirectoryCoordinate directoryCoordinate = new DirectoryCoordinate(getProjectName(), getBranchName(), "");
        ArchiveDirManagerInterface archiveDirManager = ArchiveDirManagerFactoryForServer.getInstance().getDirectoryManager(QVCSConstants.QVCS_SERVER_SERVER_NAME,
                directoryCoordinate, QVCSConstants.QVCS_SERVED_PROJECT_TYPE, QVCSConstants.QVCS_SERVER_USER, bogusResponseObject);
        ArchiveInfoForFeatureBranch instance = (ArchiveInfoForFeatureBranch) archiveDirManager.getArchiveInfo(getShortWorkfileName());
        boolean expResult = true;
        boolean result = instance.getForVisualCompare(commandLineArgs, fetchToFileName);
        ServerTransactionManager.getInstance().clientEndTransaction(bogusResponseObject);
        assertEquals(expResult, result);
        fetchedFile.delete();
    }

    /**
     * Test of checkOutRevision method, of class ArchiveInfoForFeatureBranch.
     *
     * @throws Exception if there is a problem.
     */
    @Test
    public void test16CheckOutRevision() throws Exception {
        System.out.println("checkOutRevision");
        String fetchToFileName = "GetRevisionTestFile.test";
        File fetchedFile = new File(fetchToFileName);
        CheckOutCommandArgs commandLineArgs = new CheckOutCommandArgs();
        commandLineArgs.setCheckOutComment("Test checkout comment.");
        commandLineArgs.setFullWorkfileName(fetchToFileName);
        commandLineArgs.setOutputFileName(fetchToFileName);
        commandLineArgs.setRevisionString(QVCSConstants.QVCS_DEFAULT_REVISION);
        commandLineArgs.setShortWorkfileName(fetchToFileName);
        commandLineArgs.setUserName("JimVoris");
        // Wrap this work in a server transaction so the DirectoryContents
        // stuff will behave in a useful way...
        ServerResponseFactoryInterface bogusResponseObject = new BogusResponseObject();
        // Keep track that we're in a transaction.
        ServerTransactionManager.getInstance().clientBeginTransaction(bogusResponseObject);
        DirectoryCoordinate directoryCoordinate = new DirectoryCoordinate(getProjectName(), getBranchName(), "");
        ArchiveDirManagerInterface archiveDirManager = ArchiveDirManagerFactoryForServer.getInstance().getDirectoryManager(QVCSConstants.QVCS_SERVER_SERVER_NAME,
                directoryCoordinate, QVCSConstants.QVCS_SERVED_PROJECT_TYPE, QVCSConstants.QVCS_SERVER_USER, bogusResponseObject);
        ArchiveInfoForFeatureBranch instance = (ArchiveInfoForFeatureBranch) archiveDirManager.getArchiveInfo(getShortWorkfileName());
        boolean expResult = false;
        boolean result = instance.checkOutRevision(commandLineArgs, fetchToFileName);
        ServerTransactionManager.getInstance().clientEndTransaction(bogusResponseObject);
        assertEquals(expResult, result);
        assertEquals(instance.getLockCount(), 0);
        fetchedFile.delete();
    }

    /**
     * Test of checkInRevision method, of class ArchiveInfoForFeatureBranch.
     *
     * @throws Exception if there is a problem.
     */
    @Test
    public void test17CheckInRevision() throws Exception {
        System.out.println("checkInRevision");
        String checkInFilename = "QVCSEnterpriseServer.java";
        CheckInCommandArgs commandArgs = new CheckInCommandArgs();
        commandArgs.setApplyLabelFlag(false);
        commandArgs.setCheckInComment("A test checkin comment");
        commandArgs.setCreateNewRevisionIfEqual(true);
        commandArgs.setFullWorkfileName(checkInFilename);
        commandArgs.setProjectName(getProjectName());
        commandArgs.setShortWorkfileName(checkInFilename);
        commandArgs.setUserName("JimVoris");
        commandArgs.setInputfileTimeStamp(new Date());
        // Wrap this work in a server transaction so the DirectoryContents
        // stuff will behave in a useful way...
        ServerResponseFactoryInterface bogusResponseObject = new BogusResponseObject();
        // Keep track that we're in a transaction.
        ServerTransactionManager.getInstance().clientBeginTransaction(bogusResponseObject);
        DirectoryCoordinate directoryCoordinate = new DirectoryCoordinate(getProjectName(), getBranchName(), "");
        ArchiveDirManagerInterface archiveDirManager = ArchiveDirManagerFactoryForServer.getInstance().getDirectoryManager(QVCSConstants.QVCS_SERVER_SERVER_NAME,
                directoryCoordinate, QVCSConstants.QVCS_SERVED_PROJECT_TYPE, QVCSConstants.QVCS_SERVER_USER, bogusResponseObject);
        ArchiveInfoForFeatureBranch instance = (ArchiveInfoForFeatureBranch) archiveDirManager.getArchiveInfo(getShortWorkfileName());
        boolean expResult = true;
        int beforeRevisionCount = instance.getRevisionCount();
        assertEquals(0, instance.getLockCount());
        boolean result = instance.checkInRevision(commandArgs, checkInFilename, false);
        ServerTransactionManager.getInstance().clientEndTransaction(bogusResponseObject);
        String tipRevision = instance.getDefaultRevisionString();
        assertEquals(tipRevision, "1.91.1.1");
        assertEquals(expResult, result);
        assertEquals(0, instance.getLockCount());
        assertEquals(beforeRevisionCount + 1, instance.getRevisionCount());
    }

    /**
     * Test of lockRevision method, of class ArchiveInfoForFeatureBranch.
     *
     * @throws Exception if there is a problem.
     */
    @Test
    public void test18LockRevision() throws Exception {
        System.out.println("lockRevision");
        String lockFilename = "QVCSEnterpriseServer.java";
        LockRevisionCommandArgs commandArgs = new LockRevisionCommandArgs();
        commandArgs.setCheckOutComment("Test checkout comment dude.");
        commandArgs.setFullWorkfileName(lockFilename);
        commandArgs.setOutputFileName(lockFilename);
        commandArgs.setShortWorkfileName(lockFilename);
        commandArgs.setUserName("JimVoris");
        // Wrap this work in a server transaction so the DirectoryContents
        // stuff will behave in a useful way...
        ServerResponseFactoryInterface bogusResponseObject = new BogusResponseObject();
        // Keep track that we're in a transaction.
        ServerTransactionManager.getInstance().clientBeginTransaction(bogusResponseObject);
        DirectoryCoordinate directoryCoordinate = new DirectoryCoordinate(getProjectName(), getBranchName(), "");
        ArchiveDirManagerInterface archiveDirManager = ArchiveDirManagerFactoryForServer.getInstance().getDirectoryManager(QVCSConstants.QVCS_SERVER_SERVER_NAME,
                directoryCoordinate, QVCSConstants.QVCS_SERVED_PROJECT_TYPE, QVCSConstants.QVCS_SERVER_USER, bogusResponseObject);
        ArchiveInfoForFeatureBranch instance = (ArchiveInfoForFeatureBranch) archiveDirManager.getArchiveInfo(getShortWorkfileName());
        boolean expResult = false;
        boolean result = instance.lockRevision(commandArgs);
        ServerTransactionManager.getInstance().clientEndTransaction(bogusResponseObject);
        assertEquals(expResult, result);
        assertEquals(0, instance.getLockCount());
    }

    /**
     * Test of unlockRevision method, of class ArchiveInfoForFeatureBranch.
     *
     * @throws Exception if there is a problem.
     */
    @Test
    public void test19UnlockRevision() throws Exception {
        System.out.println("unlockRevision");
        String unlockFilename = "QVCSEnterpriseServer.java";
        UnlockRevisionCommandArgs commandArgs = new UnlockRevisionCommandArgs();
        commandArgs.setFullWorkfileName(unlockFilename);
        commandArgs.setOutputFileName(unlockFilename);
        // Wrap this work in a server transaction so the DirectoryContents
        // stuff will behave in a useful way...
        ServerResponseFactoryInterface bogusResponseObject = new BogusResponseObject();
        // Keep track that we're in a transaction.
        ServerTransactionManager.getInstance().clientBeginTransaction(bogusResponseObject);
        DirectoryCoordinate directoryCoordinate = new DirectoryCoordinate(getProjectName(), getBranchName(), "");
        ArchiveDirManagerInterface archiveDirManager = ArchiveDirManagerFactoryForServer.getInstance().getDirectoryManager(QVCSConstants.QVCS_SERVER_SERVER_NAME,
                directoryCoordinate, QVCSConstants.QVCS_SERVED_PROJECT_TYPE, QVCSConstants.QVCS_SERVER_USER, bogusResponseObject);
        ArchiveInfoForFeatureBranch instance = (ArchiveInfoForFeatureBranch) archiveDirManager.getArchiveInfo(getShortWorkfileName());
        commandArgs.setRevisionString(instance.getLockedRevisionString("JimVoris"));
        commandArgs.setShortWorkfileName(unlockFilename);
        commandArgs.setUserName("JimVoris");
        boolean expResult = false;
        boolean result = instance.unlockRevision(commandArgs);
        ServerTransactionManager.getInstance().clientEndTransaction(bogusResponseObject);
        assertEquals(expResult, result);
        assertEquals(0, instance.getLockCount());
    }

    /**
     * Test of breakLock method, of class ArchiveInfoForFeatureBranch.
     *
     * @throws Exception if there is a problem.
     */
    @Test
    public void test20BreakLock() throws Exception {
        System.out.println("breakLock");
        String breakLockFilename = "QVCSEnterpriseServer.java";
        boolean enteredException = false;
        ServerResponseFactoryInterface bogusResponseObject = new BogusResponseObject();
        try {
            UnlockRevisionCommandArgs commandArgs = new UnlockRevisionCommandArgs();
            commandArgs.setFullWorkfileName(breakLockFilename);
            commandArgs.setOutputFileName(breakLockFilename);
            commandArgs.setRevisionString(QVCSConstants.QVCS_DEFAULT_REVISION);
            commandArgs.setShortWorkfileName(breakLockFilename);
            commandArgs.setUserName("JimVoris");
            ServerTransactionManager.getInstance().clientBeginTransaction(bogusResponseObject);
            DirectoryCoordinate directoryCoordinate = new DirectoryCoordinate(getProjectName(), getBranchName(), "");
            ArchiveDirManagerInterface archiveDirManager = ArchiveDirManagerFactoryForServer.getInstance().getDirectoryManager(QVCSConstants.QVCS_SERVER_SERVER_NAME,
                    directoryCoordinate, QVCSConstants.QVCS_SERVED_PROJECT_TYPE, QVCSConstants.QVCS_SERVER_USER, bogusResponseObject);
            ArchiveInfoForFeatureBranch instance = (ArchiveInfoForFeatureBranch) archiveDirManager.getArchiveInfo(getShortWorkfileName());
            boolean result = instance.breakLock(commandArgs);
        } catch (QVCSException e) {
            enteredException = true;
        } finally {
            ServerTransactionManager.getInstance().clientEndTransaction(bogusResponseObject);
        }
        assertEquals(true, enteredException);
    }

    /**
     * Test of labelRevision method, of class ArchiveInfoForFeatureBranch.
     *
     * @throws Exception if there is a problem.
     */
    @Test
    public void test21LabelRevision() throws Exception {
        System.out.println("labelRevision");
        String testLabel = "Test Label";
        LabelRevisionCommandArgs commandArgs = new LabelRevisionCommandArgs();
        commandArgs.setLabelString(testLabel);
        commandArgs.setReuseLabelFlag(true);
        commandArgs.setShortWorkfileName(getShortWorkfileName());
        commandArgs.setUserName("JimVoris");
        // Wrap this work in a server transaction so the DirectoryContents
        // stuff will behave in a useful way...
        ServerResponseFactoryInterface bogusResponseObject = new BogusResponseObject();
        // Keep track that we're in a transaction.
        ServerTransactionManager.getInstance().clientBeginTransaction(bogusResponseObject);
        DirectoryCoordinate directoryCoordinate = new DirectoryCoordinate(getProjectName(), getBranchName(), "");
        ArchiveDirManagerInterface archiveDirManager = ArchiveDirManagerFactoryForServer.getInstance().getDirectoryManager(QVCSConstants.QVCS_SERVER_SERVER_NAME,
                directoryCoordinate, QVCSConstants.QVCS_SERVED_PROJECT_TYPE, QVCSConstants.QVCS_SERVER_USER, bogusResponseObject);
        ArchiveInfoForFeatureBranch instance = (ArchiveInfoForFeatureBranch) archiveDirManager.getArchiveInfo(getShortWorkfileName());
        boolean expResult = true;
        assertEquals(false, instance.getLogfileInfo().getLogFileHeaderInfo().hasLabel(testLabel));
        boolean result = instance.labelRevision(commandArgs);
        ServerTransactionManager.getInstance().clientEndTransaction(bogusResponseObject);
        assertEquals(expResult, result);
        assertEquals(true, instance.getLogfileInfo().getLogFileHeaderInfo().hasLabel(testLabel));
    }

    /**
     * Test of unLabelRevision method, of class ArchiveInfoForFeatureBranch.
     *
     * @throws Exception if there is a problem.
     */
    @Test
    public void test22UnLabelRevision() throws Exception {
        System.out.println("unLabelRevision");
        String testLabel = "Test Label";
        UnLabelRevisionCommandArgs commandArgs = new UnLabelRevisionCommandArgs();
        commandArgs.setLabelString(testLabel);
        commandArgs.setShortWorkfileName(getShortWorkfileName());
        commandArgs.setUserName("JimVoris");
        // Wrap this work in a server transaction so the DirectoryContents
        // stuff will behave in a useful way...
        ServerResponseFactoryInterface bogusResponseObject = new BogusResponseObject();
        // Keep track that we're in a transaction.
        ServerTransactionManager.getInstance().clientBeginTransaction(bogusResponseObject);
        DirectoryCoordinate directoryCoordinate = new DirectoryCoordinate(getProjectName(), getBranchName(), "");
        ArchiveDirManagerInterface archiveDirManager = ArchiveDirManagerFactoryForServer.getInstance().getDirectoryManager(QVCSConstants.QVCS_SERVER_SERVER_NAME,
                directoryCoordinate, QVCSConstants.QVCS_SERVED_PROJECT_TYPE, QVCSConstants.QVCS_SERVER_USER, bogusResponseObject);
        ArchiveInfoForFeatureBranch instance = (ArchiveInfoForFeatureBranch) archiveDirManager.getArchiveInfo(getShortWorkfileName());
        boolean expResult = true;
        assertEquals(true, instance.getLogfileInfo().getLogFileHeaderInfo().hasLabel(testLabel));
        boolean result = instance.unLabelRevision(commandArgs);
        ServerTransactionManager.getInstance().clientEndTransaction(bogusResponseObject);
        assertEquals(expResult, result);
        assertEquals(false, instance.getLogfileInfo().getLogFileHeaderInfo().hasLabel(testLabel));

        // Remove the same label again... this should return a false;
        ServerTransactionManager.getInstance().clientBeginTransaction(bogusResponseObject);
        result = instance.unLabelRevision(commandArgs);
        ServerTransactionManager.getInstance().clientEndTransaction(bogusResponseObject);
        expResult = false;
        assertEquals(expResult, result);
    }

    /**
     * Test of setAttributes method, of class ArchiveInfoForFeatureBranch.
     *
     * @throws Exception if there is a problem.
     */
    @Test
    public void test25SetAttributes() throws Exception {
        System.out.println("setAttributes");
        String userName = "JimVoris";
        ArchiveAttributes attributes = new ArchiveAttributes();
        ArchiveInfoForFeatureBranch instance = archiveInfoForFeatureBranch;
        boolean expResult = false;
        boolean result = instance.setAttributes(userName, attributes);
        assertEquals(expResult, result);
    }

    /**
     * Test of setCommentPrefix method, of class ArchiveInfoForFeatureBranch.
     *
     * @throws Exception if there is a problem.
     */
    @Test
    public void test26SetCommentPrefix() throws Exception {
        System.out.println("setCommentPrefix");
        String userName = "";
        String commentPrefix = "";
        ArchiveInfoForFeatureBranch instance = archiveInfoForFeatureBranch;
        boolean expResult = false;
        boolean result = instance.setCommentPrefix(userName, commentPrefix);
        assertEquals(expResult, result);
    }

    /**
     * Test of setModuleDescription method, of class ArchiveInfoForFeatureBranch.
     *
     * @throws Exception if there is a problem.
     */
    @Test
    public void test27SetModuleDescription() throws Exception {
        System.out.println("setModuleDescription");
        String userName = "";
        String moduleDescription = "";
        ArchiveInfoForFeatureBranch instance = archiveInfoForFeatureBranch;
        boolean expResult = false;
        boolean result = instance.setModuleDescription(userName, moduleDescription);
        assertEquals(expResult, result);
    }

    /**
     * Test of setRevisionDescription method, of class ArchiveInfoForFeatureBranch.
     *
     * @throws Exception if there is a problem.
     */
    @Test
    public void test28SetRevisionDescription() throws Exception {
        System.out.println("setRevisionDescription");
        SetRevisionDescriptionCommandArgs commandArgs = new SetRevisionDescriptionCommandArgs();
        String newDescription = "This is a new revision description dude";
        commandArgs.setRevisionDescription(newDescription);
        commandArgs.setShortWorkfileName(getShortWorkfileName());
        commandArgs.setUserName("JimVoris");
        // Wrap this work in a server transaction so the DirectoryContents
        // stuff will behave in a useful way...
        ServerResponseFactoryInterface bogusResponseObject = new BogusResponseObject();
        // Keep track that we're in a transaction.
        ServerTransactionManager.getInstance().clientBeginTransaction(bogusResponseObject);
        DirectoryCoordinate directoryCoordinate = new DirectoryCoordinate(getProjectName(), getBranchName(), "");
        ArchiveDirManagerInterface archiveDirManager = ArchiveDirManagerFactoryForServer.getInstance().getDirectoryManager(QVCSConstants.QVCS_SERVER_SERVER_NAME,
                directoryCoordinate, QVCSConstants.QVCS_SERVED_PROJECT_TYPE, QVCSConstants.QVCS_SERVER_USER, bogusResponseObject);
        ArchiveInfoForFeatureBranch instance = (ArchiveInfoForFeatureBranch) archiveDirManager.getArchiveInfo(getShortWorkfileName());
        commandArgs.setRevisionString(instance.getDefaultRevisionString());
        boolean expResult = true;
        boolean result = instance.setRevisionDescription(commandArgs);
        assertEquals(expResult, result);
        String alteredRevisionDescription = instance.getRevisionDescription(instance.getDefaultRevisionString());
        assertEquals(newDescription, alteredRevisionDescription);
    }

    /**
     * Test of getFullArchiveFilename method, of class ArchiveInfoForFeatureBranch.
     */
    @Test
    public void test29GetFullArchiveFilename() {
        System.out.println("getFullArchiveFilename");
        ArchiveInfoForFeatureBranch instance = archiveInfoForFeatureBranch;
        String baseDirectory = System.getProperty("user.dir");
        String expResult;
        if (Utility.isMacintosh() || Utility.isLinux()) {
            expResult = baseDirectory + "/QVCSEnterpriseServer.kbwb";
        } else {
            expResult = baseDirectory + "\\QVCSEnterpriseServer.kbwb";
        }
        String result = instance.getFullArchiveFilename();
        assertEquals(expResult, result);
    }

    /**
     * Test of getRevisionInformation method, of class ArchiveInfoForFeatureBranch.
     */
    @Test
    public void test30GetRevisionInformation() {
        System.out.println("getRevisionInformation");
        ArchiveInfoForFeatureBranch instance = archiveInfoForFeatureBranch;
        RevisionInformation result = instance.getRevisionInformation();
        assertNotNull(result);
    }

    /**
     * Test of getRevisionCount method, of class ArchiveInfoForFeatureBranch.
     */
    @Test
    public void test31GetRevisionCount() {
        System.out.println("getRevisionCount");
        ArchiveInfoForFeatureBranch instance = archiveInfoForFeatureBranch;
        int expResult = 92;
        int result = instance.getRevisionCount();
        assertEquals(expResult, result);
    }

    /**
     * Test of checkOutRevision method, of class ArchiveInfoForFeatureBranch.
     *
     * @throws Exception if there is a problem.
     */
    @Test
    public void test32CheckOutRevisionOnBranch() throws Exception {
        System.out.println("checkOutRevisionOnBranch");
        String fetchToFileName = "GetRevisionTestFile.test";
        File fetchedFile = new File(fetchToFileName);
        CheckOutCommandArgs commandLineArgs = new CheckOutCommandArgs();
        commandLineArgs.setCheckOutComment("Test checkout comment.");
        commandLineArgs.setFullWorkfileName(fetchToFileName);
        commandLineArgs.setOutputFileName(fetchToFileName);
        commandLineArgs.setShortWorkfileName(fetchToFileName);
        commandLineArgs.setUserName("JimVoris");
        // Wrap this work in a server transaction so the DirectoryContents
        // stuff will behave in a useful way...
        ServerResponseFactoryInterface bogusResponseObject = new BogusResponseObject();
        // Keep track that we're in a transaction.
        ServerTransactionManager.getInstance().clientBeginTransaction(bogusResponseObject);
        DirectoryCoordinate directoryCoordinate = new DirectoryCoordinate(getProjectName(), getBranchName(), "");
        ArchiveDirManagerInterface archiveDirManager = ArchiveDirManagerFactoryForServer.getInstance().getDirectoryManager(QVCSConstants.QVCS_SERVER_SERVER_NAME,
                directoryCoordinate, QVCSConstants.QVCS_SERVED_PROJECT_TYPE, QVCSConstants.QVCS_SERVER_USER, bogusResponseObject);
        ArchiveInfoForFeatureBranch instance = (ArchiveInfoForFeatureBranch) archiveDirManager.getArchiveInfo(getShortWorkfileName());
        commandLineArgs.setRevisionString(instance.getDefaultRevisionString());
        boolean expResult = false;
        boolean result = instance.checkOutRevision(commandLineArgs, fetchToFileName);
        ServerTransactionManager.getInstance().clientEndTransaction(bogusResponseObject);
        assertEquals(expResult, result);
        assertEquals(instance.getLockCount(), 0);
        fetchedFile.delete();
    }

    /**
     * Test of checkInRevision method, of class ArchiveInfoForFeatureBranch.
     *
     * @throws Exception if there is a problem.
     */
    @Test
    public void test33CheckInRevisionFromBranch() throws Exception {
        System.out.println("checkInRevision");
        String checkInFilename = "QVCSEnterpriseServer.java";
        CheckInCommandArgs commandArgs = new CheckInCommandArgs();
        commandArgs.setApplyLabelFlag(false);
        commandArgs.setCheckInComment("A test checkin comment");
        commandArgs.setCreateNewRevisionIfEqual(true);
        commandArgs.setFullWorkfileName(checkInFilename);
        commandArgs.setProjectName(getProjectName());
        commandArgs.setShortWorkfileName(checkInFilename);
        commandArgs.setUserName("JimVoris");
        commandArgs.setInputfileTimeStamp(new Date());
        // Wrap this work in a server transaction so the DirectoryContents
        // stuff will behave in a useful way...
        ServerResponseFactoryInterface bogusResponseObject = new BogusResponseObject();
        // Keep track that we're in a transaction.
        ServerTransactionManager.getInstance().clientBeginTransaction(bogusResponseObject);
        DirectoryCoordinate directoryCoordinate = new DirectoryCoordinate(getProjectName(), getBranchName(), "");
        ArchiveDirManagerInterface archiveDirManager = ArchiveDirManagerFactoryForServer.getInstance().getDirectoryManager(QVCSConstants.QVCS_SERVER_SERVER_NAME,
                directoryCoordinate, QVCSConstants.QVCS_SERVED_PROJECT_TYPE, QVCSConstants.QVCS_SERVER_USER, bogusResponseObject);
        ArchiveInfoForFeatureBranch instance = (ArchiveInfoForFeatureBranch) archiveDirManager.getArchiveInfo(getShortWorkfileName());
        boolean expResult = true;
        int beforeRevisionCount = instance.getRevisionCount();
        assertEquals(0, instance.getLockCount());
        boolean result = instance.checkInRevision(commandArgs, checkInFilename, false);
        ServerTransactionManager.getInstance().clientEndTransaction(bogusResponseObject);
        String tipRevision = instance.getDefaultRevisionString();
        assertTrue(0 == tipRevision.compareTo("1.91.1.2"));
        assertEquals(expResult, result);
        assertEquals(0, instance.getLockCount());
        assertEquals(beforeRevisionCount + 1, instance.getRevisionCount());
    }

    /**
     * Test of lockRevision method, of class ArchiveInfoForFeatureBranch.
     *
     * @throws Exception if there is a problem.
     */
    @Test
    public void test34LockChildBranchRevision() throws Exception {
        System.out.println("lockChildBranchRevision");
        String lockFilename = "QVCSEnterpriseServer.java";
        LockRevisionCommandArgs commandArgs = new LockRevisionCommandArgs();
        commandArgs.setCheckOutComment("Test checkout comment dude.");
        commandArgs.setFullWorkfileName(lockFilename);
        commandArgs.setOutputFileName(lockFilename);
        commandArgs.setShortWorkfileName(lockFilename);
        commandArgs.setUserName("JimVoris");
        // Wrap this work in a server transaction so the DirectoryContents
        // stuff will behave in a useful way...
        ServerResponseFactoryInterface bogusResponseObject = new BogusResponseObject();
        // Keep track that we're in a transaction.
        ServerTransactionManager.getInstance().clientBeginTransaction(bogusResponseObject);
        DirectoryCoordinate directoryCoordinate = new DirectoryCoordinate(getProjectName(), getChildBranchName(), "");
        ArchiveDirManagerInterface archiveDirManager = ArchiveDirManagerFactoryForServer.getInstance().getDirectoryManager(QVCSConstants.QVCS_SERVER_SERVER_NAME,
                directoryCoordinate, QVCSConstants.QVCS_SERVED_PROJECT_TYPE, QVCSConstants.QVCS_SERVER_USER, bogusResponseObject);
        ArchiveInfoForFeatureBranch instance = (ArchiveInfoForFeatureBranch) archiveDirManager.getArchiveInfo(getShortWorkfileName());
        boolean expResult = false;
        boolean result = instance.lockRevision(commandArgs);
        ServerTransactionManager.getInstance().clientEndTransaction(bogusResponseObject);
        assertEquals(expResult, result);
        assertEquals(0, instance.getLockCount());
    }

    /**
     * Test of unlockRevision method, of class ArchiveInfoForFeatureBranch.
     *
     * @throws Exception if there is a problem.
     */
    @Test
    public void test35UnlockChildBranchRevision() throws Exception {
        System.out.println("unlockChildBranchRevision");
        String unlockFilename = "QVCSEnterpriseServer.java";
        UnlockRevisionCommandArgs commandArgs = new UnlockRevisionCommandArgs();
        commandArgs.setFullWorkfileName(unlockFilename);
        commandArgs.setOutputFileName(unlockFilename);
        // Wrap this work in a server transaction so the DirectoryContents
        // stuff will behave in a useful way...
        ServerResponseFactoryInterface bogusResponseObject = new BogusResponseObject();
        // Keep track that we're in a transaction.
        ServerTransactionManager.getInstance().clientBeginTransaction(bogusResponseObject);
        DirectoryCoordinate directoryCoordinate = new DirectoryCoordinate(getProjectName(), getChildBranchName(), "");
        ArchiveDirManagerInterface archiveDirManager = ArchiveDirManagerFactoryForServer.getInstance().getDirectoryManager(QVCSConstants.QVCS_SERVER_SERVER_NAME,
                directoryCoordinate, QVCSConstants.QVCS_SERVED_PROJECT_TYPE, QVCSConstants.QVCS_SERVER_USER, bogusResponseObject);
        ArchiveInfoForFeatureBranch instance = (ArchiveInfoForFeatureBranch) archiveDirManager.getArchiveInfo(getShortWorkfileName());
        commandArgs.setRevisionString(instance.getLockedRevisionString("JimVoris"));
        commandArgs.setShortWorkfileName(unlockFilename);
        commandArgs.setUserName("JimVoris");
        boolean expResult = false;
        boolean result = instance.unlockRevision(commandArgs);
        ServerTransactionManager.getInstance().clientEndTransaction(bogusResponseObject);
        assertEquals(expResult, result);
        assertEquals(0, instance.getLockCount());
    }

    /**
     * Test of resolveConflictFromParentBranch method, of class ArchiveInfoForFeatureBranch.
     *
     * @throws Exception if there is a problem.
     */
    @Test
    public void test36ResolveConflictFromParentBranch() throws Exception {
        System.out.println("resolveConflictFromParentBranch");
        // Wrap this work in a server transaction so the DirectoryContents
        // stuff will behave in a useful way...
        ServerResponseFactoryInterface bogusResponseObject = new BogusResponseObject();
        // Keep track that we're in a transaction.
        ServerTransactionManager.getInstance().clientBeginTransaction(bogusResponseObject);
        DirectoryCoordinate directoryCoordinate = new DirectoryCoordinate(getProjectName(), getBranchName(), "");
        ArchiveDirManagerInterface archiveDirManager = ArchiveDirManagerFactoryForServer.getInstance().getDirectoryManager(QVCSConstants.QVCS_SERVER_SERVER_NAME,
                directoryCoordinate, QVCSConstants.QVCS_SERVED_PROJECT_TYPE, QVCSConstants.QVCS_SERVER_USER, bogusResponseObject);
        ArchiveInfoForFeatureBranch instance = (ArchiveInfoForFeatureBranch) archiveDirManager.getArchiveInfo(getShortWorkfileName());
        boolean expResult = true;
        boolean result = instance.resolveConflictFromParentBranch(TestHelper.USER_NAME, new Date());
        ServerTransactionManager.getInstance().clientEndTransaction(bogusResponseObject);
        assertEquals(expResult, result);
        assertEquals(false, instance.getCurrentLogFile().hasLabel(instance.getBranchLabel()));

        // Resolve conflict again... this should throw an exception.
        boolean caughtExpectedException = false;
        try {
            ServerTransactionManager.getInstance().clientBeginTransaction(bogusResponseObject);
            instance.resolveConflictFromParentBranch(TestHelper.USER_NAME, new Date());
            ServerTransactionManager.getInstance().clientEndTransaction(bogusResponseObject);
            fail("Did not throw expected exception.");
        } catch (QVCSException e) {
            caughtExpectedException = true;
        }
        assertTrue("Did not catch expected exception.", caughtExpectedException);
    }
}
