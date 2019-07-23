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
package com.qumasoft.clientapi;

import com.qumasoft.TestHelper;
import com.qumasoft.qvcslib.QVCSConstants;
import java.util.Date;
import java.util.List;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test the client API.
 *
 * @author Jim Voris
 */
public class ClientAPIServerTest {

    /**
     * Create our logger object.
     */
    private static final Logger logger = LoggerFactory.getLogger(ClientAPIServerTest.class);
    private static final String USERNAME = TestHelper.USER_NAME;
    private static final String PASSWORD = TestHelper.PASSWORD;
    private static final String SERVER_IP_ADDRESS = "127.0.0.1";
    private static final int SERVER_PORT = 29889;
    private static final String FILENAME = "QVCSEnterpriseServer.java";
    private static Object serverSyncObject = null;

    /**
     * Default ctor.
     */
    public ClientAPIServerTest() {
    }

    /**
     * Set up stuff once for these tests.
     *
     * @throws java.lang.Exception if anything goes wrong.
     */
    @BeforeClass
    public static void setUpClass() throws Exception {
        logger.info("Starting test class");
        TestHelper.initProjectProperties();
        TestHelper.initializeArchiveFiles();
        serverSyncObject = TestHelper.startServer();
    }

    /**
     * Tear down the class level stuff.
     *
     * @throws java.lang.Exception if anything goes wrong.
     */
    @AfterClass
    public static void tearDownClass() throws Exception {
        TestHelper.stopServer(serverSyncObject);
        TestHelper.removeArchiveFiles();
        logger.info("Ending test class");
    }

    /**
     * Run the tests in order.
     * @throws com.qumasoft.clientapi.ClientAPIException for client API exceptions.
     */
    @Test
    public void testClientAPIServer() throws ClientAPIException {
        testGetProjectList();
        testGetProjectListPreserveState();
        testGetBranchList();
        testGetBranchListPreserveState();
        testGetBranchListPreserveStateMissingProject();
        testGetProjectDirectoryList();
        testGetProjectDirectoryListMissingBranch();
        testGetFileInfoListNoRecursion();
        testGetFileInfoListWithRecursion();
        testGetRevisionInfoList();
        testGetMostRecentActivity();
    }

    /**
     * Test of getProjectList method, of class ClientAPI.
     *
     * @throws ClientAPIException if there was a problem.
     */
    public void testGetProjectList() throws ClientAPIException {
        System.out.println("getProjectList");
        ClientAPIContext clientAPIContext = ClientAPIFactory.createClientAPIContext();
        clientAPIContext.setUserName(USERNAME);
        clientAPIContext.setPassword(PASSWORD);
        clientAPIContext.setServerIPAddress(SERVER_IP_ADDRESS);
        clientAPIContext.setPort(SERVER_PORT);
        ClientAPI instance = ClientAPIFactory.createClientAPI(clientAPIContext);
        String expResult = TestHelper.getTestProjectName();
        List<String> result = instance.getProjectList();
        assertTrue(result.size() > 0);
        assertEquals(expResult, result.get(0));
    }

    /**
     * Test of getProjectList method, of class ClientAPI.
     *
     * @throws ClientAPIException if there was a problem.
     */
    public void testGetProjectListPreserveState() throws ClientAPIException {
        System.out.println("getProjectListPreserveState");
        try {
            ClientAPIContext clientAPIContext = ClientAPIFactory.createClientAPIContext();
            clientAPIContext.setUserName(USERNAME);
            clientAPIContext.setPassword(PASSWORD);
            clientAPIContext.setServerIPAddress(SERVER_IP_ADDRESS);
            clientAPIContext.setPort(SERVER_PORT);
            clientAPIContext.setPreserveStateFlag(true);
            ClientAPI instance = ClientAPIFactory.createClientAPI(clientAPIContext);
            String expResult = TestHelper.getTestProjectName();
            List<String> result = instance.getProjectList();
            assertTrue(result.size() > 0);
            assertEquals(expResult, result.get(0));
            result = instance.getProjectList();
            assertTrue(result.size() > 0);
            assertEquals(expResult, result.get(0));
        } catch (RuntimeException e) {
            logger.warn(e.getLocalizedMessage(), e);
            throw e;
        }
    }

    /**
     * Test of getBranchList method, of class ClientAPI.
     *
     * @throws ClientAPIException if there was a problem.
     */
    public void testGetBranchList() throws ClientAPIException {
        System.out.println("getBranchList");
        ClientAPIContext clientAPIContext = ClientAPIFactory.createClientAPIContext();
        clientAPIContext.setUserName(USERNAME);
        clientAPIContext.setPassword(PASSWORD);
        clientAPIContext.setServerIPAddress(SERVER_IP_ADDRESS);
        clientAPIContext.setPort(SERVER_PORT);
        clientAPIContext.setProjectName(TestHelper.getTestProjectName());
        String expResult = QVCSConstants.QVCS_TRUNK_BRANCH;
        ClientAPI instance = ClientAPIFactory.createClientAPI(clientAPIContext);
        List<String> result = instance.getBranchList();
        assertTrue(result.size() > 0);
        assertEquals(expResult, result.get(0));
    }

    /**
     * Test of getBranchList method, of class ClientAPI.
     *
     * @throws ClientAPIException if there was a problem.
     */
    public void testGetBranchListPreserveState() throws ClientAPIException {
        try {
            System.out.println("getBranchListPreserveState");
            ClientAPIContext clientAPIContext = ClientAPIFactory.createClientAPIContext();
            clientAPIContext.setUserName(USERNAME);
            clientAPIContext.setPassword(PASSWORD);
            clientAPIContext.setServerIPAddress(SERVER_IP_ADDRESS);
            clientAPIContext.setPort(SERVER_PORT);
            clientAPIContext.setProjectName(TestHelper.getTestProjectName());
            clientAPIContext.setPreserveStateFlag(true);
            String expResult = QVCSConstants.QVCS_TRUNK_BRANCH;
            ClientAPI instance = ClientAPIFactory.createClientAPI(clientAPIContext);
            List<String> result = instance.getBranchList();
            assertTrue(result.size() > 0);
            assertEquals(expResult, result.get(0));
            result = instance.getBranchList();
            assertTrue(result.size() > 0);
            assertEquals(expResult, result.get(0));
        } catch (RuntimeException e) {
            logger.warn(e.getLocalizedMessage(), e);
            throw e;
        }
    }

    /**
     * Test of getBranchList method, of class ClientAPI.
     *
     * @throws ClientAPIException if there was a problem.
     */
    public void testGetBranchListPreserveStateMissingProject() throws ClientAPIException {
        boolean threwExpectedException = false;
        try {
            System.out.println("testGetBranchListPreserveStateMissingProject");
            ClientAPIContext clientAPIContext = ClientAPIFactory.createClientAPIContext();
            clientAPIContext.setUserName(USERNAME);
            clientAPIContext.setPassword(PASSWORD);
            clientAPIContext.setServerIPAddress(SERVER_IP_ADDRESS);
            clientAPIContext.setPort(SERVER_PORT);
            clientAPIContext.setProjectName(TestHelper.getTestProjectName());
            clientAPIContext.setPreserveStateFlag(true);
            String expResult = QVCSConstants.QVCS_TRUNK_BRANCH;
            ClientAPI instance = ClientAPIFactory.createClientAPI(clientAPIContext);
            List<String> result = instance.getBranchList();
            assertTrue(result.size() > 0);
            assertEquals(expResult, result.get(0));
            clientAPIContext.setProjectName("UNKNOWN");
            instance.getBranchList();
        } catch (ClientAPIException e) {
            threwExpectedException = true;
        } catch (RuntimeException e) {
            logger.warn(e.getLocalizedMessage(), e);
            fail("Unexpected exception");
        }
        assertTrue(threwExpectedException);
    }

    /**
     * Test of getProjectDirectoryList method, of class ClientAPI.
     *
     * @throws ClientAPIException if there was a problem.
     */
    public void testGetProjectDirectoryList() throws ClientAPIException {
        System.out.println("getProjectDirectoryList");
        ClientAPIContext clientAPIContext = ClientAPIFactory.createClientAPIContext();
        clientAPIContext.setUserName(USERNAME);
        clientAPIContext.setPassword(PASSWORD);
        clientAPIContext.setServerIPAddress(SERVER_IP_ADDRESS);
        clientAPIContext.setPort(SERVER_PORT);
        clientAPIContext.setProjectName(TestHelper.getTestProjectName());
        clientAPIContext.setBranchName(QVCSConstants.QVCS_TRUNK_BRANCH);
        String expResult = "";
        ClientAPI instance = ClientAPIFactory.createClientAPI(clientAPIContext);
        List<String> result = instance.getProjectDirectoryList();
        assertTrue(result.size() > 0);
        assertEquals(expResult, result.get(0));
    }

    /**
     * Test of getProjectDirectoryList method, of class ClientAPI.
     *
     * @throws ClientAPIException if there was a problem.
     */
    public void testGetProjectDirectoryListMissingBranch() throws ClientAPIException {
        System.out.println("testGetProjectDirectoryListMissingBranch");
        boolean threwExpectedException = false;
        try {
            ClientAPIContext clientAPIContext = ClientAPIFactory.createClientAPIContext();
            clientAPIContext.setUserName(USERNAME);
            clientAPIContext.setPassword(PASSWORD);
            clientAPIContext.setServerIPAddress(SERVER_IP_ADDRESS);
            clientAPIContext.setPort(SERVER_PORT);
            clientAPIContext.setProjectName(TestHelper.getTestProjectName());
            clientAPIContext.setBranchName("UNKNOWN BRANCH");
            ClientAPI instance = ClientAPIFactory.createClientAPI(clientAPIContext);
            instance.getProjectDirectoryList();
        } catch (ClientAPIException e) {
            threwExpectedException = true;
        }
        assertTrue(threwExpectedException);
    }

    /**
     * Test of getFileInfoList method, of class ClientAPI.
     *
     * @throws ClientAPIException if there was a problem.
     */
    public void testGetFileInfoListNoRecursion() throws ClientAPIException {
        System.out.println("getFileInfoList");
        ClientAPIContext clientAPIContext = ClientAPIFactory.createClientAPIContext();
        clientAPIContext.setUserName(USERNAME);
        clientAPIContext.setPassword(PASSWORD);
        clientAPIContext.setServerIPAddress(SERVER_IP_ADDRESS);
        clientAPIContext.setPort(SERVER_PORT);
        clientAPIContext.setProjectName(TestHelper.getTestProjectName());
        clientAPIContext.setBranchName(QVCSConstants.QVCS_TRUNK_BRANCH);
        clientAPIContext.setAppendedPath("");
        clientAPIContext.setRecurseFlag(false);
        ClientAPI instance = ClientAPIFactory.createClientAPI(clientAPIContext);
        List<FileInfo> result = instance.getFileInfoList();
        assertTrue(result.size() > 0);
        boolean foundFILENAME = false;
        boolean correctAppendedPathFlag = false;
        for (FileInfo fileInfo : result) {
            if (fileInfo.getShortWorkfileName().equals(FILENAME)) {
                foundFILENAME = true;
            }
            if (fileInfo.getAppendedPath().equals("")) {
                correctAppendedPathFlag = true;
            } else {
                correctAppendedPathFlag = false;
                break;
            }
            if (fileInfo.getAttributes() == null) {
                fail("Missing file attributes");
            }
            if (fileInfo.getLastCheckInDate() == null) {
                fail("Missing last checkin date");
            }
            if (fileInfo.getLockCount() > 0) {
                fail("Lock count > 0");
            }
            if (fileInfo.getLockedByString() != null) {
                System.out.println("Short filename: [" + fileInfo.getShortWorkfileName() + "] locked by: [" + fileInfo.getLockedByString() + "]");
            }
            if (fileInfo.getRevisionCount() == 0) {
                fail("Unexpected revision count");
            }
            if (fileInfo.getShortWorkfileName().length() == 0) {
                fail("Empty short workfile name");
            }
        }
        assertTrue (foundFILENAME);
        assertTrue (correctAppendedPathFlag);
    }

    /**
     * Test of getFileInfoList method, of class ClientAPI.
     *
     * @throws ClientAPIException if there was a problem.
     */
    public void testGetFileInfoListWithRecursion() throws ClientAPIException {
        System.out.println("getFileInfoList");
        ClientAPIContext clientAPIContext = ClientAPIFactory.createClientAPIContext();
        clientAPIContext.setUserName(USERNAME);
        clientAPIContext.setPassword(PASSWORD);
        clientAPIContext.setServerIPAddress(SERVER_IP_ADDRESS);
        clientAPIContext.setPort(SERVER_PORT);
        clientAPIContext.setProjectName(TestHelper.getTestProjectName());
        clientAPIContext.setBranchName(QVCSConstants.QVCS_TRUNK_BRANCH);
        clientAPIContext.setAppendedPath("");
        clientAPIContext.setRecurseFlag(true);
        ClientAPI instance = ClientAPIFactory.createClientAPI(clientAPIContext);
        List<FileInfo> result = instance.getFileInfoList();
        assertTrue(result.size() > 1);
        boolean foundFILENAME = false;
        for (FileInfo fileInfo : result) {
            if (fileInfo.getShortWorkfileName().equals(FILENAME)) {
                foundFILENAME = true;
                break;
            }
        }
        assert (foundFILENAME);
    }

    /**
     * Test of getRevisionInfoList method, of class ClientAPI.
     *
     * @throws ClientAPIException if there was a problem.
     */
    public void testGetRevisionInfoList() throws ClientAPIException {
        System.out.println("getRevisionInfoList");
        ClientAPIContext clientAPIContext = ClientAPIFactory.createClientAPIContext();
        clientAPIContext.setUserName(USERNAME);
        clientAPIContext.setPassword(PASSWORD);
        clientAPIContext.setServerIPAddress(SERVER_IP_ADDRESS);
        clientAPIContext.setPort(SERVER_PORT);
        clientAPIContext.setProjectName(TestHelper.getTestProjectName());
        clientAPIContext.setBranchName(QVCSConstants.QVCS_TRUNK_BRANCH);
        clientAPIContext.setAppendedPath("");
        clientAPIContext.setFileName(FILENAME);
        ClientAPI instance = ClientAPIFactory.createClientAPI(clientAPIContext);
        List<RevisionInfo> result = instance.getRevisionInfoList();
        assertTrue(result.size() > 0);
    }

    public void testGetMostRecentActivity() throws ClientAPIException {
        System.out.println("testGetMostRecentActivity");
        ClientAPIContext clientAPIContext = ClientAPIFactory.createClientAPIContext();
        clientAPIContext.setUserName(USERNAME);
        clientAPIContext.setPassword(PASSWORD);
        clientAPIContext.setServerIPAddress(SERVER_IP_ADDRESS);
        clientAPIContext.setPort(SERVER_PORT);
        clientAPIContext.setProjectName(TestHelper.getTestProjectName());
        clientAPIContext.setBranchName(QVCSConstants.QVCS_TRUNK_BRANCH);
        clientAPIContext.setAppendedPath("");
        ClientAPI instance = ClientAPIFactory.createClientAPI(clientAPIContext);
        Date mostRecentActivity = instance.getMostRecentActivity();
        assertNotNull(mostRecentActivity);
    }
}
