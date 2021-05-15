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
import com.qumasoft.qvcslib.ArchiveDirManagerInterface;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.QVCSException;
import com.qumasoft.qvcslib.RemoteBranchProperties;
import com.qumasoft.qvcslib.ServerResponseFactoryInterface;
import com.qumasoft.qvcslib.Utility;
import com.qumasoft.server.dataaccess.impl.DAOTestHelper;
import java.io.File;
import java.sql.SQLException;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test the Directory Contents Manager.
 *
 * @author Jim Voris
 */
public class DirectoryContentsManagerTest {

    private static final String DERBY_TEST_DIRECTORY_SUFFIX = "directoryContentsManagerTest";
    private static int testProjectId = -1;
    private static int testTrunkBranchId = -1;
    private static ProjectBranch featureBranchProjectBranch = null;
    private static ProjectBranch opaqueBranchProjectBranch = null;
    private static ProjectBranch dateBasedProjectBranch = null;
    private static RemoteBranchProperties featureBranchProperties = null;
    private static RemoteBranchProperties opaqueBranchProperties = null;
    private static RemoteBranchProperties dateBasedBranchProperties = null;
    private ServerResponseFactoryInterface bogusResponseObject = null;
    private DirectoryContentsManager directoryContentsManager = null;
    private static final String ROOT_DIRECTORY_APPENDED_PATH = "";
    private static final String SUB_DIRECTORY_1_APPENDED_PATH = TestHelper.SUBPROJECT_APPENDED_PATH;
    private static final String ADDED_SUB_DIRECTORY_APPENDED_PATH = "addedSubdirectory";

    /**
     * DirectoryContentsManager unit test class.
     */
    public DirectoryContentsManagerTest() {
    }

    /**
     * Set up stuff once for these tests.
     *
     * @throws java.lang.Exception if anything goes wrong.
     */
    @BeforeClass
    public static void setUpClass() throws Exception {
        System.out.println("@BeforeClass setUpClass start.");
        TestHelper.stopServerImmediately(null);
        String userDirectoryString = System.getProperty("user.dir");
        try {
            File userDirectory = new File(userDirectoryString);
            String canonicalUserDirectory = userDirectory.getCanonicalPath();
            System.getProperties().setProperty("user.dir", canonicalUserDirectory);
        }
        finally {
        }
        TestHelper.emptyDerbyTestDirectory(TestHelper.buildTestDirectoryName(DERBY_TEST_DIRECTORY_SUFFIX));
        TestHelper.deleteBranchStore();
        TestHelper.removeArchiveFiles();
        TestHelper.initProjectProperties();
        BranchManager.getInstance().initialize();
        DirectoryIDManager.getInstance().resetStore();
        DirectoryIDManager.getInstance().initialize();
        FileIDManager.getInstance().resetStore();
        FileIDManager.getInstance().initialize();
        DirectoryIDDictionary.getInstance().resetStore();
        DirectoryIDDictionary.getInstance().initialize();
        FileIDDictionary.getInstance().resetStore();
        FileIDDictionary.getInstance().initialize();
        QVCSEnterpriseServer.setDatabaseManager(DatabaseManager.getInstance());
        DatabaseManager.getInstance().setDerbyHomeDirectory(TestHelper.buildTestDirectoryName(DERBY_TEST_DIRECTORY_SUFFIX));
        QVCSEnterpriseServer.getDatabaseManager().initializeDatabase();
        testProjectId = DAOTestHelper.createTestProject("qvcse");
        testTrunkBranchId = DAOTestHelper.createTrunkBranch(testProjectId, "qvcse");
        System.out.println("Test trunk branch id: " + testTrunkBranchId);
        DAOTestHelper.populateDbWithTestFiles();
        initializeOpaqueBranch();
        initializeFeatureBranch();
        initializeDateBasedBranch();
        System.out.println("@BeforeClass setUpClass complete.");
    }

    /**
     * Tear down the class level stuff.
     *
     * @throws java.lang.Exception if anything goes wrong.
     */
    @AfterClass
    public static void tearDownClass() throws Exception {
        System.out.println("@AfterClass tearDownClass start.");
        DatabaseManager.getInstance().shutdownDatabase();
        System.out.println("@AfterClass tearDownClass complete.");
    }

    /**
     * Set up before each test.
     */
    public void setUp() {
        System.out.println("@Before setUp start.");
        directoryContentsManager = DirectoryContentsManagerFactory.getInstance().getDirectoryContentsManager(TestHelper.getTestProjectName());
        bogusResponseObject = new BogusResponseObject();
        System.out.println("@Before setUp complete.");
    }

    /**
     * Tear down after each test.
     */
    @After
    public void tearDown() {
    }

    static private void initializeDateBasedBranch() throws QVCSException {
        Properties projectProperties = new Properties();
        dateBasedBranchProperties = new RemoteBranchProperties(getProjectName(), getDateBasedBranchName(), projectProperties);
        dateBasedBranchProperties.setIsReadOnlyBranchFlag(true);
        dateBasedBranchProperties.setIsDateBasedBranchFlag(true);
        dateBasedBranchProperties.setIsFeatureBranchFlag(false);
        dateBasedBranchProperties.setIsOpaqueBranchFlag(false);
        dateBasedBranchProperties.setBranchParent(QVCSConstants.QVCS_TRUNK_BRANCH);
        dateBasedBranchProperties.setDateBaseDate(new Date());
        dateBasedProjectBranch = new ProjectBranch();
        dateBasedProjectBranch.setProjectName(getProjectName());
        dateBasedProjectBranch.setBranchName(getDateBasedBranchName());
        dateBasedProjectBranch.setRemoteBranchProperties(dateBasedBranchProperties);
        BranchManager.getInstance().addBranch(dateBasedProjectBranch);
    }

    static private void initializeOpaqueBranch() throws QVCSException {
        Properties projectProperties = new Properties();
        opaqueBranchProperties = new RemoteBranchProperties(getProjectName(), getFeatureBranchName(), projectProperties);
        opaqueBranchProperties.setIsReadOnlyBranchFlag(false);
        opaqueBranchProperties.setIsDateBasedBranchFlag(false);
        opaqueBranchProperties.setIsFeatureBranchFlag(false);
        opaqueBranchProperties.setIsOpaqueBranchFlag(true);
        opaqueBranchProperties.setBranchParent(QVCSConstants.QVCS_TRUNK_BRANCH);
        opaqueBranchProperties.setBranchDate(new Date());
        opaqueBranchProjectBranch = new ProjectBranch();
        opaqueBranchProjectBranch.setProjectName(getProjectName());
        opaqueBranchProjectBranch.setBranchName(getOpaqueBranchName());
        opaqueBranchProjectBranch.setRemoteBranchProperties(opaqueBranchProperties);
        BranchManager.getInstance().addBranch(opaqueBranchProjectBranch);
    }

    static private void initializeFeatureBranch() throws QVCSException {
        Properties projectProperties = new Properties();
        featureBranchProperties = new RemoteBranchProperties(getProjectName(), getFeatureBranchName(), projectProperties);
        featureBranchProperties.setIsReadOnlyBranchFlag(false);
        featureBranchProperties.setIsDateBasedBranchFlag(false);
        featureBranchProperties.setIsFeatureBranchFlag(true);
        featureBranchProperties.setIsOpaqueBranchFlag(false);
        featureBranchProperties.setBranchParent(QVCSConstants.QVCS_TRUNK_BRANCH);
        featureBranchProperties.setBranchDate(new Date());
        featureBranchProjectBranch = new ProjectBranch();
        featureBranchProjectBranch.setProjectName(getProjectName());
        featureBranchProjectBranch.setBranchName(getFeatureBranchName());
        featureBranchProjectBranch.setRemoteBranchProperties(featureBranchProperties);
        BranchManager.getInstance().addBranch(featureBranchProjectBranch);
    }

    static private String getProjectName() {
        return TestHelper.getTestProjectName();
    }

    static private String getDateBasedBranchName() {
        return "now";
    }

    static private String getOpaqueBranchName() {
        return "O-2.2.1";
    }

    static private String getFeatureBranchName() {
        return "F-2.2.2";
    }

    /**
     * Test directory contents manager. We put the tests into this single test method so we can
     * control the order in which the tests are run.
     * @throws Exception if there is a problem.
     */
    @Test
    public void testDirectoryContentsManager() throws Exception {
        testGetProjectName();
        testAddFileToTrunk();
        testAddFileToOpaqueBranch();
        testAddFileToOpaqueBranchBadBranchName();
        testAddFileToFeatureBranch();
        testAddFileToFeatureBranchBadBranchName();
        testRenameFileOnTrunk();
        testRenameFileOnOpaqueBranch();
        testRenameFileOnOpaqueBranchBadBranchName();
        testRenameFileOnFeatureBranch();
        testRenameFileOnFeatureBranchBadBranchName();
        testMoveFileOnTrunk();
        testMoveFileOnOpaqueBranch();
        testMoveFileOnOpaqueBranchBadBranchName();
        testMoveFileOnFeatureBranch();
        testMoveFileOnFeatureBranchBadBranchName();
        testDeleteFileOnTrunk();
        testDeleteFileOnOpaqueBranch();
        testDeleteFileOnOpaqueBranchBadBranchName();
        testDeleteFileOnFeatureBranch();
        testDeleteFileOnFeatureBranchBadBranchName();
        testMoveFileFromFeatureBranchCemetery();
        testAddDirectoryOnTrunk();
        testAddDirectoryOnOpaqueBranch();
        testAddDirectoryOnFeatureBranch();
        testDeleteDirectoryOnTrunk();
        testDeleteDirectoryOnOpaqueBranch();
        testDeleteDirectoryOnFeatureBranch();
        testGetDirectoryIDCollectionForDateBasedBranch();
        testGetDirectoryIDCollectionForFeatureBranch();
        testGetDirectoryIDCollectionForOpaqueBranch();
        testGetPriority();
    }

    /**
     * Test of getProjectName method, of class DirectoryContentsManager.
     */
    public void testGetProjectName() {
        setUp();
        ServerTransactionManager.getInstance().flushClientTransaction(bogusResponseObject);
        ServerTransactionManager.getInstance().clientBeginTransaction(bogusResponseObject);
        DirectoryContentsManager instance = directoryContentsManager;
        String expResult = TestHelper.getTestProjectName();
        String result = instance.getProjectName();
        assertEquals(expResult, result);
        ServerTransactionManager.getInstance().clientEndTransaction(bogusResponseObject);
    }

    /**
     * Test of addFile method, of class DirectoryContentsManager. This test adds a file to the Trunk.
     *
     * @throws Exception if there is a problem.
     */
    public void testAddFileToTrunk() throws Exception {
        setUp();
        ServerTransactionManager.getInstance().flushClientTransaction(bogusResponseObject);
        ServerTransactionManager.getInstance().clientBeginTransaction(bogusResponseObject);
        int directoryID = 1;
        int fileID = 1000;
        String shortWorkfileName = "testAddFile.java";
        DirectoryContentsManager instance = directoryContentsManager;
        instance.addFileToTrunk(directoryID, fileID, shortWorkfileName, bogusResponseObject);
        DirectoryContents directoryContents = instance.getDirectoryContentsForTrunk(ROOT_DIRECTORY_APPENDED_PATH, directoryID, bogusResponseObject);
        String fileName = directoryContents.getFiles().get(fileID);
        ServerTransactionManager.getInstance().clientEndTransaction(bogusResponseObject);
        assertEquals(shortWorkfileName, fileName);
    }

    /**
     * Test of addFile method, of class DirectoryContentsManager. This test adds a file to an opaque branch.
     *
     * @throws Exception if there is a problem.
     */
    public void testAddFileToOpaqueBranch() throws Exception {
        setUp();
        ServerTransactionManager.getInstance().flushClientTransaction(bogusResponseObject);
        ServerTransactionManager.getInstance().clientBeginTransaction(bogusResponseObject);
        String branchName = getOpaqueBranchName();
        int directoryID = 1;
        int fileID = 1001;
        String shortWorkfileName = "testAddFileToOpaqueBranch.java";
        DirectoryContentsManager instance = directoryContentsManager;
        instance.addFileToOpaqueBranch(branchName, directoryID, fileID, shortWorkfileName, bogusResponseObject);
        DirectoryContents directoryContents = instance.getDirectoryContentsForOpaqueBranch(opaqueBranchProjectBranch, ROOT_DIRECTORY_APPENDED_PATH, directoryID, bogusResponseObject);
        String fileName = directoryContents.getFiles().get(fileID);
        ServerTransactionManager.getInstance().clientEndTransaction(bogusResponseObject);
        assertEquals(shortWorkfileName, fileName);

        // Go back and look again... this one should actually have to go to disk to find the directory contents object.
        ServerTransactionManager.getInstance().clientBeginTransaction(bogusResponseObject);
        directoryContents = instance.getDirectoryContentsForOpaqueBranch(opaqueBranchProjectBranch, ROOT_DIRECTORY_APPENDED_PATH, directoryID, bogusResponseObject);
        fileName = directoryContents.getFiles().get(fileID);
        ServerTransactionManager.getInstance().clientEndTransaction(bogusResponseObject);
        assertEquals(shortWorkfileName, fileName);
    }

    /**
     * Test of addFile method, of class DirectoryContentsManager. This test adds a file to an opaque branch.
     *
     * @throws Exception if there is a problem.
     */
    public void testAddFileToOpaqueBranchBadBranchName() throws Exception {
        setUp();
        ServerTransactionManager.getInstance().flushClientTransaction(bogusResponseObject);
        ServerTransactionManager.getInstance().clientBeginTransaction(bogusResponseObject);
        String branchName = "Bogus Opaque Branch";
        int directoryID = 1;
        int fileID = 1001;
        String shortWorkfileName = "testAddFileToOpaqueBranch.java";
        DirectoryContentsManager instance = directoryContentsManager;
        boolean caughtExpectedException = false;
        try {
            instance.addFileToOpaqueBranch(branchName, directoryID, fileID, shortWorkfileName, bogusResponseObject);
        }
        catch (QVCSException e) {
            caughtExpectedException = true;
        }
        assertTrue(caughtExpectedException);
    }

    /**
     * Test of addFile method, of class DirectoryContentsManager. This test adds a file to a feature branch.
     *
     * @throws Exception if there is a problem.
     */
    public void testAddFileToFeatureBranch() throws Exception {
        setUp();
        ServerTransactionManager.getInstance().flushClientTransaction(bogusResponseObject);
        ServerTransactionManager.getInstance().clientBeginTransaction(bogusResponseObject);
        String branchName = getFeatureBranchName();
        int directoryID = 1;
        int fileID = 1002;
        String shortWorkfileName = "testAddFileToFeatureBranch.java";
        DirectoryContentsManager instance = directoryContentsManager;
        instance.addFileToFeatureBranch(branchName, directoryID, fileID, shortWorkfileName, bogusResponseObject);
        DirectoryContents directoryContents = instance.getDirectoryContentsForFeatureBranch(featureBranchProjectBranch, ROOT_DIRECTORY_APPENDED_PATH, directoryID, bogusResponseObject);
        String fileName = directoryContents.getFiles().get(fileID);
        ServerTransactionManager.getInstance().clientEndTransaction(bogusResponseObject);
        assertEquals(shortWorkfileName, fileName);

        // Go back and look again... this one should actually have to go to disk to find the directory contents object.
        ServerTransactionManager.getInstance().clientBeginTransaction(bogusResponseObject);
        directoryContents = instance.getDirectoryContentsForFeatureBranch(featureBranchProjectBranch, ROOT_DIRECTORY_APPENDED_PATH, directoryID, bogusResponseObject);
        fileName = directoryContents.getFiles().get(fileID);
        ServerTransactionManager.getInstance().clientEndTransaction(bogusResponseObject);
        assertEquals(shortWorkfileName, fileName);
    }

    /**
     * Test of addFile method, of class DirectoryContentsManager. This test tries to add a file to a non-existent feature branch... which causes the code under test to throw a
     * QVCSException.
     *
     * @throws java.sql.SQLException for unexpected db problems
     */
    public void testAddFileToFeatureBranchBadBranchName() throws SQLException {
        setUp();
        ServerTransactionManager.getInstance().flushClientTransaction(bogusResponseObject);
        ServerTransactionManager.getInstance().clientBeginTransaction(bogusResponseObject);
        String branchName = "Bogus Branch Name";
        int directoryID = 1;
        int fileID = 1002;
        String shortWorkfileName = "testAddFileToFeatureBranch.java";
        DirectoryContentsManager instance = directoryContentsManager;
        boolean caughtExpectedException = false;
        try {
            instance.addFileToFeatureBranch(branchName, directoryID, fileID, shortWorkfileName, bogusResponseObject);
        }
        catch (QVCSException e) {
            caughtExpectedException = true;
        }
        assertTrue(caughtExpectedException);
    }

    /**
     * Test of renameFile method, of class DirectoryContentsManager on the Trunk branch.
     *
     * @throws Exception if there is a problem.
     */
    public void testRenameFileOnTrunk() throws Exception {
        setUp();
        ServerTransactionManager.getInstance().flushClientTransaction(bogusResponseObject);
        ServerTransactionManager.getInstance().clientBeginTransaction(bogusResponseObject);
        int directoryID = 1;
        int fileID = 1000;
        String oldWorkfileName = "testAddFile.java";
        String newWorkfileName = "newTestAddFileName.java";
        DirectoryContentsManager instance = directoryContentsManager;
        instance.renameFileOnTrunk(directoryID, fileID, oldWorkfileName, newWorkfileName, bogusResponseObject);
        DirectoryContents directoryContents = instance.getDirectoryContentsForTrunk(ROOT_DIRECTORY_APPENDED_PATH, directoryID, bogusResponseObject);
        String fileName = directoryContents.getFiles().get(fileID);
        ServerTransactionManager.getInstance().clientEndTransaction(bogusResponseObject);
        assertEquals(newWorkfileName, fileName);

        // Go back and look again... this one should actually have to go to disk to find the directory contents object.
        ServerTransactionManager.getInstance().clientBeginTransaction(bogusResponseObject);
        directoryContents = instance.getDirectoryContentsForTrunk(ROOT_DIRECTORY_APPENDED_PATH, directoryID, bogusResponseObject);
        fileName = directoryContents.getFiles().get(fileID);
        ServerTransactionManager.getInstance().clientEndTransaction(bogusResponseObject);
        assertEquals(newWorkfileName, fileName);
    }

    /**
     * Test of renameFile method, of class DirectoryContentsManager on the opaque branch.
     *
     * @throws Exception if there is a problem.
     */
    public void testRenameFileOnOpaqueBranch() throws Exception {
        setUp();
        ServerTransactionManager.getInstance().flushClientTransaction(bogusResponseObject);
        ServerTransactionManager.getInstance().clientBeginTransaction(bogusResponseObject);
        String branchName = getOpaqueBranchName();
        int directoryID = 1;
        int fileID = 1001;
        String oldWorkfileName = "testAddFileToOpaqueBranch.java";
        String newWorkfileName = "newTestAddFileToOpaqueBranch.java";
        DirectoryContentsManager instance = directoryContentsManager;
        instance.renameFileOnOpaqueBranch(branchName, fileID, oldWorkfileName, newWorkfileName, bogusResponseObject);
        DirectoryContents directoryContents = instance.getDirectoryContentsForOpaqueBranch(opaqueBranchProjectBranch, ROOT_DIRECTORY_APPENDED_PATH, directoryID, bogusResponseObject);
        String fileName = directoryContents.getFiles().get(fileID);
        ServerTransactionManager.getInstance().clientEndTransaction(bogusResponseObject);
        assertEquals(newWorkfileName, fileName);

        // Go back and look again... this one should actually have to go to disk to find the directory contents object.
        ServerTransactionManager.getInstance().clientBeginTransaction(bogusResponseObject);
        directoryContents = instance.getDirectoryContentsForOpaqueBranch(opaqueBranchProjectBranch, ROOT_DIRECTORY_APPENDED_PATH, directoryID, bogusResponseObject);
        fileName = directoryContents.getFiles().get(fileID);
        ServerTransactionManager.getInstance().clientEndTransaction(bogusResponseObject);
        assertEquals(newWorkfileName, fileName);
    }

    /**
     * Test of renameFile method, of class DirectoryContentsManager on the opaque branch.
     *
     * @throws Exception if there is a problem.
     */
    public void testRenameFileOnOpaqueBranchBadBranchName() throws Exception {
        setUp();
        ServerTransactionManager.getInstance().flushClientTransaction(bogusResponseObject);
        ServerTransactionManager.getInstance().clientBeginTransaction(bogusResponseObject);
        String branchName = "Bogus Branch Name";
        int fileID = 1001;
        String oldWorkfileName = "testAddFileToOpaqueBranch.java";
        String newWorkfileName = "newTestAddFileToOpaqueBranch.java";
        DirectoryContentsManager instance = directoryContentsManager;
        boolean caughtExpectedException = false;
        try {
            instance.renameFileOnOpaqueBranch(branchName, fileID, oldWorkfileName, newWorkfileName, bogusResponseObject);
        }
        catch (QVCSException e) {
            caughtExpectedException = true;
        }
        assertTrue(caughtExpectedException);
    }

    /**
     * Test of renameFile method, of class DirectoryContentsManager on the feature branch.
     *
     * @throws Exception if there is a problem.
     */
    public void testRenameFileOnFeatureBranch() throws Exception {
        setUp();
        ServerTransactionManager.getInstance().flushClientTransaction(bogusResponseObject);
        ServerTransactionManager.getInstance().clientBeginTransaction(bogusResponseObject);
        String branchName = getFeatureBranchName();
        int directoryID = 1;
        int fileID = 1002;
        String oldWorkfileName = "testAddFileToFeatureBranch.java";
        String newWorkfileName = "newTestAddFileToFeatureBranch.java";
        DirectoryContentsManager instance = directoryContentsManager;
        instance.renameFileOnFeatureBranch(branchName, fileID, oldWorkfileName, newWorkfileName, bogusResponseObject);
        DirectoryContents directoryContents = instance.getDirectoryContentsForFeatureBranch(featureBranchProjectBranch, ROOT_DIRECTORY_APPENDED_PATH, directoryID, bogusResponseObject);
        String fileName = directoryContents.getFiles().get(fileID);
        ServerTransactionManager.getInstance().clientEndTransaction(bogusResponseObject);
        assertEquals(newWorkfileName, fileName);

        // Go back and look again... this one should actually have to go to disk to find the directory contents object.
        ServerTransactionManager.getInstance().clientBeginTransaction(bogusResponseObject);
        directoryContents = instance.getDirectoryContentsForFeatureBranch(featureBranchProjectBranch, ROOT_DIRECTORY_APPENDED_PATH, directoryID, bogusResponseObject);
        fileName = directoryContents.getFiles().get(fileID);
        ServerTransactionManager.getInstance().clientEndTransaction(bogusResponseObject);
        assertEquals(newWorkfileName, fileName);
    }

    /**
     * Test of renameFile method, of class DirectoryContentsManager on the feature branch.
     *
     * @throws Exception if there is a problem.
     */
    public void testRenameFileOnFeatureBranchBadBranchName() throws Exception {
        setUp();
        ServerTransactionManager.getInstance().flushClientTransaction(bogusResponseObject);
        ServerTransactionManager.getInstance().clientBeginTransaction(bogusResponseObject);
        String branchName = "Bogus Branch Name";
        int fileID = 1002;
        String oldWorkfileName = "testAddFileToFeatureBranch.java";
        String newWorkfileName = "newTestAddFileToFeatureBranch.java";
        DirectoryContentsManager instance = directoryContentsManager;
        boolean caughtExpectedException = false;
        try {
            instance.renameFileOnFeatureBranch(branchName, fileID, oldWorkfileName, newWorkfileName, bogusResponseObject);
        }
        catch (QVCSException e) {
            caughtExpectedException = true;
        }
        assertTrue(caughtExpectedException);
    }

    /**
     * Test of moveFile method, of class DirectoryContentsManager. This one tests moves on the Trunk.
     *
     * @throws Exception if there is a problem.
     */
    public void testMoveFileOnTrunk() throws Exception {
        setUp();
        ServerTransactionManager.getInstance().flushClientTransaction(bogusResponseObject);
        ServerTransactionManager.getInstance().clientBeginTransaction(bogusResponseObject);
        String branchName = QVCSConstants.QVCS_TRUNK_BRANCH;
        int originDirectoryID = 1;
        int destinationDirectoryID = 2;
        int fileID = 1000;
        String workfileName = "newTestAddFileName.java";
        DirectoryContentsManager instance = directoryContentsManager;
        instance.moveFileOnTrunk(branchName, originDirectoryID, destinationDirectoryID, fileID, bogusResponseObject);
        DirectoryContents directoryContents = instance.getDirectoryContentsForTrunk(SUB_DIRECTORY_1_APPENDED_PATH, destinationDirectoryID, bogusResponseObject);
        String fileName = directoryContents.getFiles().get(fileID);
        ServerTransactionManager.getInstance().clientEndTransaction(bogusResponseObject);
        assertEquals(workfileName, fileName);

        // Go back and look again... this one should actually have to go to disk to find the directory contents object.
        ServerTransactionManager.getInstance().clientBeginTransaction(bogusResponseObject);
        directoryContents = instance.getDirectoryContentsForTrunk(SUB_DIRECTORY_1_APPENDED_PATH, destinationDirectoryID, bogusResponseObject);
        fileName = directoryContents.getFiles().get(fileID);
        ServerTransactionManager.getInstance().clientEndTransaction(bogusResponseObject);
        assertEquals(workfileName, fileName);
    }

    /**
     * Test of moveFile method, of class DirectoryContentsManager. This one tests moves on an opaque branch.
     *
     * @throws Exception if there is a problem.
     */
    public void testMoveFileOnOpaqueBranch() throws Exception {
        setUp();
        ServerTransactionManager.getInstance().flushClientTransaction(bogusResponseObject);
        ServerTransactionManager.getInstance().clientBeginTransaction(bogusResponseObject);
        String branchName = getOpaqueBranchName();
        int originDirectoryID = 1;
        int destinationDirectoryID = 2;
        int fileID = 1001;
        String workfileName = "newTestAddFileToOpaqueBranch.java";
        DirectoryContentsManager instance = directoryContentsManager;
        instance.moveFileOnOpaqueBranch(branchName, originDirectoryID, destinationDirectoryID, fileID, bogusResponseObject);
        DirectoryContents directoryContents = instance.getDirectoryContentsForOpaqueBranch(opaqueBranchProjectBranch, SUB_DIRECTORY_1_APPENDED_PATH, destinationDirectoryID, bogusResponseObject);
        String fileName = directoryContents.getFiles().get(fileID);
        ServerTransactionManager.getInstance().clientEndTransaction(bogusResponseObject);
        assertEquals(workfileName, fileName);

        // Go back and look again... this one should actually have to go to disk to find the directory contents object.
        ServerTransactionManager.getInstance().clientBeginTransaction(bogusResponseObject);
        directoryContents = instance.getDirectoryContentsForOpaqueBranch(opaqueBranchProjectBranch, SUB_DIRECTORY_1_APPENDED_PATH, destinationDirectoryID, bogusResponseObject);
        fileName = directoryContents.getFiles().get(fileID);
        ServerTransactionManager.getInstance().clientEndTransaction(bogusResponseObject);
        assertEquals(workfileName, fileName);
    }

    /**
     * Test of moveFile method, of class DirectoryContentsManager. This one tests moves on an opaque branch.
     *
     * @throws Exception if there is a problem.
     */
    public void testMoveFileOnOpaqueBranchBadBranchName() throws Exception {
        setUp();
        ServerTransactionManager.getInstance().flushClientTransaction(bogusResponseObject);
        ServerTransactionManager.getInstance().clientBeginTransaction(bogusResponseObject);
        String branchName = "Bogus Branch Name";
        int originDirectoryID = 1;
        int destinationDirectoryID = 2;
        int fileID = 1001;
        DirectoryContentsManager instance = directoryContentsManager;
        boolean caughtExpectedException = false;
        try {
            instance.moveFileOnOpaqueBranch(branchName, originDirectoryID, destinationDirectoryID, fileID, bogusResponseObject);
        }
        catch (QVCSException e) {
            caughtExpectedException = true;
        }
        assertTrue(caughtExpectedException);
    }

    /**
     * Test of moveFile method, of class DirectoryContentsManager. This one tests moves on an feature branch.
     *
     * @throws Exception if there is a problem.
     */
    public void testMoveFileOnFeatureBranch() throws Exception {
        setUp();
        ServerTransactionManager.getInstance().flushClientTransaction(bogusResponseObject);
        ServerTransactionManager.getInstance().clientBeginTransaction(bogusResponseObject);
        String branchName = getFeatureBranchName();
        int originDirectoryID = 1;
        int destinationDirectoryID = 2;
        int fileID = 1002;
        String workfileName = "newTestAddFileToFeatureBranch.java";
        DirectoryContentsManager instance = directoryContentsManager;
        instance.moveFileOnFeatureBranch(branchName, originDirectoryID, destinationDirectoryID, fileID, bogusResponseObject);
        DirectoryContents directoryContents = instance.getDirectoryContentsForFeatureBranch(featureBranchProjectBranch, SUB_DIRECTORY_1_APPENDED_PATH, destinationDirectoryID,
                bogusResponseObject);
        String fileName = directoryContents.getFiles().get(fileID);
        ServerTransactionManager.getInstance().clientEndTransaction(bogusResponseObject);
        assertEquals(workfileName, fileName);

        // Go back and look again... this one should actually have to go to disk to find the directory contents object.
        ServerTransactionManager.getInstance().clientBeginTransaction(bogusResponseObject);
        directoryContents = instance.getDirectoryContentsForFeatureBranch(featureBranchProjectBranch, SUB_DIRECTORY_1_APPENDED_PATH, destinationDirectoryID, bogusResponseObject);
        fileName = directoryContents.getFiles().get(fileID);
        ServerTransactionManager.getInstance().clientEndTransaction(bogusResponseObject);
        assertEquals(workfileName, fileName);
    }

    /**
     * Test of moveFile method, of class DirectoryContentsManager. This one tests moves on an feature branch.
     *
     * @throws Exception if there is a problem.
     */
    public void testMoveFileOnFeatureBranchBadBranchName() throws Exception {
        setUp();
        ServerTransactionManager.getInstance().flushClientTransaction(bogusResponseObject);
        ServerTransactionManager.getInstance().clientBeginTransaction(bogusResponseObject);
        String branchName = "Bogus Branch Name";
        int originDirectoryID = 1;
        int destinationDirectoryID = 2;
        int fileID = 1002;
        DirectoryContentsManager instance = directoryContentsManager;
        boolean caughtExpectedException = false;
        try {
            instance.moveFileOnFeatureBranch(branchName, originDirectoryID, destinationDirectoryID, fileID, bogusResponseObject);
        }
        catch (QVCSException e) {
            caughtExpectedException = true;
        }
        assertTrue(caughtExpectedException);
    }

    /**
     * Test of deleteFile method, of class DirectoryContentsManager. This one tests deletes on the Trunk.
     *
     * @throws Exception if there is a problem.
     */
    public void testDeleteFileOnTrunk() throws Exception {
        setUp();
        ServerTransactionManager.getInstance().flushClientTransaction(bogusResponseObject);
        ServerTransactionManager.getInstance().clientBeginTransaction(bogusResponseObject);
        int originDirectoryID = 2;
        try {
            // Get the cemetery archive directory manager
            ArchiveDirManagerInterface cemeteryArchiveDirManagerInterface = ServerUtility.getCemeteryArchiveDirManager(getProjectName(), bogusResponseObject);
            // Cast the cemeteryArchiveDirManagerInterface to an actual ArchiveDirManager, since that is
            // what it MUST be here...
            ArchiveDirManager cemeteryArchiveDirManager = (ArchiveDirManager) cemeteryArchiveDirManagerInterface;
            int cemeteryDirectoryID = cemeteryArchiveDirManager.getDirectoryID();
            int fileID = 1000;
            String workfileName = "newTestAddFileName.java";
            ServerResponseFactoryInterface response = bogusResponseObject;
            DirectoryContentsManager instance = directoryContentsManager;
            instance.deleteFileFromTrunk(SUB_DIRECTORY_1_APPENDED_PATH, originDirectoryID, cemeteryDirectoryID, fileID, workfileName, response);
            DirectoryContents cemeteryDirectoryContents = instance.getDirectoryContentsForTrunk(QVCSConstants.QVCS_CEMETERY_DIRECTORY, cemeteryDirectoryID, bogusResponseObject);
            DirectoryContents originDirectoryContents = instance.getDirectoryContentsForTrunk(SUB_DIRECTORY_1_APPENDED_PATH, originDirectoryID, bogusResponseObject);
            String cemeteryFileName = cemeteryDirectoryContents.getFiles().get(fileID);
            String cemeteryShortArchiveName = Utility.createCemeteryShortArchiveName(fileID);
            String cemeteryShortFileName = Utility.convertArchiveNameToShortWorkfileName(cemeteryShortArchiveName);
            ServerTransactionManager.getInstance().clientEndTransaction(bogusResponseObject);
            assertNotNull(cemeteryFileName);
            assertNull(originDirectoryContents.getFiles().get(fileID));
            assertEquals(cemeteryShortFileName, cemeteryFileName);

            // Go back and look again... this one should actually have to go to disk to find the directory contents object.
            ServerTransactionManager.getInstance().clientBeginTransaction(bogusResponseObject);
            originDirectoryContents = instance.getDirectoryContentsForTrunk(SUB_DIRECTORY_1_APPENDED_PATH, originDirectoryID, bogusResponseObject);
            instance.getDirectoryContentsForTrunk(QVCSConstants.QVCS_CEMETERY_DIRECTORY, cemeteryDirectoryID, bogusResponseObject);
            cemeteryShortArchiveName = Utility.createCemeteryShortArchiveName(fileID);
            cemeteryShortFileName = Utility.convertArchiveNameToShortWorkfileName(cemeteryShortArchiveName);
            ServerTransactionManager.getInstance().clientEndTransaction(bogusResponseObject);
            assertNull(originDirectoryContents.getFiles().get(fileID));
            assertEquals(cemeteryShortFileName, cemeteryFileName);
        }
        catch (NullPointerException e) {
            String message = Utility.expandStackTraceToString(e);
            fail("Unexpected null pointer exception: " + message);
        }
        catch (QVCSException e) {
            String message = Utility.expandStackTraceToString(e);
            fail("Unexpected QVCS exception: " + message);
        }
    }

    /**
     * Test of deleteFile method, of class DirectoryContentsManager. This one tests deletes on an opaque branch.
     */
    public void testDeleteFileOnOpaqueBranch() {
        setUp();
        ServerTransactionManager.getInstance().flushClientTransaction(bogusResponseObject);
        ServerTransactionManager.getInstance().clientBeginTransaction(bogusResponseObject);
        String branchName = getOpaqueBranchName();
        int originDirectoryID = 2;

        int fileID = 1001;
        String workfileName = "newTestAddFileToOpaqueBranch.java";
        boolean noExceptionsFlag = false;
        try {
            DirectoryContentsManager instance = directoryContentsManager;
            instance.deleteFileFromOpaqueBranch(branchName, originDirectoryID, -1, fileID, workfileName, bogusResponseObject);
            DirectoryContents originDirectoryContents = instance.getDirectoryContentsForOpaqueBranch(opaqueBranchProjectBranch, SUB_DIRECTORY_1_APPENDED_PATH, originDirectoryID, bogusResponseObject);
            ServerTransactionManager.getInstance().clientEndTransaction(bogusResponseObject);
            assertTrue(null == originDirectoryContents.getFiles().get(fileID));

            // Go back and look again... this one should actually have to go to disk to find the directory contents object.
            ServerTransactionManager.getInstance().clientBeginTransaction(bogusResponseObject);
            originDirectoryContents = instance.getDirectoryContentsForOpaqueBranch(opaqueBranchProjectBranch, SUB_DIRECTORY_1_APPENDED_PATH, originDirectoryID, bogusResponseObject);
            String fileName = originDirectoryContents.getFiles().get(fileID);
            ServerTransactionManager.getInstance().clientEndTransaction(bogusResponseObject);
            assertEquals(null, fileName);
            noExceptionsFlag = true;
        }
        catch (NullPointerException e) {
            fail("Unexpected null pointer exception");
        }
        catch (QVCSException e) {
            fail("Unexpected QVCS exception");
        }
        finally {
            if (noExceptionsFlag == false) {
                fail("Unknown exception caused test to fail.");
            }
        }
    }

    /**
     * Test of deleteFile method, of class DirectoryContentsManager. This one tests deletes on an opaque branch.
     *
     * @throws Exception if there is a problem.
     */
    public void testDeleteFileOnOpaqueBranchBadBranchName() throws Exception {
        setUp();
        ServerTransactionManager.getInstance().flushClientTransaction(bogusResponseObject);
        ServerTransactionManager.getInstance().clientBeginTransaction(bogusResponseObject);
        String branchName = "Bogus Branch Name";
        int originDirectoryID = 2;

        int fileID = 1001;
        String workfileName = "newTestAddFileToOpaqueBranch.java";
        boolean caughtExpectedException = false;
        try {
            DirectoryContentsManager instance = directoryContentsManager;
            instance.deleteFileFromOpaqueBranch(branchName, originDirectoryID, -1, fileID, workfileName, bogusResponseObject);
        }
        catch (QVCSException e) {
            caughtExpectedException = true;
        }
        assertTrue(caughtExpectedException);
    }

    /**
     * Test of deleteFile method, of class DirectoryContentsManager. This one tests deletes on a feature branch.
     */
    public void testDeleteFileOnFeatureBranch() {
        setUp();
        ServerTransactionManager.getInstance().flushClientTransaction(bogusResponseObject);
        ServerTransactionManager.getInstance().clientBeginTransaction(bogusResponseObject);
        String branchName = getFeatureBranchName();
        int originDirectoryID = 2;

        int fileID = 1002;
        String workfileName = "newTestAddFileToFeatureBranch.java";
        boolean noExceptionsFlag = false;
        try {
            DirectoryContentsManager instance = directoryContentsManager;
            instance.deleteFileFromFeatureBranch(branchName, originDirectoryID, -1, fileID, workfileName, bogusResponseObject);
            DirectoryContents originDirectoryContents = instance.getDirectoryContentsForFeatureBranch(featureBranchProjectBranch, SUB_DIRECTORY_1_APPENDED_PATH,
                    originDirectoryID, bogusResponseObject);
            ServerTransactionManager.getInstance().clientEndTransaction(bogusResponseObject);
            assertTrue(null == originDirectoryContents.getFiles().get(fileID));

            // Go back and look again... this one should actually have to go to disk to find the directory contents object.
            ServerTransactionManager.getInstance().clientBeginTransaction(bogusResponseObject);
            originDirectoryContents = instance.getDirectoryContentsForFeatureBranch(featureBranchProjectBranch, SUB_DIRECTORY_1_APPENDED_PATH, originDirectoryID, bogusResponseObject);
            String fileName = originDirectoryContents.getFiles().get(fileID);
            ServerTransactionManager.getInstance().clientEndTransaction(bogusResponseObject);
            assertEquals(null, fileName);
            noExceptionsFlag = true;
        }
        catch (NullPointerException e) {
            fail("Unexpected null pointer exception");
        }
        catch (QVCSException e) {
            fail("Unexpected QVCS exception");
        }
        finally {
            if (noExceptionsFlag == false) {
                fail("Some unknown exception was thrown");
            }
        }
    }

    /**
     * Test of deleteFile method, of class DirectoryContentsManager. This one tests deletes on a non-existent feature branch.
     *
     * @throws Exception if there is a problem.
     */
    public void testDeleteFileOnFeatureBranchBadBranchName() throws Exception {
        setUp();
        ServerTransactionManager.getInstance().flushClientTransaction(bogusResponseObject);
        ServerTransactionManager.getInstance().clientBeginTransaction(bogusResponseObject);
        String branchName = "Bogus Branch Name";
        int originDirectoryID = 2;

        int fileID = 1002;
        String workfileName = "newTestAddFileToFeatureBranch.java";
        boolean caughtExpectedException = false;
        try {
            DirectoryContentsManager instance = directoryContentsManager;
            instance.deleteFileFromFeatureBranch(branchName, originDirectoryID, -1, fileID, workfileName, bogusResponseObject);
        }
        catch (QVCSException e) {
            caughtExpectedException = true;
        }
        assertTrue(caughtExpectedException);
    }

    /**
     * Test of moveFileFromFeatureBranchCemetery method, of class DirectoryContentsManager.
     */
    public void testMoveFileFromFeatureBranchCemetery() {
        setUp();
        ServerTransactionManager.getInstance().flushClientTransaction(bogusResponseObject);
        ServerTransactionManager.getInstance().clientBeginTransaction(bogusResponseObject);
        String branchName = getFeatureBranchName();
        int destinationDirectoryId = 2;

        int fileId = 1002;
        String workfileName = "newTestAddFileToFeatureBranch.java";
        boolean noExceptionsFlag = false;
        try {
            DirectoryContentsManager instance = directoryContentsManager;
            instance.moveFileFromFeatureBranchCemetery(branchName, destinationDirectoryId, fileId, workfileName, bogusResponseObject);
            DirectoryContents originDirectoryContents = instance.getDirectoryContentsForFeatureBranch(featureBranchProjectBranch, SUB_DIRECTORY_1_APPENDED_PATH,
                    destinationDirectoryId, bogusResponseObject);
            ServerTransactionManager.getInstance().clientEndTransaction(bogusResponseObject);
            assertTrue(null != originDirectoryContents.getFiles().get(fileId));

            // Go back and look again... this one should actually have to go to disk to find the directory contents object.
            ServerTransactionManager.getInstance().clientBeginTransaction(bogusResponseObject);
            originDirectoryContents = instance.getDirectoryContentsForFeatureBranch(featureBranchProjectBranch, SUB_DIRECTORY_1_APPENDED_PATH, destinationDirectoryId, bogusResponseObject);
            String fileName = originDirectoryContents.getFiles().get(fileId);
            ServerTransactionManager.getInstance().clientEndTransaction(bogusResponseObject);
            assertNotNull(fileName);
            noExceptionsFlag = true;
        }
        catch (NullPointerException e) {
            fail("Unexpected null pointer exception");
        }
        catch (QVCSException e) {
            fail("Unexpected QVCS exception");
        }
        finally {
            if (noExceptionsFlag == false) {
                fail("Some unknown exception was thrown");
            }
        }
    }

    /**
     * Test of addDirectory method, of class DirectoryContentsManager.
     *
     * @throws Exception if there is a problem.
     */
    public void testAddDirectoryOnTrunk() throws Exception {
        setUp();
        ServerTransactionManager.getInstance().flushClientTransaction(bogusResponseObject);
        ServerTransactionManager.getInstance().clientBeginTransaction(bogusResponseObject);
        String branchName = QVCSConstants.QVCS_TRUNK_BRANCH;
        int parentDirectoryID = 1;
        int childDirectoryID = 10;
        String childDirectoryName = ADDED_SUB_DIRECTORY_APPENDED_PATH;
        directoryContentsManager.addDirectory(branchName, 1, ROOT_DIRECTORY_APPENDED_PATH, parentDirectoryID, childDirectoryID, childDirectoryName, bogusResponseObject);
        ServerTransactionManager.getInstance().clientEndTransaction(bogusResponseObject);

        // Go back and look again... this one should actually have to go to disk to find the directory contents object.
        ServerTransactionManager.getInstance().clientBeginTransaction(bogusResponseObject);
        DirectoryContents directoryContents = directoryContentsManager.getDirectoryContentsForTrunk(ROOT_DIRECTORY_APPENDED_PATH, parentDirectoryID, bogusResponseObject);
        String directoryName = directoryContents.getChildDirectories().get(childDirectoryID);
        ServerTransactionManager.getInstance().clientEndTransaction(bogusResponseObject);
        assertEquals(childDirectoryName, directoryName);
    }

    /**
     * Test of addDirectory method, of class DirectoryContentsManager.
     *
     * @throws Exception if there is a problem.
     */
    public void testAddDirectoryOnOpaqueBranch() throws Exception {
        System.out.println("testAddDirectoryOnOpaqueBranch");
        setUp();
        ServerTransactionManager.getInstance().flushClientTransaction(bogusResponseObject);
        ServerTransactionManager.getInstance().clientBeginTransaction(bogusResponseObject);
        String branchName = getOpaqueBranchName();
        int parentDirectoryID = 1;
        int childDirectoryID = 10;
        String childDirectoryName = ADDED_SUB_DIRECTORY_APPENDED_PATH;
        directoryContentsManager.addDirectory(branchName, 1, ROOT_DIRECTORY_APPENDED_PATH, parentDirectoryID, childDirectoryID, childDirectoryName, bogusResponseObject);
        ServerTransactionManager.getInstance().clientEndTransaction(bogusResponseObject);

        // Go back and look again... this one should actually have to go to disk to find the directory contents object.
        ServerTransactionManager.getInstance().clientBeginTransaction(bogusResponseObject);
        DirectoryContents directoryContents = directoryContentsManager.getDirectoryContentsForOpaqueBranch(opaqueBranchProjectBranch, ROOT_DIRECTORY_APPENDED_PATH, parentDirectoryID,
                bogusResponseObject);
        String directoryName = directoryContents.getChildDirectories().get(childDirectoryID);
        ServerTransactionManager.getInstance().clientEndTransaction(bogusResponseObject);
        assertEquals(childDirectoryName, directoryName);
    }

    /**
     * Test of addDirectory method, of class DirectoryContentsManager.
     *
     * @throws Exception if there is a problem.
     */
    public void testAddDirectoryOnFeatureBranch() throws Exception {
        setUp();
        ServerTransactionManager.getInstance().flushClientTransaction(bogusResponseObject);
        ServerTransactionManager.getInstance().clientBeginTransaction(bogusResponseObject);
        String branchName = getFeatureBranchName();
        int parentDirectoryID = 1;
        int childDirectoryID = 10;
        String childDirectoryName = ADDED_SUB_DIRECTORY_APPENDED_PATH;
        directoryContentsManager.addDirectory(branchName, 1, ROOT_DIRECTORY_APPENDED_PATH, parentDirectoryID, childDirectoryID, childDirectoryName, bogusResponseObject);
        ServerTransactionManager.getInstance().clientEndTransaction(bogusResponseObject);

        // Go back and look again... this one should actually have to go to disk to find the directory contents object.
        ServerTransactionManager.getInstance().clientBeginTransaction(bogusResponseObject);
        DirectoryContents directoryContents = directoryContentsManager.getDirectoryContentsForFeatureBranch(featureBranchProjectBranch, ROOT_DIRECTORY_APPENDED_PATH, parentDirectoryID,
                bogusResponseObject);
        String directoryName = directoryContents.getChildDirectories().get(childDirectoryID);
        ServerTransactionManager.getInstance().clientEndTransaction(bogusResponseObject);
        assertEquals(childDirectoryName, directoryName);
    }

    /**
     * Test of deleteDirectory method, of class DirectoryContentsManager.
     *
     * @throws Exception if there is a problem.
     */
    public void testDeleteDirectoryOnTrunk() throws Exception {
        setUp();
        ServerTransactionManager.getInstance().flushClientTransaction(bogusResponseObject);
        ServerTransactionManager.getInstance().clientBeginTransaction(bogusResponseObject);
        int parentDirectoryID = 1;
        int childDirectoryID = 10;
        DirectoryContentsManager instance = directoryContentsManager;
        DirectoryContents directoryContents = instance.getDirectoryContentsForTrunk(ROOT_DIRECTORY_APPENDED_PATH, parentDirectoryID, bogusResponseObject);
        String directoryName = directoryContents.getChildDirectories().get(childDirectoryID);
        assertNotNull(directoryName);
        instance.deleteDirectoryOnTrunk(childDirectoryID, bogusResponseObject);
        ServerTransactionManager.getInstance().clientEndTransaction(bogusResponseObject);

        // Go back and look again... this one should actually have to go to disk to find the directory contents object.
        ServerTransactionManager.getInstance().clientBeginTransaction(bogusResponseObject);
        directoryContents = instance.getDirectoryContentsForTrunk(ROOT_DIRECTORY_APPENDED_PATH, parentDirectoryID, bogusResponseObject);
        directoryName = directoryContents.getChildDirectories().get(childDirectoryID);
        ServerTransactionManager.getInstance().clientEndTransaction(bogusResponseObject);
        assertEquals(null, directoryName);
    }

    /**
     * Test of deleteDirectory method, of class DirectoryContentsManager.
     *
     * @throws Exception if there is a problem.
     */
    public void testDeleteDirectoryOnOpaqueBranch() throws Exception {
        setUp();
        ServerTransactionManager.getInstance().flushClientTransaction(bogusResponseObject);
        ServerTransactionManager.getInstance().clientBeginTransaction(bogusResponseObject);
        String branchName = getOpaqueBranchName();
        int parentDirectoryID = 1;
        int childDirectoryID = 10;
        DirectoryContentsManager instance = directoryContentsManager;
        DirectoryContents directoryContents = instance.getDirectoryContentsForOpaqueBranch(opaqueBranchProjectBranch, ROOT_DIRECTORY_APPENDED_PATH, parentDirectoryID, bogusResponseObject);
        String directoryName = directoryContents.getChildDirectories().get(childDirectoryID);
        assertNotNull(directoryName);
        instance.deleteDirectoryOnOpaqueBranch(branchName, childDirectoryID, bogusResponseObject);
        ServerTransactionManager.getInstance().clientEndTransaction(bogusResponseObject);

        // Go back and look again... this one should actually have to go to disk to find the directory contents object.
        ServerTransactionManager.getInstance().clientBeginTransaction(bogusResponseObject);
        directoryContents = instance.getDirectoryContentsForOpaqueBranch(opaqueBranchProjectBranch, ROOT_DIRECTORY_APPENDED_PATH, parentDirectoryID, bogusResponseObject);
        directoryName = directoryContents.getChildDirectories().get(childDirectoryID);
        ServerTransactionManager.getInstance().clientEndTransaction(bogusResponseObject);
        assertEquals(null, directoryName);
    }

    /**
     * Test of deleteDirectory method, of class DirectoryContentsManager.
     *
     * @throws QVCSException if there is a problem.
     */
    public void testDeleteDirectoryOnFeatureBranch() throws QVCSException {
        setUp();
        ServerTransactionManager.getInstance().flushClientTransaction(bogusResponseObject);
        ServerTransactionManager.getInstance().clientBeginTransaction(bogusResponseObject);
        String branchName = getFeatureBranchName();
        int parentDirectoryID = 1;
        int childDirectoryID = 10;
        DirectoryContentsManager instance = directoryContentsManager;
        DirectoryContents directoryContents = instance.getDirectoryContentsForFeatureBranch(featureBranchProjectBranch, ROOT_DIRECTORY_APPENDED_PATH, parentDirectoryID, bogusResponseObject);
        String directoryName = directoryContents.getChildDirectories().get(childDirectoryID);
        assertNotNull(directoryName);
        instance.deleteDirectoryOnFeatureBranch(branchName, childDirectoryID, bogusResponseObject);
        ServerTransactionManager.getInstance().clientEndTransaction(bogusResponseObject);

        // Go back and look again... this one should actually have to go to disk to find the directory contents object.
        ServerTransactionManager.getInstance().clientBeginTransaction(bogusResponseObject);
        directoryContents = instance.getDirectoryContentsForFeatureBranch(featureBranchProjectBranch, ROOT_DIRECTORY_APPENDED_PATH, parentDirectoryID, bogusResponseObject);
        directoryName = directoryContents.getChildDirectories().get(childDirectoryID);
        ServerTransactionManager.getInstance().clientEndTransaction(bogusResponseObject);
        assertEquals(null, directoryName);
    }

    /**
     * Test of getDirectoryIDCollectionForFeatureBranch method, of class DirectoryContentsManager.
     *
     * @throws Exception if there is a problem.
     */
    public void testGetDirectoryIDCollectionForDateBasedBranch() throws Exception {
        setUp();
        ServerTransactionManager.getInstance().flushClientTransaction(bogusResponseObject);
        ServerTransactionManager.getInstance().clientBeginTransaction(bogusResponseObject);
        int directoryId = 1;
        DirectoryContentsManager instance = directoryContentsManager;
        Map<Integer, String> result = instance.getDirectoryIDCollectionForDateBasedBranch(dateBasedProjectBranch.getBranchName(), ROOT_DIRECTORY_APPENDED_PATH, directoryId, bogusResponseObject);
        assertEquals(1, result.size());
        ServerTransactionManager.getInstance().clientEndTransaction(bogusResponseObject);
    }

    /**
     * Test of getDirectoryIDCollectionForFeatureBranch method, of class DirectoryContentsManager.
     *
     * @throws Exception if there is a problem.
     */
    public void testGetDirectoryIDCollectionForFeatureBranch() throws Exception {
        setUp();
        ServerTransactionManager.getInstance().flushClientTransaction(bogusResponseObject);
        ServerTransactionManager.getInstance().clientBeginTransaction(bogusResponseObject);
        int directoryId = 1;
        DirectoryContentsManager instance = directoryContentsManager;
        Map<Integer, String> result = instance.getDirectoryIDCollectionForFeatureBranch(featureBranchProjectBranch, ROOT_DIRECTORY_APPENDED_PATH, directoryId, bogusResponseObject);
        assertEquals(1, result.size());
        ServerTransactionManager.getInstance().clientEndTransaction(bogusResponseObject);
    }

    /**
     * Test of getDirectoryIDCollectionForOpaqueBranch method, of class DirectoryContentsManager.
     *
     * @throws Exception if there is a problem.
     */
    public void testGetDirectoryIDCollectionForOpaqueBranch() throws Exception {
        setUp();
        ServerTransactionManager.getInstance().flushClientTransaction(bogusResponseObject);
        ServerTransactionManager.getInstance().clientBeginTransaction(bogusResponseObject);
        int directoryId = 1;
        DirectoryContentsManager instance = directoryContentsManager;
        Map<Integer, String> result = instance.getDirectoryIDCollectionForOpaqueBranch(opaqueBranchProjectBranch, ROOT_DIRECTORY_APPENDED_PATH, directoryId, bogusResponseObject);
        assertEquals(1, result.size());
        ServerTransactionManager.getInstance().clientEndTransaction(bogusResponseObject);
    }

    /**
     * Test of getPriority method, of class DirectoryContentsManager.
     */
    public void testGetPriority() {
        setUp();
        ServerTransactionManager.getInstance().flushClientTransaction(bogusResponseObject);
        ServerTransactionManager.getInstance().clientBeginTransaction(bogusResponseObject);
        DirectoryContentsManager instance = directoryContentsManager;
        int expResult = TransactionParticipantInterface.HIGH_PRIORITY;
        int result = instance.getPriority();
        assertEquals(expResult, result);
        ServerTransactionManager.getInstance().clientEndTransaction(bogusResponseObject);
    }
}
