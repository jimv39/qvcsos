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
//     $Date: Friday, April 06, 2012 10:12:47 PM $
//   $Header: ClientAPITest.java Revision:1.5 Friday, April 06, 2012 10:12:47 PM JimVoris $
// $Copyright © 2011-2012 Define this string in the qvcs.keywords.properties property file $

package com.qumasoft.clientapi;

import com.qumasoft.TestHelper;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.Utility;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test the client API.
 *
 * @author $Author: JimVoris $
 */
public class ClientAPITest {

    /**
     * Create our logger object
     */
    private static Logger logger = Logger.getLogger("com.qumasoft.clientapi");
    private static final String USERNAME = TestHelper.USER_NAME;
    private static final String PASSWORD = TestHelper.PASSWORD;
    private static final String SERVER_IP_ADDRESS = "127.0.0.1";
    private static final int SERVER_PORT = 29889;
    private static final String FILENAME = "QVCSEnterpriseServer.java";

    /**
     * Default ctor.
     */
    public ClientAPITest() {
    }

    /**
     * Set up stuff once for these tests.
     *
     * @throws java.lang.Exception if anything goes wrong.
     */
    @BeforeClass
    public static void setUpClass() throws Exception {
        TestHelper.initializeArchiveFiles();
        TestHelper.startServer();
    }

    /**
     * Tear down the class level stuff.
     *
     * @throws java.lang.Exception if anything goes wrong.
     */
    @AfterClass
    public static void tearDownClass() throws Exception {
        TestHelper.stopServer();
        TestHelper.removeArchiveFiles();
    }

    /**
     * Run before each test.
     */
    @Before
    public void setUp() {
    }

    /**
     * Run after each test.
     */
    @After
    public void tearDown() {
    }

    /**
     * Test of getProjectList method, of class ClientAPI.
     *
     * @throws ClientAPIException if there was a problem.
     */
    @Test
    public void testGetProjectList() throws ClientAPIException {
        System.out.println("getProjectList");
        ClientAPIContext clientAPIContext = ClientAPIFactory.createClientAPIContext();
        clientAPIContext.setUserName(USERNAME);
        clientAPIContext.setPassword(PASSWORD);
        clientAPIContext.setServerIPAddress(SERVER_IP_ADDRESS);
        clientAPIContext.setPort(Integer.valueOf(SERVER_PORT));
        ClientAPI instance = ClientAPIFactory.createClientAPI();
        String expResult = TestHelper.getTestProjectName();
        List<String> result = instance.getProjectList(clientAPIContext);
        assertTrue(result.size() > 0);
        assertEquals(expResult, result.get(0));
    }

    /**
     * Test of getProjectList method, of class ClientAPI.
     *
     * @throws ClientAPIException if there was a problem.
     */
    @Test
    public void testGetProjectListPreserveState() throws ClientAPIException {
        System.out.println("getProjectListPreserveState");
        try {
            ClientAPIContext clientAPIContext = ClientAPIFactory.createClientAPIContext();
            clientAPIContext.setUserName(USERNAME);
            clientAPIContext.setPassword(PASSWORD);
            clientAPIContext.setServerIPAddress(SERVER_IP_ADDRESS);
            clientAPIContext.setPort(Integer.valueOf(SERVER_PORT));
            clientAPIContext.setPreserveStateFlag(true);
            ClientAPI instance = ClientAPIFactory.createClientAPI();
            String expResult = TestHelper.getTestProjectName();
            List<String> result = instance.getProjectList(clientAPIContext);
            assertTrue(result.size() > 0);
            assertEquals(expResult, result.get(0));
            result = instance.getProjectList(clientAPIContext);
            assertTrue(result.size() > 0);
            assertEquals(expResult, result.get(0));
        } catch (RuntimeException e) {
            logger.log(Level.WARNING, Utility.expandStackTraceToString(e));
            throw e;
        }
    }

    /**
     * Test of getViewList method, of class ClientAPI.
     *
     * @throws ClientAPIException if there was a problem.
     */
    @Test
    public void testGetViewList() throws ClientAPIException {
        System.out.println("getViewList");
        ClientAPIContext clientAPIContext = ClientAPIFactory.createClientAPIContext();
        ClientAPI instance = ClientAPIFactory.createClientAPI();
        clientAPIContext.setUserName(USERNAME);
        clientAPIContext.setPassword(PASSWORD);
        clientAPIContext.setServerIPAddress(SERVER_IP_ADDRESS);
        clientAPIContext.setPort(Integer.valueOf(SERVER_PORT));
        clientAPIContext.setProjectName(TestHelper.getTestProjectName());
        String expResult = QVCSConstants.QVCS_TRUNK_VIEW;
        List<String> result = instance.getViewList(clientAPIContext);
        assertTrue(result.size() > 0);
        assertEquals(expResult, result.get(0));
    }

    /**
     * Test of getViewList method, of class ClientAPI.
     *
     * @throws ClientAPIException if there was a problem.
     */
    @Test
    public void testGetViewListPreserveState() throws ClientAPIException {
        try {
            System.out.println("getViewListPreserveState");
            ClientAPIContext clientAPIContext = ClientAPIFactory.createClientAPIContext();
            ClientAPI instance = ClientAPIFactory.createClientAPI();
            clientAPIContext.setUserName(USERNAME);
            clientAPIContext.setPassword(PASSWORD);
            clientAPIContext.setServerIPAddress(SERVER_IP_ADDRESS);
            clientAPIContext.setPort(Integer.valueOf(SERVER_PORT));
            clientAPIContext.setProjectName(TestHelper.getTestProjectName());
            clientAPIContext.setPreserveStateFlag(true);
            String expResult = QVCSConstants.QVCS_TRUNK_VIEW;
            List<String> result = instance.getViewList(clientAPIContext);
            assertTrue(result.size() > 0);
            assertEquals(expResult, result.get(0));
            result = instance.getViewList(clientAPIContext);
            assertTrue(result.size() > 0);
            assertEquals(expResult, result.get(0));
        } catch (RuntimeException e) {
            logger.log(Level.WARNING, Utility.expandStackTraceToString(e));
            throw e;
        }
    }

    /**
     * Test of getProjectDirectoryList method, of class ClientAPI.
     *
     * @throws ClientAPIException if there was a problem.
     */
    @Test
    public void testGetProjectDirectoryList() throws ClientAPIException {
        System.out.println("getProjectDirectoryList");
        ClientAPIContext clientAPIContext = ClientAPIFactory.createClientAPIContext();
        ClientAPI instance = ClientAPIFactory.createClientAPI();
        clientAPIContext.setUserName(USERNAME);
        clientAPIContext.setPassword(PASSWORD);
        clientAPIContext.setServerIPAddress(SERVER_IP_ADDRESS);
        clientAPIContext.setPort(Integer.valueOf(SERVER_PORT));
        clientAPIContext.setProjectName(TestHelper.getTestProjectName());
        clientAPIContext.setViewName(QVCSConstants.QVCS_TRUNK_VIEW);
        String expResult = "";
        List<String> result = instance.getProjectDirectoryList(clientAPIContext);
        assertTrue(result.size() > 0);
        assertEquals(expResult, result.get(0));
    }

    /**
     * Test of getFileInfoList method, of class ClientAPI.
     *
     * @throws ClientAPIException if there was a problem.
     */
    @Test
    public void testGetFileInfoListNoRecursion() throws ClientAPIException {
        System.out.println("getFileInfoList");
        ClientAPIContext clientAPIContext = ClientAPIFactory.createClientAPIContext();
        ClientAPI instance = ClientAPIFactory.createClientAPI();
        clientAPIContext.setUserName(USERNAME);
        clientAPIContext.setPassword(PASSWORD);
        clientAPIContext.setServerIPAddress(SERVER_IP_ADDRESS);
        clientAPIContext.setPort(Integer.valueOf(SERVER_PORT));
        clientAPIContext.setProjectName(TestHelper.getTestProjectName());
        clientAPIContext.setViewName(QVCSConstants.QVCS_TRUNK_VIEW);
        clientAPIContext.setAppendedPath("");
        clientAPIContext.setRecurseFlag(false);
        List<FileInfo> result = instance.getFileInfoList(clientAPIContext);
        assertTrue(result.size() > 0);
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
     * Test of getFileInfoList method, of class ClientAPI.
     *
     * @throws ClientAPIException if there was a problem.
     */
    @Test
    public void testGetFileInfoListWithRecursion() throws ClientAPIException {
        System.out.println("getFileInfoList");
        ClientAPIContext clientAPIContext = ClientAPIFactory.createClientAPIContext();
        ClientAPI instance = ClientAPIFactory.createClientAPI();
        clientAPIContext.setUserName(USERNAME);
        clientAPIContext.setPassword(PASSWORD);
        clientAPIContext.setServerIPAddress(SERVER_IP_ADDRESS);
        clientAPIContext.setPort(Integer.valueOf(SERVER_PORT));
        clientAPIContext.setProjectName(TestHelper.getTestProjectName());
        clientAPIContext.setViewName(QVCSConstants.QVCS_TRUNK_VIEW);
        clientAPIContext.setAppendedPath("");
        clientAPIContext.setRecurseFlag(true);
        List<FileInfo> result = instance.getFileInfoList(clientAPIContext);
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
    @Test
    public void testGetRevisionInfoList() throws ClientAPIException {
        System.out.println("getRevisionInfoList");
        ClientAPIContext clientAPIContext = ClientAPIFactory.createClientAPIContext();
        ClientAPI instance = ClientAPIFactory.createClientAPI();
        clientAPIContext.setUserName(USERNAME);
        clientAPIContext.setPassword(PASSWORD);
        clientAPIContext.setServerIPAddress(SERVER_IP_ADDRESS);
        clientAPIContext.setPort(Integer.valueOf(SERVER_PORT));
        clientAPIContext.setProjectName(TestHelper.getTestProjectName());
        clientAPIContext.setViewName(QVCSConstants.QVCS_TRUNK_VIEW);
        clientAPIContext.setAppendedPath("");
        clientAPIContext.setFileName(FILENAME);
        List<RevisionInfo> result = instance.getRevisionInfoList(clientAPIContext);
        assertTrue(result.size() > 0);
    }

    @Test
    public void testGetMostRecentActivity() throws ClientAPIException {
        System.out.println("testGetMostRecentActivity");
        ClientAPIContext clientAPIContext = ClientAPIFactory.createClientAPIContext();
        ClientAPI instance = ClientAPIFactory.createClientAPI();
        clientAPIContext.setUserName(USERNAME);
        clientAPIContext.setPassword(PASSWORD);
        clientAPIContext.setServerIPAddress(SERVER_IP_ADDRESS);
        clientAPIContext.setPort(Integer.valueOf(SERVER_PORT));
        clientAPIContext.setProjectName(TestHelper.getTestProjectName());
        clientAPIContext.setViewName(QVCSConstants.QVCS_TRUNK_VIEW);
        clientAPIContext.setAppendedPath("");
        Date mostRecentActivity = instance.getMostRecentActivity(clientAPIContext);
        assertNotNull(mostRecentActivity);
    }
}
