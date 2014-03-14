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
package com.qumasoft.server;

import com.qumasoft.TestHelper;
import com.qumasoft.qvcslib.AbstractProjectProperties;
import com.qumasoft.qvcslib.ArchiveDirManagerInterface;
import com.qumasoft.qvcslib.ArchiveInfoInterface;
import com.qumasoft.qvcslib.DirectoryManagerInterface;
import com.qumasoft.qvcslib.LogFileOperationCreateArchiveCommandArgs;
import com.qumasoft.qvcslib.LogfileActionSetCommentPrefix;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.QVCSException;
import com.qumasoft.qvcslib.RemoteViewProperties;
import com.qumasoft.qvcslib.ServerResponseFactoryInterface;
import java.io.File;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

/**
 * Unit test class for ArchiveDirManagerForTranslucentBranch.
 *
 * @author Jim Voris
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ArchiveDirManagerForTranslucentBranchServerTest {

    private static ProjectView translucentProjectView = null;
    private static RemoteViewProperties translucentBranchProperties = null;
    private ServerResponseFactoryInterface bogusResponseObject = null;
    private ArchiveDirManagerForTranslucentBranch archiveDirManagerForTranslucentBranch = null;
    private static final String TEST_USER_NAME = "Test User";
    private static final String TEST_SERVER_NAME = "Test Server";
    private static final String APPENDED_PATH = TestHelper.SUBPROJECT_APPENDED_PATH;
    private static Object serverSyncObject = null;

    /**
     * Default ctor.
     */
    public ArchiveDirManagerForTranslucentBranchServerTest() {
    }

    /**
     * Set up stuff once for these tests.
     *
     * @throws java.lang.Exception if anything goes wrong.
     */
    @BeforeClass
    public static void setUpClass() throws Exception {
        TestHelper.stopServerImmediately(null);
        TestHelper.removeArchiveFiles();
        TestHelper.deleteViewStore();
        TestHelper.initProjectProperties();
        TestHelper.initializeArchiveFiles();
        serverSyncObject = TestHelper.startServer();
        initializeTranslucentBranch();
    }

    /**
     * Tear down the class level stuff.
     *
     * @throws java.lang.Exception if anything goes wrong.
     */
    @AfterClass
    public static void tearDownClass() throws Exception {
        TestHelper.stopServer(serverSyncObject);
        TestHelper.deleteViewStore();
        TestHelper.removeArchiveFiles();
    }

    /**
     * Run before each test.
     */
    @Before
    public void setUp() {
        bogusResponseObject = new BogusResponseObject();
        String branchParent = translucentBranchProperties.getBranchParent();
        ServerTransactionManager.getInstance().flushClientTransaction(bogusResponseObject);
        ServerTransactionManager.getInstance().clientBeginTransaction(bogusResponseObject);
        archiveDirManagerForTranslucentBranch = new ArchiveDirManagerForTranslucentBranch(branchParent, translucentBranchProperties, getTranslucentBranchName(), APPENDED_PATH,
                TEST_USER_NAME, bogusResponseObject);
    }

    /**
     * Run after each test.
     */
    @After
    public void tearDown() {
        ServerTransactionManager.getInstance().clientEndTransaction(bogusResponseObject);
    }

    static private void initializeTranslucentBranch() throws QVCSException {
        System.out.println("---Begin TestHelper.initializeTranslucentBranch " + TestHelper.addThreadAndTimeStamp());
        Properties projectProperties = new Properties();
        translucentBranchProperties = new RemoteViewProperties(getProjectName(), getTranslucentBranchName(), projectProperties);
        translucentBranchProperties.setIsReadOnlyViewFlag(false);
        translucentBranchProperties.setIsDateBasedViewFlag(false);
        translucentBranchProperties.setIsTranslucentBranchFlag(true);
        translucentBranchProperties.setIsOpaqueBranchFlag(false);
        translucentBranchProperties.setBranchParent(QVCSConstants.QVCS_TRUNK_VIEW);
        translucentBranchProperties.setBranchDate(new Date());
        translucentProjectView = new ProjectView();
        translucentProjectView.setProjectName(getProjectName());
        translucentProjectView.setViewName(getTranslucentBranchName());
        translucentProjectView.setRemoteViewProperties(translucentBranchProperties);
        ViewManager.getInstance().initialize();
        ViewManager.getInstance().addView(translucentProjectView);
        System.out.println("------End TestHelper.initializeTranslucentBranch " + TestHelper.addThreadAndTimeStamp());
    }

    static private String getProjectName() {
        return TestHelper.getTestProjectName();
    }

    static private String getTranslucentBranchName() {
        return "2.2.2";
    }

    /**
     * Test of setDirectoryManager method, of class ArchiveDirManagerForTranslucentBranch.
     */
    @Test
    public void test01SetDirectoryManager() {
        System.out.println("setDirectoryManager " + TestHelper.addThreadAndTimeStamp());
        DirectoryManagerInterface directoryManager = null;
        ArchiveDirManagerForTranslucentBranch instance = archiveDirManagerForTranslucentBranch;
        instance.setDirectoryManager(directoryManager);
    }

    /**
     * Test of getAppendedPath method, of class ArchiveDirManagerForTranslucentBranch.
     */
    @Test
    public void test02GetAppendedPath() {
        System.out.println("getAppendedPath " + TestHelper.addThreadAndTimeStamp());
        ArchiveDirManagerForTranslucentBranch instance = archiveDirManagerForTranslucentBranch;
        String expResult = APPENDED_PATH;
        String result = instance.getAppendedPath();
        assertEquals(expResult, result);
    }

    /**
     * Test of getProjectName method, of class ArchiveDirManagerForTranslucentBranch.
     */
    @Test
    public void test03GetProjectName() {
        System.out.println("getProjectName " + TestHelper.addThreadAndTimeStamp());
        ArchiveDirManagerForTranslucentBranch instance = archiveDirManagerForTranslucentBranch;
        String expResult = getProjectName();
        String result = instance.getProjectName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getViewName method, of class ArchiveDirManagerForTranslucentBranch.
     */
    @Test
    public void test04GetViewName() {
        System.out.println("getViewName " + TestHelper.addThreadAndTimeStamp());
        ArchiveDirManagerForTranslucentBranch instance = archiveDirManagerForTranslucentBranch;
        String expResult = getTranslucentBranchName();
        String result = instance.getViewName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getUserName method, of class ArchiveDirManagerForTranslucentBranch.
     */
    @Test
    public void test05GetUserName() {
        System.out.println("getUserName " + TestHelper.addThreadAndTimeStamp());
        ArchiveDirManagerForTranslucentBranch instance = archiveDirManagerForTranslucentBranch;
        String expResult = TEST_USER_NAME;
        String result = instance.getUserName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getBranchParent method, of class ArchiveDirManagerForTranslucentBranch.
     */
    @Test
    public void test06GetBranchParent() {
        System.out.println("getBranchParent " + TestHelper.addThreadAndTimeStamp());
        ArchiveDirManagerForTranslucentBranch instance = archiveDirManagerForTranslucentBranch;
        String expResult = QVCSConstants.QVCS_TRUNK_VIEW;
        String result = instance.getBranchParent();
        assertEquals(expResult, result);
    }

    /**
     * Test of getProjectProperties method, of class ArchiveDirManagerForTranslucentBranch.
     */
    @Test
    public void test07GetProjectProperties() {
        System.out.println("getProjectProperties " + TestHelper.addThreadAndTimeStamp());
        ArchiveDirManagerForTranslucentBranch instance = archiveDirManagerForTranslucentBranch;
        AbstractProjectProperties expResult = translucentBranchProperties;
        AbstractProjectProperties result = instance.getProjectProperties();
        assertEquals(expResult.getAttributes().getAttributesAsInt(), result.getAttributes().getAttributesAsInt());
    }

    /**
     * Test of getArchiveInfo method, of class ArchiveDirManagerForTranslucentBranch.
     */
    @Test
    public void test08GetArchiveInfo() {
        System.out.println("getArchiveInfo " + TestHelper.addThreadAndTimeStamp());
        String shortWorkfileName = "QVCSEnterpriseServer.java";
        ArchiveDirManagerForTranslucentBranch instance = archiveDirManagerForTranslucentBranch;
        String expResult = "1.91";
        ArchiveInfoInterface result = instance.getArchiveInfo(shortWorkfileName);
        assertEquals(expResult, result.getDefaultRevisionString());
        assertEquals("JimVoris", result.getLastEditBy());
        assertEquals(92, result.getRevisionCount());
    }

    /**
     * Test of createArchive method, of class ArchiveDirManagerForTranslucentBranch.
     *
     * @throws Exception if there was a problem.
     */
    @Test
    public void test09CreateArchive() throws Exception {
        System.out.println("createArchive " + TestHelper.addThreadAndTimeStamp());
        LogFileOperationCreateArchiveCommandArgs commandLineArgs = new LogFileOperationCreateArchiveCommandArgs();
        String workfileName = "TestCheckInArchive.java";
        commandLineArgs.setArchiveDescription("New archive on translucent branch.");
        commandLineArgs.setWorkfileName(workfileName);
        commandLineArgs.setUserName("JimVoris");
        commandLineArgs.setInputfileTimeStamp(new Date());
        String fullWorkfilename = System.getProperty(TestHelper.USER_DIR) + File.separator + "TestCheckInArchive.java";
        ServerResponseFactoryInterface response = bogusResponseObject;
        ArchiveDirManagerForTranslucentBranch instance = archiveDirManagerForTranslucentBranch;
        boolean expResult = true;
        boolean result = instance.createArchive(commandLineArgs, fullWorkfilename, response);
        assertEquals(expResult, result);
        DirectoryContentsManager directoryContentsManager = DirectoryContentsManagerFactory.getInstance().getDirectoryContentsManager(TestHelper.getTestProjectName());
        DirectoryContents directoryContents = directoryContentsManager.getDirectoryContentsForTranslucentBranch(translucentProjectView,
                archiveDirManagerForTranslucentBranch.getAppendedPath(),
                archiveDirManagerForTranslucentBranch.getDirectoryID(),
                bogusResponseObject);
        Map<Integer, String> fileNameMap = directoryContents.getFiles();
        boolean fileNameFoundFlag = false;
        int fileID = -1;
        for (Integer key : fileNameMap.keySet()) {
            String fileName = fileNameMap.get(key);
            System.out.println("File: " + fileName);
            if (0 == fileName.compareTo(workfileName)) {
                fileNameFoundFlag = true;
                fileID = key.intValue();
            }
        }
        assertEquals(true, fileNameFoundFlag);
        ServerTransactionManager.getInstance().clientEndTransaction(bogusResponseObject);
        ServerTransactionManager.getInstance().clientBeginTransaction(bogusResponseObject);
        FileIDInfo fileIDInfo = FileIDDictionary.getInstance().lookupFileIDInfo(getProjectName(), getTranslucentBranchName(), fileID);
        int directoryID = fileIDInfo.getDirectoryID();
        assertEquals(archiveDirManagerForTranslucentBranch.getDirectoryID(), directoryID);
    }

    /**
     * Test of createReferenceCopy method, of class ArchiveDirManagerForTranslucentBranch.
     */
    @Test
    public void test10CreateReferenceCopy() {
        System.out.println("createReferenceCopy " + TestHelper.addThreadAndTimeStamp());
        AbstractProjectProperties projectProperties = null;
        ArchiveInfoInterface logfile = null;
        byte[] buffer = null;
        ArchiveDirManagerForTranslucentBranch instance = archiveDirManagerForTranslucentBranch;
        instance.createReferenceCopy(projectProperties, logfile, buffer);
    }

    /**
     * Test of deleteReferenceCopy method, of class ArchiveDirManagerForTranslucentBranch.
     */
    @Test
    public void test11DeleteReferenceCopy() {
        System.out.println("deleteReferenceCopy " + TestHelper.addThreadAndTimeStamp());
        AbstractProjectProperties projectProperties = null;
        ArchiveInfoInterface logfile = null;
        ArchiveDirManagerForTranslucentBranch instance = archiveDirManagerForTranslucentBranch;
        instance.deleteReferenceCopy(projectProperties, logfile);
    }

    /**
     * Test of moveArchive method, of class ArchiveDirManagerForTranslucentBranch.
     *
     * @throws Exception if something goes wrong.
     */
    @Test
    public void test12MoveArchive() throws Exception {
        System.out.println("moveArchive " + TestHelper.addThreadAndTimeStamp());
        String userName = TEST_USER_NAME;
        String shortWorkfileName = "TestCheckInArchive.java";
        String branchParent = translucentBranchProperties.getBranchParent();
        ArchiveDirManagerInterface targetArchiveDirManager = new ArchiveDirManagerForTranslucentBranch(branchParent, translucentBranchProperties, getTranslucentBranchName(),
                TestHelper.SUBPROJECT2_APPENDED_PATH, TEST_USER_NAME, bogusResponseObject);
        ServerResponseFactoryInterface response = bogusResponseObject;
        ArchiveDirManagerForTranslucentBranch instance = archiveDirManagerForTranslucentBranch;
        boolean expResult = true;
        boolean result = instance.moveArchive(userName, shortWorkfileName, targetArchiveDirManager, response);
        assertEquals(expResult, result);

        DirectoryContentsManager directoryContentsManager = DirectoryContentsManagerFactory.getInstance().getDirectoryContentsManager(TestHelper.getTestProjectName());
        DirectoryContents originDirectoryContents = directoryContentsManager.getDirectoryContentsForTranslucentBranch(translucentProjectView,
                instance.getAppendedPath(),
                instance.getDirectoryID(),
                bogusResponseObject);
        Map<Integer, String> fileNameMap = originDirectoryContents.getFiles();
        boolean fileNameFoundFlag = false;
        for (Integer key : fileNameMap.keySet()) {
            String fileName = fileNameMap.get(key);
            System.out.println("File: " + fileName);
            if (0 == fileName.compareTo(shortWorkfileName)) {
                fileNameFoundFlag = true;
            }
        }
        assertEquals(false, fileNameFoundFlag);
        ArchiveInfoInterface archiveInfo = instance.getArchiveInfo(shortWorkfileName);
        assertEquals(null, archiveInfo);
        archiveInfo = targetArchiveDirManager.getArchiveInfo(shortWorkfileName);
        assertNotNull(archiveInfo);
        int fileID = archiveInfo.getFileID();
        ServerTransactionManager.getInstance().clientEndTransaction(bogusResponseObject);
        ServerTransactionManager.getInstance().clientBeginTransaction(bogusResponseObject);
        FileIDInfo fileIDInfo = FileIDDictionary.getInstance().lookupFileIDInfo(getProjectName(), getTranslucentBranchName(), fileID);
        // This should still point to the original directory, since we did not move the actual archive file!!
        assertEquals(targetArchiveDirManager.getDirectoryID(), fileIDInfo.getDirectoryID());
    }

    /**
     * Test of renameArchive method, of class ArchiveDirManagerForTranslucentBranch.
     *
     * @throws Exception if something goes wrong.
     */
    @Test
    public void test13RenameArchive() throws Exception {
        System.out.println("renameArchive " + TestHelper.addThreadAndTimeStamp());
        String userName = TEST_USER_NAME;
        String oldShortWorkfileName = "TestCheckInArchive.java";
        String newShortWorkfileName = "RenamedTestCheckInArchive.java";
        ServerResponseFactoryInterface response = bogusResponseObject;
        String branchParent = translucentBranchProperties.getBranchParent();
        ArchiveDirManagerForTranslucentBranch instance = new ArchiveDirManagerForTranslucentBranch(branchParent, translucentBranchProperties, getTranslucentBranchName(),
                TestHelper.SUBPROJECT2_APPENDED_PATH, TEST_USER_NAME, bogusResponseObject);
        boolean expResult = true;
        boolean result = instance.renameArchive(userName, oldShortWorkfileName, newShortWorkfileName, response);
        assertEquals(expResult, result);
        DirectoryContentsManager directoryContentsManager = DirectoryContentsManagerFactory.getInstance().getDirectoryContentsManager(TestHelper.getTestProjectName());
        DirectoryContents originDirectoryContents = directoryContentsManager.getDirectoryContentsForTranslucentBranch(translucentProjectView,
                instance.getAppendedPath(),
                instance.getDirectoryID(),
                bogusResponseObject);
        Map<Integer, String> fileNameMap = originDirectoryContents.getFiles();
        boolean newFileNameFoundFlag = false;
        boolean oldFileNameFoundFlag = false;
        for (Integer key : fileNameMap.keySet()) {
            String fileName = fileNameMap.get(key);
            System.out.println("File: " + fileName);
            if (0 == fileName.compareTo(newShortWorkfileName)) {
                newFileNameFoundFlag = true;
            }
            if (0 == fileName.compareTo(oldShortWorkfileName)) {
                oldFileNameFoundFlag = true;
            }
        }
        assertEquals("Did not find new workfile name!", true, newFileNameFoundFlag);
        assertEquals("Found old workfile name!", false, oldFileNameFoundFlag);
    }

    /**
     * Test of deleteArchive method, of class ArchiveDirManagerForTranslucentBranch.
     *
     * @throws Exception if something goes wrong.
     */
    @Test
    public void test14DeleteArchive() throws Exception {
        System.out.println("deleteArchive " + TestHelper.addThreadAndTimeStamp());
        String userName = TEST_USER_NAME;
        String shortWorkfileName = "RenamedTestCheckInArchive.java";
        ServerResponseFactoryInterface response = bogusResponseObject;
        String branchParent = translucentBranchProperties.getBranchParent();
        ArchiveDirManagerForTranslucentBranch instance = new ArchiveDirManagerForTranslucentBranch(branchParent, translucentBranchProperties, getTranslucentBranchName(),
                TestHelper.SUBPROJECT2_APPENDED_PATH, TEST_USER_NAME, bogusResponseObject);
        boolean expResult = true;
        boolean result = instance.deleteArchive(userName, shortWorkfileName, response);
        assertEquals(expResult, result);
        DirectoryContentsManager directoryContentsManager = DirectoryContentsManagerFactory.getInstance().getDirectoryContentsManager(TestHelper.getTestProjectName());
        DirectoryContents originDirectoryContents = directoryContentsManager.getDirectoryContentsForTranslucentBranch(translucentProjectView,
                instance.getAppendedPath(),
                instance.getDirectoryID(),
                bogusResponseObject);
        Map<Integer, String> fileNameMap = originDirectoryContents.getFiles();
        boolean fileNameFoundFlag = false;
        for (Integer key : fileNameMap.keySet()) {
            String fileName = fileNameMap.get(key);
            System.out.println("File: " + fileName);
            if (0 == fileName.compareTo(shortWorkfileName)) {
                fileNameFoundFlag = true;
            }
        }
        assertEquals("Found deleted workfile name in origin directory contents!", false, fileNameFoundFlag);
    }

    /**
     * Test of createDirectory method, of class ArchiveDirManagerForTranslucentBranch.
     */
    @Test
    public void test15CreateDirectory() {
        System.out.println("createDirectory " + TestHelper.addThreadAndTimeStamp());
        ArchiveDirManagerForTranslucentBranch instance = archiveDirManagerForTranslucentBranch;
        boolean expResult = true;
        boolean result = instance.createDirectory();
        assertEquals(expResult, result);
    }

    /**
     * Test of addChangeListener method, of class ArchiveDirManagerForTranslucentBranch. This is a no-op method that exists solely to satisfy the interface definition.
     */
    @Test
    public void test16AddChangeListener() {
        System.out.println("addChangeListener " + TestHelper.addThreadAndTimeStamp());
        ArchiveDirManagerForTranslucentBranch instance = archiveDirManagerForTranslucentBranch;
        boolean caughtExpectedException = false;
        try {
            instance.addChangeListener(null);
        } catch (UnsupportedOperationException e) {
            caughtExpectedException = true;
        }
        assertTrue(caughtExpectedException);
    }

    /**
     * Test of removeChangeListener method, of class ArchiveDirManagerForTranslucentBranch. This is a no-op method that exists solely to satisfy the interface definition.
     */
    @Test
    public void test17RemoveChangeListener() {
        System.out.println("removeChangeListener " + TestHelper.addThreadAndTimeStamp());
        ArchiveDirManagerForTranslucentBranch instance = archiveDirManagerForTranslucentBranch;
        boolean caughtExpectedException = false;
        try {
            instance.removeChangeListener(null);
        } catch (UnsupportedOperationException e) {
            caughtExpectedException = true;
        }
        assertTrue(caughtExpectedException);
    }

    /**
     * Test of startDirectoryManager method, of class ArchiveDirManagerForTranslucentBranch.
     */
    @Test
    public void test18StartDirectoryManager() {
        System.out.println("startDirectoryManager " + TestHelper.addThreadAndTimeStamp());
        ArchiveDirManagerForTranslucentBranch instance = archiveDirManagerForTranslucentBranch;
        instance.startDirectoryManager();
    }

    /**
     * Test of notifyListeners method, of class ArchiveDirManagerForTranslucentBranch. This is a no-op method that exists solely to satisfy the interface definition.
     */
    @Test
    public void test19NotifyListeners() {
        System.out.println("notifyListeners " + TestHelper.addThreadAndTimeStamp());
        ArchiveDirManagerForTranslucentBranch instance = archiveDirManagerForTranslucentBranch;
        boolean caughtExpectedException = false;
        try {
            instance.notifyListeners();
        } catch (UnsupportedOperationException e) {
            caughtExpectedException = true;
        }
        assertTrue(caughtExpectedException);
    }

    /**
     * Test of setFastNotify method, of class ArchiveDirManagerForTranslucentBranch. This is a no-op method that exists solely to satisfy the interface definition.
     */
    @Test
    public void test20SetFastNotify() {
        System.out.println("setFastNotify " + TestHelper.addThreadAndTimeStamp());
        boolean flag = false;
        ArchiveDirManagerForTranslucentBranch instance = archiveDirManagerForTranslucentBranch;
        instance.setFastNotify(flag);
    }

    /**
     * Test of getFastNotify method, of class ArchiveDirManagerForTranslucentBranch.
     */
    @Test
    public void test21GetFastNotify() {
        System.out.println("getFastNotify " + TestHelper.addThreadAndTimeStamp());
        ArchiveDirManagerForTranslucentBranch instance = archiveDirManagerForTranslucentBranch;
        boolean expResult = false;
        boolean result = instance.getFastNotify();
        assertEquals(expResult, result);
    }

    /**
     * Test of getArchiveInfoCollection method, of class ArchiveDirManagerForTranslucentBranch.
     */
    @Test
    public void test22GetArchiveInfoCollection() {
        System.out.println("getArchiveInfoCollection " + TestHelper.addThreadAndTimeStamp());
        ArchiveDirManagerForTranslucentBranch instance = archiveDirManagerForTranslucentBranch;
        Map<String, ArchiveInfoInterface> result = instance.getArchiveInfoCollection();
        assertNotNull(result);
        assertTrue("Size of archive info collection is too small", result.size() > 0);
    }

    /**
     * Test of getOldestRevision method, of class ArchiveDirManagerForTranslucentBranch.
     */
    @Test
    public void test23GetOldestRevision() {
        System.out.println("getOldestRevision " + TestHelper.addThreadAndTimeStamp());
        ArchiveDirManagerForTranslucentBranch instance = archiveDirManagerForTranslucentBranch;
        Date now = new Date();
        long expResult = now.getTime();
        long result = instance.getOldestRevision();
        assertTrue(expResult > result);
    }

    /**
     * Test of getDirectoryID method, of class ArchiveDirManagerForTranslucentBranch.
     */
    @Test
    public void test24GetDirectoryID() {
        System.out.println("getDirectoryID " + TestHelper.addThreadAndTimeStamp());
        ArchiveDirManagerForTranslucentBranch instance = archiveDirManagerForTranslucentBranch;
        int expResult = 0;
        int result = instance.getDirectoryID();
        assertTrue(expResult < result);
    }

    /**
     * Test of addLogFileListener method, of class ArchiveDirManagerForTranslucentBranch.
     */
    @Test
    public void test25AddRemoveNotifyLogFileListener() {
        System.out.println("addRemoveNotifyLogFileListener " + TestHelper.addThreadAndTimeStamp());
        TestServerResponseFactory logfileListener = new TestServerResponseFactory();
        ArchiveDirManagerForTranslucentBranch instance = archiveDirManagerForTranslucentBranch;
        instance.addLogFileListener(logfileListener);
        LogfileActionSetCommentPrefix logfileActionSetCommentPrefix = new LogfileActionSetCommentPrefix("Comment Prefix");

        Iterator<ArchiveInfoInterface> it = archiveDirManagerForTranslucentBranch.getArchiveInfoCollection().values().iterator();
        while (it.hasNext()) {
            instance.notifyLogfileListener(it.next(), logfileActionSetCommentPrefix);
        }
        instance.removeLogFileListener(logfileListener);
        assertTrue("Did not try to send a message to the client", logfileListener.getResponseSentCount() > 0);
    }

    /**
     * Test the ViewManager remove view method. We test it here instead of in the ViewManager test class because this test class starts up the server
     * (which we need in order to do a decent test of the removeView method).
     */
    @Test
    public void test26RemoveView() {
        System.out.println("removeView " + TestHelper.addThreadAndTimeStamp());
        assertTrue("View is not present", null != ViewManager.getInstance().getView(getProjectName(), getTranslucentBranchName()));
        ViewManager.getInstance().removeView(translucentProjectView, bogusResponseObject);
        assertTrue("View is still present", null == ViewManager.getInstance().getView(getProjectName(), getTranslucentBranchName()));
    }

    /**
     * This is a test class that we use for testing the logfilelistener code.
     */
    class TestServerResponseFactory implements ServerResponseFactoryInterface {
        // Use this counter to see if we actually tried to send some data to a 'client'.

        private int m_ResponseSentCount = 0;

        @Override
        public void addArchiveDirManager(ArchiveDirManagerInterface archiveDirManager) {
        }

        @Override
        public void createServerResponse(java.io.Serializable responseObject) {
            m_ResponseSentCount++;
        }

        @Override
        public String getServerName() {
            return TEST_SERVER_NAME;
        }

        @Override
        public String getUserName() {
            return TEST_USER_NAME;
        }

        @Override
        public int getClientPort() {
            return 9889;
        }

        @Override
        public String getClientIPAddress() {
            return "localhost";
        }

        @Override
        public boolean getConnectionAliveFlag() {
            return true;
        }

        @Override
        public void clientIsAlive() {
        }

        int getResponseSentCount() {
            return m_ResponseSentCount;
        }
    }

}
