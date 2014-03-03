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
//     $Date: Wednesday, March 21, 2012 10:31:04 PM $
//   $Header: DirectoryContentsManagerTest.java Revision:1.3 Wednesday, March 21, 2012 10:31:04 PM JimVoris $
// $Copyright © 2011-2012 Define this string in the qvcs.keywords.properties property file $

package com.qumasoft.server;

import com.qumasoft.TestHelper;
import com.qumasoft.qvcslib.ArchiveDirManagerInterface;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.QVCSException;
import com.qumasoft.qvcslib.RemoteViewProperties;
import com.qumasoft.qvcslib.ServerResponseFactoryInterface;
import com.qumasoft.qvcslib.Utility;
import com.qumasoft.server.dataaccess.impl.DAOTestHelper;
import java.io.File;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test the Directory Contents Manager.
 *
 * @author $Author: JimVoris $
 */
public class DirectoryContentsManagerTest {

    private static final String DERBY_TEST_DIRECTORY = "/temp/qvcse/directoryContentsManagerTest";
    private static int testProjectId = -1;
    private static int testTrunkBranchId = -1;
    private static ProjectView translucentBranchProjectView = null;
    private static ProjectView opaqueBranchProjectView = null;
    private static ProjectView dateBasedViewProjectView = null;
    private static RemoteViewProperties translucentBranchProperties = null;
    private static RemoteViewProperties opaqueBranchProperties = null;
    private static RemoteViewProperties dateBasedViewProperties = null;
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
        String userDirectoryString = System.getProperty("user.dir");
        try {
            File userDirectory = new File(userDirectoryString);
            String canonicalUserDirectory = userDirectory.getCanonicalPath();
            System.getProperties().setProperty("user.dir", canonicalUserDirectory);
        } finally {
        }
        TestHelper.emptyDerbyTestDirectory(DERBY_TEST_DIRECTORY);
        TestHelper.deleteViewStore();
        TestHelper.removeArchiveFiles();
        ViewManager.getInstance().initialize();
        DirectoryIDManager.getInstance().resetStore();
        DirectoryIDManager.getInstance().initialize();
        FileIDManager.getInstance().resetStore();
        FileIDManager.getInstance().initialize();
        DirectoryIDDictionary.getInstance().resetStore();
        DirectoryIDDictionary.getInstance().initialize();
        FileIDDictionary.getInstance().resetStore();
        FileIDDictionary.getInstance().initialize();
        DatabaseManager.getInstance().setDerbyHomeDirectory(DERBY_TEST_DIRECTORY);
        DatabaseManager.getInstance().initializeDatabase();
        testProjectId = DAOTestHelper.createTestProject();
        testTrunkBranchId = DAOTestHelper.createTrunkBranch(testProjectId);
        DAOTestHelper.populateDbWithTestFiles();
        initializeOpaqueBranch();
        initializeTranslucentBranch();
        initializeDateBasedView();
    }

    /**
     * Tear down the class level stuff.
     *
     * @throws java.lang.Exception if anything goes wrong.
     */
    @AfterClass
    public static void tearDownClass() throws Exception {
        DatabaseManager.getInstance().shutdownDatabase();
    }

    /**
     * Set up before each test.
     */
    @Before
    public void setUp() {
        directoryContentsManager = DirectoryContentsManagerFactory.getInstance().getDirectoryContentsManager(TestHelper.getTestProjectName());
        bogusResponseObject = new BogusResponseObject();
        ServerTransactionManager.getInstance().flushClientTransaction(bogusResponseObject);
        ServerTransactionManager.getInstance().clientBeginTransaction(bogusResponseObject);
    }

    /**
     * Tear down after each test.
     */
    @After
    public void tearDown() {
    }

    static private void initializeDateBasedView() throws QVCSException {
        Properties projectProperties = new Properties();
        dateBasedViewProperties = new RemoteViewProperties(getProjectName(), getDateBasedViewName(), projectProperties);
        dateBasedViewProperties.setIsReadOnlyViewFlag(true);
        dateBasedViewProperties.setIsDateBasedViewFlag(true);
        dateBasedViewProperties.setIsTranslucentBranchFlag(false);
        dateBasedViewProperties.setIsOpaqueBranchFlag(false);
        dateBasedViewProperties.setBranchParent(QVCSConstants.QVCS_TRUNK_VIEW);
        dateBasedViewProperties.setDateBaseDate(new Date());
        dateBasedViewProjectView = new ProjectView();
        dateBasedViewProjectView.setProjectName(getProjectName());
        dateBasedViewProjectView.setViewName(getDateBasedViewName());
        dateBasedViewProjectView.setRemoteViewProperties(dateBasedViewProperties);
        ViewManager.getInstance().addView(dateBasedViewProjectView);
    }

    static private void initializeOpaqueBranch() throws QVCSException {
        Properties projectProperties = new Properties();
        opaqueBranchProperties = new RemoteViewProperties(getProjectName(), getTranslucentBranchName(), projectProperties);
        opaqueBranchProperties.setIsReadOnlyViewFlag(false);
        opaqueBranchProperties.setIsDateBasedViewFlag(false);
        opaqueBranchProperties.setIsTranslucentBranchFlag(false);
        opaqueBranchProperties.setIsOpaqueBranchFlag(true);
        opaqueBranchProperties.setBranchParent(QVCSConstants.QVCS_TRUNK_VIEW);
        opaqueBranchProperties.setBranchDate(new Date());
        opaqueBranchProjectView = new ProjectView();
        opaqueBranchProjectView.setProjectName(getProjectName());
        opaqueBranchProjectView.setViewName(getOpaqueBranchName());
        opaqueBranchProjectView.setRemoteViewProperties(opaqueBranchProperties);
        ViewManager.getInstance().addView(opaqueBranchProjectView);
    }

    static private void initializeTranslucentBranch() throws QVCSException {
        Properties projectProperties = new Properties();
        translucentBranchProperties = new RemoteViewProperties(getProjectName(), getTranslucentBranchName(), projectProperties);
        translucentBranchProperties.setIsReadOnlyViewFlag(false);
        translucentBranchProperties.setIsDateBasedViewFlag(false);
        translucentBranchProperties.setIsTranslucentBranchFlag(true);
        translucentBranchProperties.setIsOpaqueBranchFlag(false);
        translucentBranchProperties.setBranchParent(QVCSConstants.QVCS_TRUNK_VIEW);
        translucentBranchProperties.setBranchDate(new Date());
        translucentBranchProjectView = new ProjectView();
        translucentBranchProjectView.setProjectName(getProjectName());
        translucentBranchProjectView.setViewName(getTranslucentBranchName());
        translucentBranchProjectView.setRemoteViewProperties(translucentBranchProperties);
        ViewManager.getInstance().addView(translucentBranchProjectView);
    }

    static private String getProjectName() {
        return TestHelper.getTestProjectName();
    }

    static private String getDateBasedViewName() {
        return "now";
    }

    static private String getOpaqueBranchName() {
        return "O-2.2.1";
    }

    static private String getTranslucentBranchName() {
        return "T-2.2.2";
    }

    /**
     * Test of getProjectName method, of class DirectoryContentsManager.
     */
    @Test
    public void testGetProjectName() {
        System.out.println("getProjectName");
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
    @Test
    public void testAddFileToTrunk() throws Exception {
        System.out.println("testAddFileToTrunk");
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
    @Test
    public void testAddFileToOpaqueBranch() throws Exception {
        System.out.println("testAddFileToOpaqueBranch");
        String branchName = getOpaqueBranchName();
        int directoryID = 1;
        int fileID = 1001;
        String shortWorkfileName = "testAddFileToOpaqueBranch.java";
        DirectoryContentsManager instance = directoryContentsManager;
        instance.addFileToOpaqueBranch(branchName, directoryID, fileID, shortWorkfileName, bogusResponseObject);
        DirectoryContents directoryContents = instance.getDirectoryContentsForOpaqueBranch(opaqueBranchProjectView, ROOT_DIRECTORY_APPENDED_PATH, directoryID, bogusResponseObject);
        String fileName = directoryContents.getFiles().get(fileID);
        ServerTransactionManager.getInstance().clientEndTransaction(bogusResponseObject);
        assertEquals(shortWorkfileName, fileName);

        // Go back and look again... this one should actually have to go to disk to find the directory contents object.
        ServerTransactionManager.getInstance().clientBeginTransaction(bogusResponseObject);
        directoryContents = instance.getDirectoryContentsForOpaqueBranch(opaqueBranchProjectView, ROOT_DIRECTORY_APPENDED_PATH, directoryID, bogusResponseObject);
        fileName = directoryContents.getFiles().get(fileID);
        ServerTransactionManager.getInstance().clientEndTransaction(bogusResponseObject);
        assertEquals(shortWorkfileName, fileName);
    }

    /**
     * Test of addFile method, of class DirectoryContentsManager. This test adds a file to an opaque branch.
     *
     * @throws Exception if there is a problem.
     */
    @Test
    public void testAddFileToOpaqueBranchBadBranchName() throws Exception {
        System.out.println("testAddFileToOpaqueBranchBadBranchName");
        String branchName = "Bogus Opaque Branch";
        int directoryID = 1;
        int fileID = 1001;
        String shortWorkfileName = "testAddFileToOpaqueBranch.java";
        DirectoryContentsManager instance = directoryContentsManager;
        boolean caughtExpectedException = false;
        try {
            instance.addFileToOpaqueBranch(branchName, directoryID, fileID, shortWorkfileName, bogusResponseObject);
        } catch (QVCSException e) {
            caughtExpectedException = true;
        }
        assertTrue(caughtExpectedException);
    }

    /**
     * Test of addFile method, of class DirectoryContentsManager. This test adds a file to a translucent branch.
     *
     * @throws Exception if there is a problem.
     */
    @Test
    public void testAddFileToTranslucentBranch() throws Exception {
        System.out.println("testAddFileToTranslucentBranch");
        String branchName = getTranslucentBranchName();
        int directoryID = 1;
        int fileID = 1002;
        String shortWorkfileName = "testAddFileToTranslucentBranch.java";
        DirectoryContentsManager instance = directoryContentsManager;
        instance.addFileToTranslucentBranch(branchName, directoryID, fileID, shortWorkfileName, bogusResponseObject);
        DirectoryContents directoryContents = instance.getDirectoryContentsForTranslucentBranch(translucentBranchProjectView, ROOT_DIRECTORY_APPENDED_PATH, directoryID, bogusResponseObject);
        String fileName = directoryContents.getFiles().get(fileID);
        ServerTransactionManager.getInstance().clientEndTransaction(bogusResponseObject);
        assertEquals(shortWorkfileName, fileName);

        // Go back and look again... this one should actually have to go to disk to find the directory contents object.
        ServerTransactionManager.getInstance().clientBeginTransaction(bogusResponseObject);
        directoryContents = instance.getDirectoryContentsForTranslucentBranch(translucentBranchProjectView, ROOT_DIRECTORY_APPENDED_PATH, directoryID, bogusResponseObject);
        fileName = directoryContents.getFiles().get(fileID);
        ServerTransactionManager.getInstance().clientEndTransaction(bogusResponseObject);
        assertEquals(shortWorkfileName, fileName);
    }

    /**
     * Test of addFile method, of class DirectoryContentsManager. This test adds a file to a translucent branch.
     *
     * @throws Exception if there is a problem.
     */
    @Test
    public void testAddFileToTranslucentBranchBadBranchName() throws Exception {
        System.out.println("testAddFileToTranslucentBranchBadBranchName");
        String branchName = "Bogus Branch Name";
        int directoryID = 1;
        int fileID = 1002;
        String shortWorkfileName = "testAddFileToTranslucentBranch.java";
        DirectoryContentsManager instance = directoryContentsManager;
        boolean caughtExpectedException = false;
        try {
            instance.addFileToTranslucentBranch(branchName, directoryID, fileID, shortWorkfileName, bogusResponseObject);
        } catch (QVCSException e) {
            caughtExpectedException = true;
        }
        assertTrue(caughtExpectedException);
    }

    /**
     * Test of renameFile method, of class DirectoryContentsManager on the Trunk view.
     *
     * @throws Exception if there is a problem.
     */
    @Test
    public void testRenameFileOnTrunk() throws Exception {
        System.out.println("testRenameFileOnTrunk");
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
    @Test
    public void testRenameFileOnOpaqueBranch() throws Exception {
        System.out.println("testRenameFileOnOpaqueBranch");
        String viewName = getOpaqueBranchName();
        int directoryID = 1;
        int fileID = 1001;
        String oldWorkfileName = "testAddFileToOpaqueBranch.java";
        String newWorkfileName = "newTestAddFileToOpaqueBranch.java";
        DirectoryContentsManager instance = directoryContentsManager;
        instance.renameFileOnOpaqueBranch(viewName, fileID, oldWorkfileName, newWorkfileName, bogusResponseObject);
        DirectoryContents directoryContents = instance.getDirectoryContentsForOpaqueBranch(opaqueBranchProjectView, ROOT_DIRECTORY_APPENDED_PATH, directoryID, bogusResponseObject);
        String fileName = directoryContents.getFiles().get(fileID);
        ServerTransactionManager.getInstance().clientEndTransaction(bogusResponseObject);
        assertEquals(newWorkfileName, fileName);

        // Go back and look again... this one should actually have to go to disk to find the directory contents object.
        ServerTransactionManager.getInstance().clientBeginTransaction(bogusResponseObject);
        directoryContents = instance.getDirectoryContentsForOpaqueBranch(opaqueBranchProjectView, ROOT_DIRECTORY_APPENDED_PATH, directoryID, bogusResponseObject);
        fileName = directoryContents.getFiles().get(fileID);
        ServerTransactionManager.getInstance().clientEndTransaction(bogusResponseObject);
        assertEquals(newWorkfileName, fileName);
    }

    /**
     * Test of renameFile method, of class DirectoryContentsManager on the opaque branch.
     *
     * @throws Exception if there is a problem.
     */
    @Test
    public void testRenameFileOnOpaqueBranchBadBranchName() throws Exception {
        System.out.println("testRenameFileOnOpaqueBranchBadBranchName");
        String viewName = "Bogus Branch Name";
        int fileID = 1001;
        String oldWorkfileName = "testAddFileToOpaqueBranch.java";
        String newWorkfileName = "newTestAddFileToOpaqueBranch.java";
        DirectoryContentsManager instance = directoryContentsManager;
        boolean caughtExpectedException = false;
        try {
            instance.renameFileOnOpaqueBranch(viewName, fileID, oldWorkfileName, newWorkfileName, bogusResponseObject);
        } catch (QVCSException e) {
            caughtExpectedException = true;
        }
        assertTrue(caughtExpectedException);
    }

    /**
     * Test of renameFile method, of class DirectoryContentsManager on the translucent branch.
     *
     * @throws Exception if there is a problem.
     */
    @Test
    public void testRenameFileOnTranslucentBranch() throws Exception {
        System.out.println("testRenameFileOnTranslucentBranch");
        String viewName = getTranslucentBranchName();
        int directoryID = 1;
        int fileID = 1002;
        String oldWorkfileName = "testAddFileToTranslucentBranch.java";
        String newWorkfileName = "newTestAddFileToTranslucentBranch.java";
        DirectoryContentsManager instance = directoryContentsManager;
        instance.renameFileOnTranslucentBranch(viewName, fileID, oldWorkfileName, newWorkfileName, bogusResponseObject);
        DirectoryContents directoryContents = instance.getDirectoryContentsForTranslucentBranch(translucentBranchProjectView, ROOT_DIRECTORY_APPENDED_PATH, directoryID, bogusResponseObject);
        String fileName = directoryContents.getFiles().get(fileID);
        ServerTransactionManager.getInstance().clientEndTransaction(bogusResponseObject);
        assertEquals(newWorkfileName, fileName);

        // Go back and look again... this one should actually have to go to disk to find the directory contents object.
        ServerTransactionManager.getInstance().clientBeginTransaction(bogusResponseObject);
        directoryContents = instance.getDirectoryContentsForTranslucentBranch(translucentBranchProjectView, ROOT_DIRECTORY_APPENDED_PATH, directoryID, bogusResponseObject);
        fileName = directoryContents.getFiles().get(fileID);
        ServerTransactionManager.getInstance().clientEndTransaction(bogusResponseObject);
        assertEquals(newWorkfileName, fileName);
    }

    /**
     * Test of renameFile method, of class DirectoryContentsManager on the translucent branch.
     *
     * @throws Exception if there is a problem.
     */
    @Test
    public void testRenameFileOnTranslucentBranchBadBranchName() throws Exception {
        System.out.println("testRenameFileOnTranslucentBranchBadBranchName");
        String viewName = "Bogus Branch Name";
        int fileID = 1002;
        String oldWorkfileName = "testAddFileToTranslucentBranch.java";
        String newWorkfileName = "newTestAddFileToTranslucentBranch.java";
        DirectoryContentsManager instance = directoryContentsManager;
        boolean caughtExpectedException = false;
        try {
            instance.renameFileOnTranslucentBranch(viewName, fileID, oldWorkfileName, newWorkfileName, bogusResponseObject);
        } catch (QVCSException e) {
            caughtExpectedException = true;
        }
        assertTrue(caughtExpectedException);
    }

    /**
     * Test of moveFile method, of class DirectoryContentsManager. This one tests moves on the Trunk.
     *
     * @throws Exception if there is a problem.
     */
    @Test
    public void testMoveFileOnTrunk() throws Exception {
        System.out.println("testMoveFileOnTrunk");
        String viewName = QVCSConstants.QVCS_TRUNK_VIEW;
        int originDirectoryID = 1;
        int destinationDirectoryID = 2;
        int fileID = 1000;
        String workfileName = "newTestAddFileName.java";
        DirectoryContentsManager instance = directoryContentsManager;
        instance.moveFileOnTrunk(viewName, originDirectoryID, destinationDirectoryID, fileID, bogusResponseObject);
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
    @Test
    public void testMoveFileOnOpaqueBranch() throws Exception {
        System.out.println("testMoveFileOnOpaqueBranch");
        String viewName = getOpaqueBranchName();
        int originDirectoryID = 1;
        int destinationDirectoryID = 2;
        int fileID = 1001;
        String workfileName = "newTestAddFileToOpaqueBranch.java";
        DirectoryContentsManager instance = directoryContentsManager;
        instance.moveFileOnOpaqueBranch(viewName, originDirectoryID, destinationDirectoryID, fileID, bogusResponseObject);
        DirectoryContents directoryContents = instance.getDirectoryContentsForOpaqueBranch(opaqueBranchProjectView, SUB_DIRECTORY_1_APPENDED_PATH, destinationDirectoryID, bogusResponseObject);
        String fileName = directoryContents.getFiles().get(fileID);
        ServerTransactionManager.getInstance().clientEndTransaction(bogusResponseObject);
        assertEquals(workfileName, fileName);

        // Go back and look again... this one should actually have to go to disk to find the directory contents object.
        ServerTransactionManager.getInstance().clientBeginTransaction(bogusResponseObject);
        directoryContents = instance.getDirectoryContentsForOpaqueBranch(opaqueBranchProjectView, SUB_DIRECTORY_1_APPENDED_PATH, destinationDirectoryID, bogusResponseObject);
        fileName = directoryContents.getFiles().get(fileID);
        ServerTransactionManager.getInstance().clientEndTransaction(bogusResponseObject);
        assertEquals(workfileName, fileName);
    }

    /**
     * Test of moveFile method, of class DirectoryContentsManager. This one tests moves on an opaque branch.
     *
     * @throws Exception if there is a problem.
     */
    @Test
    public void testMoveFileOnOpaqueBranchBadBranchName() throws Exception {
        System.out.println("testMoveFileOnOpaqueBranchBadBranchName");
        String viewName = "Bogus Branch Name";
        int originDirectoryID = 1;
        int destinationDirectoryID = 2;
        int fileID = 1001;
        DirectoryContentsManager instance = directoryContentsManager;
        boolean caughtExpectedException = false;
        try {
            instance.moveFileOnOpaqueBranch(viewName, originDirectoryID, destinationDirectoryID, fileID, bogusResponseObject);
        } catch (QVCSException e) {
            caughtExpectedException = true;
        }
        assertTrue(caughtExpectedException);
    }

    /**
     * Test of moveFile method, of class DirectoryContentsManager. This one tests moves on an translucent branch.
     *
     * @throws Exception if there is a problem.
     */
    @Test
    public void testMoveFileOnTranslucentBranch() throws Exception {
        System.out.println("testMoveFileOnTranslucentBranch");
        String viewName = getTranslucentBranchName();
        int originDirectoryID = 1;
        int destinationDirectoryID = 2;
        int fileID = 1002;
        String workfileName = "newTestAddFileToTranslucentBranch.java";
        DirectoryContentsManager instance = directoryContentsManager;
        instance.moveFileOnTranslucentBranch(viewName, originDirectoryID, destinationDirectoryID, fileID, bogusResponseObject);
        DirectoryContents directoryContents = instance.getDirectoryContentsForTranslucentBranch(translucentBranchProjectView, SUB_DIRECTORY_1_APPENDED_PATH, destinationDirectoryID,
                bogusResponseObject);
        String fileName = directoryContents.getFiles().get(fileID);
        ServerTransactionManager.getInstance().clientEndTransaction(bogusResponseObject);
        assertEquals(workfileName, fileName);

        // Go back and look again... this one should actually have to go to disk to find the directory contents object.
        ServerTransactionManager.getInstance().clientBeginTransaction(bogusResponseObject);
        directoryContents = instance.getDirectoryContentsForTranslucentBranch(translucentBranchProjectView, SUB_DIRECTORY_1_APPENDED_PATH, destinationDirectoryID, bogusResponseObject);
        fileName = directoryContents.getFiles().get(fileID);
        ServerTransactionManager.getInstance().clientEndTransaction(bogusResponseObject);
        assertEquals(workfileName, fileName);
    }

    /**
     * Test of moveFile method, of class DirectoryContentsManager. This one tests moves on an translucent branch.
     *
     * @throws Exception if there is a problem.
     */
    @Test
    public void testMoveFileOnTranslucentBranchBadBranchName() throws Exception {
        System.out.println("testMoveFileOnTranslucentBranchBadBranchName");
        String viewName = "Bogus Branch Name";
        int originDirectoryID = 1;
        int destinationDirectoryID = 2;
        int fileID = 1002;
        DirectoryContentsManager instance = directoryContentsManager;
        boolean caughtExpectedException = false;
        try {
            instance.moveFileOnTranslucentBranch(viewName, originDirectoryID, destinationDirectoryID, fileID, bogusResponseObject);
        } catch (QVCSException e) {
            caughtExpectedException = true;
        }
        assertTrue(caughtExpectedException);
    }

    /**
     * Test of deleteFile method, of class DirectoryContentsManager. This one tests deletes on the Trunk.
     *
     * @throws Exception if there is a problem.
     */
    @Test
    public void testDeleteFileOnTrunk() throws Exception {
        System.out.println("testDeleteFileOnTrunk");
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
            assertTrue(cemeteryFileName != null);
            assertTrue(null == originDirectoryContents.getFiles().get(fileID));
            assertEquals(cemeteryShortFileName, cemeteryFileName);

            // Go back and look again... this one should actually have to go to disk to find the directory contents object.
            ServerTransactionManager.getInstance().clientBeginTransaction(bogusResponseObject);
            originDirectoryContents = instance.getDirectoryContentsForTrunk(SUB_DIRECTORY_1_APPENDED_PATH, originDirectoryID, bogusResponseObject);
            instance.getDirectoryContentsForTrunk(QVCSConstants.QVCS_CEMETERY_DIRECTORY, cemeteryDirectoryID, bogusResponseObject);
            cemeteryShortArchiveName = Utility.createCemeteryShortArchiveName(fileID);
            cemeteryShortFileName = Utility.convertArchiveNameToShortWorkfileName(cemeteryShortArchiveName);
            ServerTransactionManager.getInstance().clientEndTransaction(bogusResponseObject);
            assertTrue(cemeteryFileName != null);
            assertTrue(null == originDirectoryContents.getFiles().get(fileID));
            assertEquals(cemeteryShortFileName, cemeteryFileName);
        } catch (NullPointerException e) {
            String message = Utility.expandStackTraceToString(e);
            fail("Unexpected null pointer exception: " + message);
        } catch (QVCSException e) {
            String message = Utility.expandStackTraceToString(e);
            fail("Unexpected QVCS exception: " + message);
        }
    }

    /**
     * Test of deleteFile method, of class DirectoryContentsManager. This one tests deletes on an opaque branch.
     *
     * @throws Exception if there is a problem.
     */
    @Test
    public void testDeleteFileOnOpaqueBranch() throws Exception {
        System.out.println("testDeleteFileOnOpaqueBranch");
        String viewName = getOpaqueBranchName();
        int originDirectoryID = 2;

        int fileID = 1001;
        String workfileName = "newTestAddFileToOpaqueBranch.java";
        boolean noExceptionsFlag = false;
        try {
            DirectoryContentsManager instance = directoryContentsManager;
            instance.deleteFileFromOpaqueBranch(viewName, originDirectoryID, -1, fileID, workfileName, bogusResponseObject);
            DirectoryContents originDirectoryContents = instance.getDirectoryContentsForOpaqueBranch(opaqueBranchProjectView, SUB_DIRECTORY_1_APPENDED_PATH, originDirectoryID, bogusResponseObject);
            ServerTransactionManager.getInstance().clientEndTransaction(bogusResponseObject);
            assertTrue(null == originDirectoryContents.getFiles().get(fileID));

            // Go back and look again... this one should actually have to go to disk to find the directory contents object.
            ServerTransactionManager.getInstance().clientBeginTransaction(bogusResponseObject);
            originDirectoryContents = instance.getDirectoryContentsForOpaqueBranch(opaqueBranchProjectView, SUB_DIRECTORY_1_APPENDED_PATH, originDirectoryID, bogusResponseObject);
            String fileName = originDirectoryContents.getFiles().get(fileID);
            ServerTransactionManager.getInstance().clientEndTransaction(bogusResponseObject);
            assertEquals(null, fileName);
            noExceptionsFlag = true;
        } catch (NullPointerException e) {
            fail("Unexpected null pointer exception");
        } catch (QVCSException e) {
            fail("Unexpected QVCS exception");
        } finally {
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
    @Test
    public void testDeleteFileOnOpaqueBranchBadBranchName() throws Exception {
        System.out.println("testDeleteFileOnOpaqueBranchBadBranchName");
        String viewName = "Bogus Branch Name";
        int originDirectoryID = 2;

        int fileID = 1001;
        String workfileName = "newTestAddFileToOpaqueBranch.java";
        boolean caughtExpectedException = false;
        try {
            DirectoryContentsManager instance = directoryContentsManager;
            instance.deleteFileFromOpaqueBranch(viewName, originDirectoryID, -1, fileID, workfileName, bogusResponseObject);
        } catch (QVCSException e) {
            caughtExpectedException = true;
        }
        assertTrue(caughtExpectedException);
    }

    /**
     * Test of deleteFile method, of class DirectoryContentsManager. This one tests deletes on a translucent branch.
     *
     * @throws Exception if there is a problem.
     */
    @Test
    public void testDeleteFileOnTranslucentBranch() throws Exception {
        System.out.println("testDeleteFileOnTranslucentBranch");
        String viewName = getTranslucentBranchName();
        int originDirectoryID = 2;

        int fileID = 1002;
        String workfileName = "newTestAddFileToTranslucentBranch.java";
        boolean noExceptionsFlag = false;
        try {
            DirectoryContentsManager instance = directoryContentsManager;
            instance.deleteFileFromTranslucentBranch(viewName, originDirectoryID, -1, fileID, workfileName, bogusResponseObject);
            DirectoryContents originDirectoryContents = instance.getDirectoryContentsForTranslucentBranch(translucentBranchProjectView, SUB_DIRECTORY_1_APPENDED_PATH,
                    originDirectoryID, bogusResponseObject);
            ServerTransactionManager.getInstance().clientEndTransaction(bogusResponseObject);
            assertTrue(null == originDirectoryContents.getFiles().get(fileID));

            // Go back and look again... this one should actually have to go to disk to find the directory contents object.
            ServerTransactionManager.getInstance().clientBeginTransaction(bogusResponseObject);
            originDirectoryContents = instance.getDirectoryContentsForTranslucentBranch(translucentBranchProjectView, SUB_DIRECTORY_1_APPENDED_PATH, originDirectoryID, bogusResponseObject);
            String fileName = originDirectoryContents.getFiles().get(fileID);
            ServerTransactionManager.getInstance().clientEndTransaction(bogusResponseObject);
            assertEquals(null, fileName);
            noExceptionsFlag = true;
        } catch (NullPointerException e) {
            fail("Unexpected null pointer exception");
        } catch (QVCSException e) {
            fail("Unexpected QVCS exception");
        } finally {
            if (noExceptionsFlag == false) {
                fail("Some unknown exception was thrown");
            }
        }
    }

    /**
     * Test of deleteFile method, of class DirectoryContentsManager. This one tests deletes on a translucent branch.
     *
     * @throws Exception if there is a problem.
     */
    @Test
    public void testDeleteFileOnTranslucentBranchBadBranchName() throws Exception {
        System.out.println("testDeleteFileOnTranslucentBranch");
        String viewName = "Bogus Branch Name";
        int originDirectoryID = 2;

        int fileID = 1002;
        String workfileName = "newTestAddFileToTranslucentBranch.java";
        boolean caughtExpectedException = false;
        try {
            DirectoryContentsManager instance = directoryContentsManager;
            instance.deleteFileFromTranslucentBranch(viewName, originDirectoryID, -1, fileID, workfileName, bogusResponseObject);
        } catch (QVCSException e) {
            caughtExpectedException = true;
        }
        assertTrue(caughtExpectedException);
    }

    /**
     * Test of moveFileFromTranslucentBranchCemetery method, of class DirectoryContentsManager.
     *
     * @throws QVCSException if there is a problem.
     */
    @Test
    public void testMoveFileFromTranslucentBranchCemetery() throws QVCSException {
        System.out.println("testMoveFileFromTranslucentBranchCemetery");
        String viewName = getTranslucentBranchName();
        int destinationDirectoryId = 2;

        int fileId = 1002;
        String workfileName = "newTestAddFileToTranslucentBranch.java";
        boolean noExceptionsFlag = false;
        try {
            DirectoryContentsManager instance = directoryContentsManager;
            instance.moveFileFromTranslucentBranchCemetery(viewName, destinationDirectoryId, fileId, workfileName, bogusResponseObject);
            DirectoryContents originDirectoryContents = instance.getDirectoryContentsForTranslucentBranch(translucentBranchProjectView, SUB_DIRECTORY_1_APPENDED_PATH,
                    destinationDirectoryId, bogusResponseObject);
            ServerTransactionManager.getInstance().clientEndTransaction(bogusResponseObject);
            assertTrue(null != originDirectoryContents.getFiles().get(fileId));

            // Go back and look again... this one should actually have to go to disk to find the directory contents object.
            ServerTransactionManager.getInstance().clientBeginTransaction(bogusResponseObject);
            originDirectoryContents = instance.getDirectoryContentsForTranslucentBranch(translucentBranchProjectView, SUB_DIRECTORY_1_APPENDED_PATH, destinationDirectoryId, bogusResponseObject);
            String fileName = originDirectoryContents.getFiles().get(fileId);
            ServerTransactionManager.getInstance().clientEndTransaction(bogusResponseObject);
            assertNotNull(fileName);
            noExceptionsFlag = true;
        } catch (NullPointerException e) {
            fail("Unexpected null pointer exception");
        } catch (QVCSException e) {
            fail("Unexpected QVCS exception");
        } finally {
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
    @Test
    public void testAddDirectoryOnTrunk() throws Exception {
        System.out.println("testAddDirectoryOnTrunk");
        String viewName = QVCSConstants.QVCS_TRUNK_VIEW;
        int parentDirectoryID = 1;
        int childDirectoryID = 10;
        String childDirectoryName = ADDED_SUB_DIRECTORY_APPENDED_PATH;
        directoryContentsManager.addDirectory(viewName, 1, ROOT_DIRECTORY_APPENDED_PATH, parentDirectoryID, childDirectoryID, childDirectoryName, bogusResponseObject);
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
    @Test
    public void testAddDirectoryOnOpaqueBranch() throws Exception {
        System.out.println("testAddDirectoryOnOpaqueBranch");
        String viewName = getOpaqueBranchName();
        int parentDirectoryID = 1;
        int childDirectoryID = 10;
        String childDirectoryName = ADDED_SUB_DIRECTORY_APPENDED_PATH;
        directoryContentsManager.addDirectory(viewName, 1, ROOT_DIRECTORY_APPENDED_PATH, parentDirectoryID, childDirectoryID, childDirectoryName, bogusResponseObject);
        ServerTransactionManager.getInstance().clientEndTransaction(bogusResponseObject);

        // Go back and look again... this one should actually have to go to disk to find the directory contents object.
        ServerTransactionManager.getInstance().clientBeginTransaction(bogusResponseObject);
        DirectoryContents directoryContents = directoryContentsManager.getDirectoryContentsForOpaqueBranch(opaqueBranchProjectView, ROOT_DIRECTORY_APPENDED_PATH, parentDirectoryID,
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
    @Test
    public void testAddDirectoryOnTranslucentBranch() throws Exception {
        System.out.println("testAddDirectoryOnTranslucentBranch");
        String viewName = getTranslucentBranchName();
        int parentDirectoryID = 1;
        int childDirectoryID = 10;
        String childDirectoryName = ADDED_SUB_DIRECTORY_APPENDED_PATH;
        directoryContentsManager.addDirectory(viewName, 1, ROOT_DIRECTORY_APPENDED_PATH, parentDirectoryID, childDirectoryID, childDirectoryName, bogusResponseObject);
        ServerTransactionManager.getInstance().clientEndTransaction(bogusResponseObject);

        // Go back and look again... this one should actually have to go to disk to find the directory contents object.
        ServerTransactionManager.getInstance().clientBeginTransaction(bogusResponseObject);
        DirectoryContents directoryContents = directoryContentsManager.getDirectoryContentsForTranslucentBranch(translucentBranchProjectView, ROOT_DIRECTORY_APPENDED_PATH, parentDirectoryID,
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
    @Test
    public void testDeleteDirectoryOnTrunk() throws Exception {
        System.out.println("testDeleteDirectoryOnTrunk");
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
    @Test
    public void testDeleteDirectoryOnOpaqueBranch() throws Exception {
        System.out.println("testDeleteDirectoryOnOpaqueBranch");
        String viewName = getOpaqueBranchName();
        int parentDirectoryID = 1;
        int childDirectoryID = 10;
        DirectoryContentsManager instance = directoryContentsManager;
        DirectoryContents directoryContents = instance.getDirectoryContentsForOpaqueBranch(opaqueBranchProjectView, ROOT_DIRECTORY_APPENDED_PATH, parentDirectoryID, bogusResponseObject);
        String directoryName = directoryContents.getChildDirectories().get(childDirectoryID);
        assertNotNull(directoryName);
        instance.deleteDirectoryOnOpaqueBranch(viewName, childDirectoryID, bogusResponseObject);
        ServerTransactionManager.getInstance().clientEndTransaction(bogusResponseObject);

        // Go back and look again... this one should actually have to go to disk to find the directory contents object.
        ServerTransactionManager.getInstance().clientBeginTransaction(bogusResponseObject);
        directoryContents = instance.getDirectoryContentsForOpaqueBranch(opaqueBranchProjectView, ROOT_DIRECTORY_APPENDED_PATH, parentDirectoryID, bogusResponseObject);
        directoryName = directoryContents.getChildDirectories().get(childDirectoryID);
        ServerTransactionManager.getInstance().clientEndTransaction(bogusResponseObject);
        assertEquals(null, directoryName);
    }

    /**
     * Test of deleteDirectory method, of class DirectoryContentsManager.
     *
     * @throws Exception if there is a problem.
     */
    @Test
    public void testDeleteDirectoryOnTranslucentBranch() throws QVCSException {
        System.out.println("testDeleteDirectoryOnTranslucentBranch");
        String viewName = getTranslucentBranchName();
        int parentDirectoryID = 1;
        int childDirectoryID = 10;
        DirectoryContentsManager instance = directoryContentsManager;
        DirectoryContents directoryContents = instance.getDirectoryContentsForTranslucentBranch(translucentBranchProjectView, ROOT_DIRECTORY_APPENDED_PATH, parentDirectoryID, bogusResponseObject);
        String directoryName = directoryContents.getChildDirectories().get(childDirectoryID);
        assertNotNull(directoryName);
        instance.deleteDirectoryOnTranslucentBranch(viewName, childDirectoryID, bogusResponseObject);
        ServerTransactionManager.getInstance().clientEndTransaction(bogusResponseObject);

        // Go back and look again... this one should actually have to go to disk to find the directory contents object.
        ServerTransactionManager.getInstance().clientBeginTransaction(bogusResponseObject);
        directoryContents = instance.getDirectoryContentsForTranslucentBranch(translucentBranchProjectView, ROOT_DIRECTORY_APPENDED_PATH, parentDirectoryID, bogusResponseObject);
        directoryName = directoryContents.getChildDirectories().get(childDirectoryID);
        ServerTransactionManager.getInstance().clientEndTransaction(bogusResponseObject);
        assertEquals(null, directoryName);
    }

    /**
     * Test of getDirectoryIDCollectionForTranslucentBranch method, of class DirectoryContentsManager.
     *
     * @throws Exception if there is a problem.
     */
    @Test
    public void testGetDirectoryIDCollectionForDateBasedView() throws Exception {
        System.out.println("getDirectoryIDCollectionForDateBasedView");
        int directoryId = 1;
        DirectoryContentsManager instance = directoryContentsManager;
        Map<Integer, String> result = instance.getDirectoryIDCollectionForDateBasedView(dateBasedViewProjectView.getViewName(), ROOT_DIRECTORY_APPENDED_PATH, directoryId, bogusResponseObject);
        assertEquals(1, result.size());
        ServerTransactionManager.getInstance().clientEndTransaction(bogusResponseObject);
    }

    /**
     * Test of getDirectoryIDCollectionForTranslucentBranch method, of class DirectoryContentsManager.
     *
     * @throws Exception if there is a problem.
     */
    @Test
    public void testGetDirectoryIDCollectionForTranslucentBranch() throws Exception {
        System.out.println("getDirectoryIDCollectionForTranslucentBranch");
        int directoryId = 1;
        DirectoryContentsManager instance = directoryContentsManager;
        Map<Integer, String> result = instance.getDirectoryIDCollectionForTranslucentBranch(translucentBranchProjectView, ROOT_DIRECTORY_APPENDED_PATH, directoryId, bogusResponseObject);
        assertEquals(1, result.size());
        ServerTransactionManager.getInstance().clientEndTransaction(bogusResponseObject);
    }

    /**
     * Test of getDirectoryIDCollectionForOpaqueBranch method, of class DirectoryContentsManager.
     *
     * @throws Exception if there is a problem.
     */
    @Test
    public void testGetDirectoryIDCollectionForOpaqueBranch() throws Exception {
        System.out.println("getDirectoryIDCollectionForOpaqueBranch");
        int directoryId = 1;
        DirectoryContentsManager instance = directoryContentsManager;
        Map<Integer, String> result = instance.getDirectoryIDCollectionForOpaqueBranch(opaqueBranchProjectView, ROOT_DIRECTORY_APPENDED_PATH, directoryId, bogusResponseObject);
        assertEquals(1, result.size());
        ServerTransactionManager.getInstance().clientEndTransaction(bogusResponseObject);
    }

    /**
     * Test of getPriority method, of class DirectoryContentsManager.
     */
    @Test
    public void testGetPriority() {
        System.out.println("getPriority");
        DirectoryContentsManager instance = directoryContentsManager;
        int expResult = TransactionParticipantInterface.HIGH_PRIORITY;
        int result = instance.getPriority();
        assertEquals(expResult, result);
        ServerTransactionManager.getInstance().clientEndTransaction(bogusResponseObject);
    }
}
