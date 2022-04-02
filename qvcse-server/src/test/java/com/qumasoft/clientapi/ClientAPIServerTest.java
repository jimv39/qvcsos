/*   Copyright 2004-2022 Jim Voris
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
import com.qumasoft.server.QVCSEnterpriseServer;
import com.qvcsos.CommonTestHelper;
import com.qvcsos.server.DatabaseManager;
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
    private static DatabaseManager databaseManager;

    /**
     * Create our logger object.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientAPIServerTest.class);
    private static final String USERNAME = TestHelper.USER_NAME;
    private static final String PASSWORD = TestHelper.PASSWORD;
    private static final String SERVER_IP_ADDRESS = "127.0.0.1";
    private static final int SERVER_PORT = 29889;
    private static final String FILENAME = "QVCSEnterpriseServer.java";
    private static Object serverSyncObject = null;
    private static final long ONE_SECOND = 1000L;

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
        while (QVCSEnterpriseServer.getServerIsRunningFlag()) {
            // We need to wait for the server to exit.
            LOGGER.info("Waiting for server to exit.");
            Thread.sleep(ONE_SECOND);
        }
        CommonTestHelper.getCommonTestHelper().acquireSyncObject();
        CommonTestHelper.getCommonTestHelper().resetTestDatabaseViaPsqlScript();
        CommonTestHelper.getCommonTestHelper().resetQvcsosTestDatabaseViaPsqlScript();
        databaseManager = DatabaseManager.getInstance();
        databaseManager.initializeDatabase();
        TestHelper.addUserToDatabase(USERNAME, PASSWORD);
        TestHelper.updateAdminPassword();
        TestHelper.addTestFilesToTestProject();
        // Only the server should have a db connection. We use the db only to set things up before starting the test.
        databaseManager.closeConnection();
        databaseManager.shutdownDatabase();
        LOGGER.info("Starting ClientAPIServerTest test class");
        serverSyncObject = TestHelper.startServer();
    }

    /**
     * Tear down the class level stuff.
     *
     * @throws java.lang.Exception if anything goes wrong.
     */
    @AfterClass
    public static void tearDownClass() throws Exception {
        LOGGER.info("Beginning tearDownClass for ClientAPIServerTest test class");
        Thread.sleep(ONE_SECOND);
        TestHelper.stopServerByMessage();
        CommonTestHelper.getCommonTestHelper().releaseSyncObject();
        LOGGER.info("Ending ClientAPIServerTest test class");
    }

    /**
     * Run the tests in order.
     * @throws com.qumasoft.clientapi.ClientAPIException for client API exceptions.
     */
    @Test
    public void testClientAPIServer() throws Exception {
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
        LOGGER.info("ClientAPIServerTest.getProjectList");
        ClientAPIContext clientAPIContext = ClientAPIFactory.createClientAPIContext();
        clientAPIContext.setUserName(USERNAME);
        clientAPIContext.setPassword(PASSWORD);
        clientAPIContext.setServerIPAddress(SERVER_IP_ADDRESS);
        clientAPIContext.setPort(SERVER_PORT);
        ClientAPI instance = ClientAPIFactory.createClientAPI(clientAPIContext);
        String expResult = TestHelper.getTestProjectName();
        instance.login();
        List<String> result = instance.getProjectList();
        assertTrue(!result.isEmpty());
        assertEquals(expResult, result.get(0));
    }

    /**
     * Test of getProjectList method, of class ClientAPI.
     *
     * @throws ClientAPIException if there was a problem.
     */
    public void testGetProjectListPreserveState() throws ClientAPIException {
        LOGGER.info("ClientAPIServerTest.getProjectListPreserveState");
        try {
            ClientAPIContext clientAPIContext = ClientAPIFactory.createClientAPIContext();
            clientAPIContext.setUserName(USERNAME);
            clientAPIContext.setPassword(PASSWORD);
            clientAPIContext.setServerIPAddress(SERVER_IP_ADDRESS);
            clientAPIContext.setPort(SERVER_PORT);
            clientAPIContext.setPreserveStateFlag(true);
            ClientAPI instance = ClientAPIFactory.createClientAPI(clientAPIContext);
            String expResult = TestHelper.getTestProjectName();
            instance.login();
            List<String> result = instance.getProjectList();
            assertTrue(!result.isEmpty());
            assertEquals(expResult, result.get(0));
            result = instance.getProjectList();
            assertTrue(!result.isEmpty());
            assertEquals(expResult, result.get(0));
        } catch (RuntimeException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
            throw e;
        }
    }

    /**
     * Test of getBranchList method, of class ClientAPI.
     *
     * @throws ClientAPIException if there was a problem.
     */
    public void testGetBranchList() throws ClientAPIException {
        LOGGER.info("ClientAPIServerTest.getBranchList");
        ClientAPIContext clientAPIContext = ClientAPIFactory.createClientAPIContext();
        clientAPIContext.setUserName(USERNAME);
        clientAPIContext.setPassword(PASSWORD);
        clientAPIContext.setServerIPAddress(SERVER_IP_ADDRESS);
        clientAPIContext.setPort(SERVER_PORT);
        clientAPIContext.setProjectName(TestHelper.getTestProjectName());
        String expResult = QVCSConstants.QVCS_TRUNK_BRANCH;
        ClientAPI instance = ClientAPIFactory.createClientAPI(clientAPIContext);
        instance.login();
        List<String> result = instance.getBranchList();
        assertTrue(!result.isEmpty());
        assertEquals(expResult, result.get(0));
    }

    /**
     * Test of getBranchList method, of class ClientAPI.
     *
     * @throws ClientAPIException if there was a problem.
     */
    public void testGetBranchListPreserveState() throws ClientAPIException {
        LOGGER.info("ClientAPIServerTest.getBranchListPreserveState");
        try {
            ClientAPIContext clientAPIContext = ClientAPIFactory.createClientAPIContext();
            clientAPIContext.setUserName(USERNAME);
            clientAPIContext.setPassword(PASSWORD);
            clientAPIContext.setServerIPAddress(SERVER_IP_ADDRESS);
            clientAPIContext.setPort(SERVER_PORT);
            clientAPIContext.setProjectName(TestHelper.getTestProjectName());
            clientAPIContext.setPreserveStateFlag(true);
            String expResult = QVCSConstants.QVCS_TRUNK_BRANCH;
            ClientAPI instance = ClientAPIFactory.createClientAPI(clientAPIContext);
            instance.login();
            List<String> result = instance.getBranchList();
            assertTrue(!result.isEmpty());
            assertEquals(expResult, result.get(0));
            result = instance.getBranchList();
            assertTrue(!result.isEmpty());
            assertEquals(expResult, result.get(0));
        } catch (RuntimeException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
            throw e;
        }
    }

    /**
     * Test of getBranchList method, of class ClientAPI.
     *
     * @throws ClientAPIException if there was a problem.
     */
    public void testGetBranchListPreserveStateMissingProject() throws ClientAPIException {
        LOGGER.info("ClientAPIServerTest.getBranchListPreserveStateMissingProject");
        boolean threwExpectedException = false;
        try {
            ClientAPIContext clientAPIContext = ClientAPIFactory.createClientAPIContext();
            clientAPIContext.setUserName(USERNAME);
            clientAPIContext.setPassword(PASSWORD);
            clientAPIContext.setServerIPAddress(SERVER_IP_ADDRESS);
            clientAPIContext.setPort(SERVER_PORT);
            clientAPIContext.setProjectName(TestHelper.getTestProjectName());
            clientAPIContext.setPreserveStateFlag(true);
            String expResult = QVCSConstants.QVCS_TRUNK_BRANCH;
            ClientAPI instance = ClientAPIFactory.createClientAPI(clientAPIContext);
            instance.login();
            List<String> result = instance.getBranchList();
            assertTrue(!result.isEmpty());
            assertEquals(expResult, result.get(0));
            clientAPIContext.setProjectName("UNKNOWN");
            instance.getBranchList();
        } catch (ClientAPIException e) {
            threwExpectedException = true;
        } catch (RuntimeException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
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
        LOGGER.info("ClientAPIServerTest.getProjectDirectoryList");
        ClientAPIContext clientAPIContext = ClientAPIFactory.createClientAPIContext();
        clientAPIContext.setUserName(USERNAME);
        clientAPIContext.setPassword(PASSWORD);
        clientAPIContext.setServerIPAddress(SERVER_IP_ADDRESS);
        clientAPIContext.setPort(SERVER_PORT);
        clientAPIContext.setProjectName(TestHelper.getTestProjectName());
        clientAPIContext.setBranchName(QVCSConstants.QVCS_TRUNK_BRANCH);
        String expResult = "";
        ClientAPI instance = ClientAPIFactory.createClientAPI(clientAPIContext);
        instance.login();
        List<String> result = instance.getProjectDirectoryList();
        assertTrue(!result.isEmpty());
        assertEquals(expResult, result.get(0));
    }

    /**
     * Test of getProjectDirectoryList method, of class ClientAPI.
     *
     * @throws ClientAPIException if there was a problem.
     */
    public void testGetProjectDirectoryListMissingBranch() throws ClientAPIException {
        LOGGER.info("ClientAPIServerTest.getProjectDirectoryListMissingBranch");
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
            instance.login();
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
        LOGGER.info("ClientAPIServerTest.getFileInfoListNoRecursion");
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
        instance.login();
        List<FileInfo> result = instance.getFileInfoList();
        assertTrue(!result.isEmpty());
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
        LOGGER.info("ClientAPIServerTest.getFileInfoListWithRecursion");
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
        instance.login();
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
        LOGGER.info("ClientAPIServerTest.getRevisionInfoList");
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
        instance.login();
        List<RevisionInfo> result = instance.getRevisionInfoList();
        assertTrue(!result.isEmpty());
    }

    public void testGetMostRecentActivity() throws ClientAPIException {
        LOGGER.info("ClientAPIServerTest.getMostRecentActivity");
        ClientAPIContext clientAPIContext = ClientAPIFactory.createClientAPIContext();
        clientAPIContext.setUserName(USERNAME);
        clientAPIContext.setPassword(PASSWORD);
        clientAPIContext.setServerIPAddress(SERVER_IP_ADDRESS);
        clientAPIContext.setPort(SERVER_PORT);
        clientAPIContext.setProjectName(TestHelper.getTestProjectName());
        clientAPIContext.setBranchName(QVCSConstants.QVCS_TRUNK_BRANCH);
        clientAPIContext.setAppendedPath("");
        ClientAPI instance = ClientAPIFactory.createClientAPI(clientAPIContext);
        instance.login();
        Date mostRecentActivity = instance.getMostRecentActivity();
        assertNotNull(mostRecentActivity);
    }
}
