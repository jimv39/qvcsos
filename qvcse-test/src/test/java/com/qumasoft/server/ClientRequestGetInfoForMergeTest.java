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
//     $Date: Wednesday, March 21, 2012 10:31:03 PM $
//   $Header: ClientRequestGetInfoForMergeTest.java Revision:1.3 Wednesday, March 21, 2012 10:31:03 PM JimVoris $
// $Copyright © 2011-2012 Define this string in the qvcs.keywords.properties property file $

package com.qumasoft.server;

import com.qumasoft.TestHelper;
import com.qumasoft.qvcslib.ClientRequestGetInfoForMergeData;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.QVCSException;
import com.qumasoft.qvcslib.RemoteViewProperties;
import com.qumasoft.qvcslib.ServerResponseFactoryInterface;
import java.util.Date;
import java.util.Properties;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author JimVoris
 */
public class ClientRequestGetInfoForMergeTest {

    private static ProjectView translucentProjectView = null;
    private static Date viewDate = null;
    private static RemoteViewProperties translucentBranchProperties = null;
    private ServerResponseFactoryInterface bogusResponseObject = null;
    private ClientRequestGetInfoForMergeData clientRequestGetInfoForMergeData = null;

    /**
     * Constructor for unit test.
     */
    public ClientRequestGetInfoForMergeTest() {
    }

    /**
     * Set up stuff once for these tests.
     *
     * @throws java.lang.Exception if anything goes wrong.
     */
    @BeforeClass
    public static void setUpClass() throws Exception {
        TestHelper.deleteViewStore();
        TestHelper.initializeArchiveFiles();
        TestHelper.startServer();
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
        TestHelper.stopServer();
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
