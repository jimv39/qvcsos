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
package com.qumasoft.server.clientrequest;

import com.qumasoft.TestHelper;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.QVCSException;
import com.qumasoft.qvcslib.RemoteViewProperties;
import com.qumasoft.qvcslib.ServerResponseFactoryInterface;
import com.qumasoft.qvcslib.requestdata.ClientRequestGetInfoForMergeData;
import com.qumasoft.server.BogusResponseObject;
import com.qumasoft.server.ProjectView;
import com.qumasoft.server.ServerTransactionManager;
import com.qumasoft.server.ViewManager;
import java.util.Date;
import java.util.Properties;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Get info for merge server test.
 * @author Jim Voris
 */
public class ClientRequestGetInfoForMergeServerTest {

    private static ProjectView translucentProjectView;
    private static Date viewDate;
    private static RemoteViewProperties translucentBranchProperties;
    private ServerResponseFactoryInterface bogusResponseObject;
    private ClientRequestGetInfoForMergeData clientRequestGetInfoForMergeData;
    private static Object serverSyncObject = null;

    /**
     * Constructor for unit test.
     */
    public ClientRequestGetInfoForMergeServerTest() {
    }

    /**
     * Set up stuff once for these tests.
     *
     * @throws java.lang.Exception if anything goes wrong.
     */
    @BeforeClass
    public static void setUpClass() throws Exception {
        TestHelper.deleteViewStore();
        TestHelper.initProjectProperties();
        TestHelper.initializeArchiveFiles();
        serverSyncObject = TestHelper.startServer();
        viewDate = new Date();
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
     * Set up before each test.
     */
    @Before
    public void setUp() {
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

    static private void initializeTranslucentBranch() throws QVCSException {
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
        ViewManager.getInstance().addView(translucentProjectView);
    }

    static private String getProjectName() {
        return TestHelper.getTestProjectName();
    }

    static private String getTranslucentBranchName() {
        return "2.2.2";
    }

    /**
     * Test of execute method, of class ClientRequestGetInfoForMerge.
     * TODO
     */
    @Test
    public void testExecuteA() {
//        clientRequestGetInfoForMergeData = new ClientRequestGetInfoForMergeData();
//        clientRequestGetInfoForMergeData.setProjectName(getProjectName());
//        clientRequestGetInfoForMergeData.setViewName(getTranslucentBranchName());
//        clientRequestGetInfoForMergeData.setAppendedPath(TestHelper.SUBPROJECT2_APPENDED_PATH);
//        clientRequestGetInfoForMergeData.setFileID(4);
//        String[] args = null;
//        ClientRequestGetInfoForMerge instance = new ClientRequestGetInfoForMerge(clientRequestGetInfoForMergeData);
//        SerializableObjectInterface result = instance.execute(TestHelper.USER_NAME, bogusResponseObject);
    }
//    /**
//     * Test of execute method, of class ClientRequestGetInfoForMerge.
//     */
//    @Test
//    public void testExecuteB()
//    {
//        String userName = "";
//        ClientRequestGetInfoForMerge instance = null;
//        SerializableObjectInterface expResult = null;
//        SerializableObjectInterface result = instance.execute(TestHelper.USER_NAME, bogusResponseObject);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
}
