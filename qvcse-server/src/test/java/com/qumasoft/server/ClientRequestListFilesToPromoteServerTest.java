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
package com.qumasoft.server;

import com.qumasoft.TestHelper;
import com.qumasoft.qvcslib.ClientRequestCheckInData;
import com.qumasoft.qvcslib.ClientRequestListFilesToPromoteData;
import com.qumasoft.qvcslib.LogFileOperationCheckInCommandArgs;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.RemoteViewProperties;
import com.qumasoft.qvcslib.ServerResponseCheckIn;
import com.qumasoft.qvcslib.ServerResponseFactoryInterface;
import com.qumasoft.qvcslib.ServerResponseInterface;
import com.qumasoft.qvcslib.ServerResponseListFilesToPromote;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Unit test for client request list files to promote.
 *
 * @author Jim Voris
 */
public class ClientRequestListFilesToPromoteServerTest {

    private static ProjectView projectView = null;
    private static RemoteViewProperties translucentBranchProperties = null;

    /**
     * Default constructor.
     */
    public ClientRequestListFilesToPromoteServerTest() {
    }

    /**
     * Execute once when the class is loaded.
     *
     * @throws Exception if there is a problem.
     */
    @BeforeClass
    public static void setUpClass() throws Exception {
        TestHelper.deleteViewStore();
        initializeArchiveFiles();
        TestHelper.startServer();
        Properties projectProperties = new Properties();
        translucentBranchProperties = new RemoteViewProperties(getProjectName(), getBranchName(), projectProperties);
        translucentBranchProperties.setIsReadOnlyViewFlag(false);
        translucentBranchProperties.setIsDateBasedViewFlag(false);
        translucentBranchProperties.setIsTranslucentBranchFlag(true);
        translucentBranchProperties.setIsOpaqueBranchFlag(false);
        translucentBranchProperties.setBranchParent(QVCSConstants.QVCS_TRUNK_VIEW);
        translucentBranchProperties.setBranchDate(new Date());
        projectView = new ProjectView();
        projectView.setProjectName(getProjectName());
        projectView.setViewName(getBranchName());
        projectView.setRemoteViewProperties(translucentBranchProperties);
        ViewManager.getInstance().addView(projectView);
    }

    /**
     * Execute once when all the tests are finished.
     *
     * @throws Exception if there is a problem.
     */
    @AfterClass
    public static void tearDownClass() throws Exception {
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
        } catch (IOException ex) {
            Logger.getLogger(ClientRequestListFilesToPromoteServerTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    static private String getProjectName() {
        return TestHelper.getTestProjectName();
    }

    static private String getBranchName() {
        return "2.2.3";
    }

    static private String getShortWorkfileName() {
        return "QVCSEnterpriseServer.java";
    }

    /**
     * Test of execute method, of class ClientRequestListFilesToPromote.
     */
    @Test
    public void testExecute() {
        System.out.println("execute");
        checkInRevisionForBranch();

        ClientRequestListFilesToPromoteData data = new ClientRequestListFilesToPromoteData();
        data.setProjectName(getProjectName());
        data.setViewName(getBranchName());
        ClientRequestListFilesToPromote instance = new ClientRequestListFilesToPromote(data);
        // Wrap this work in a server transaction so the DirectoryContents
        // stuff will behave in a useful way...
        ServerResponseFactoryInterface bogusResponseObject = new BogusResponseObject();
        // Keep track that we're in a transaction.
        ServerTransactionManager.getInstance().clientBeginTransaction(bogusResponseObject);
        ServerResponseInterface serverResponse = instance.execute("JimVoris", bogusResponseObject);
        ServerTransactionManager.getInstance().clientEndTransaction(bogusResponseObject);
        if (serverResponse instanceof ServerResponseListFilesToPromote) {
            ServerResponseListFilesToPromote serverResponseListFilesToPromote = (ServerResponseListFilesToPromote) serverResponse;
            assertNotNull(serverResponseListFilesToPromote.getFilesToPromoteList());
        } else {
            fail("Did not return ServerResponseListFilesToPromote object.");
        }
    }

    private void checkInRevisionForBranch() {
        String checkInFilename = getShortWorkfileName();
        LogFileOperationCheckInCommandArgs commandArgs = new LogFileOperationCheckInCommandArgs();
        commandArgs.setApplyLabelFlag(false);
        commandArgs.setCheckInComment("A test checkin comment");
        commandArgs.setCreateNewRevisionIfEqual(true);
        commandArgs.setFullWorkfileName(checkInFilename);
        commandArgs.setProjectName(getProjectName());
        commandArgs.setShortWorkfileName(checkInFilename);
        commandArgs.setUserName("JimVoris");
        commandArgs.setInputfileTimeStamp(new Date());

        ClientRequestCheckInData clientRequestCheckInData = new ClientRequestCheckInData();
        clientRequestCheckInData.setCommandArgs(commandArgs);
        clientRequestCheckInData.setProjectName(getProjectName());
        clientRequestCheckInData.setViewName(getBranchName());
        clientRequestCheckInData.setAppendedPath("");
        clientRequestCheckInData.setIndex(10);
        String junkBuffer = "ABC DEF GHI JKL MNOP QRS TUV WXYZ";
        byte[] buffer = junkBuffer.getBytes();
        clientRequestCheckInData.setBuffer(buffer);

        ServerResponseFactoryInterface bogusResponseObject = new BogusResponseObject();
        // Keep track that we're in a transaction.
        ServerTransactionManager.getInstance().clientBeginTransaction(bogusResponseObject);
        ClientRequestCheckIn clientRequestCheckIn = new ClientRequestCheckIn(clientRequestCheckInData);
        ServerResponseInterface serverResponse = clientRequestCheckIn.execute(TestHelper.USER_NAME, bogusResponseObject);
        ServerTransactionManager.getInstance().clientEndTransaction(bogusResponseObject);
        if (serverResponse instanceof ServerResponseCheckIn) {
            ServerResponseCheckIn serverResponseCheckIn = (ServerResponseCheckIn) serverResponse;
            assertNotNull(serverResponseCheckIn.getShortWorkfileName());
        }
    }
}
