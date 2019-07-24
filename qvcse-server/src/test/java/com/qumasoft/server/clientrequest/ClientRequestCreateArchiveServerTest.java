/*
 * Copyright 2019 JimVoris.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qumasoft.server.clientrequest;

import com.qumasoft.TestHelper;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.RemoteBranchProperties;
import com.qumasoft.qvcslib.ServerResponseFactoryInterface;
import com.qumasoft.qvcslib.commandargs.CreateArchiveCommandArgs;
import com.qumasoft.qvcslib.requestdata.ClientRequestCreateArchiveData;
import com.qumasoft.qvcslib.response.ServerResponseCreateArchive;
import com.qumasoft.qvcslib.response.ServerResponseInterface;
import com.qumasoft.server.BogusResponseObject;
import com.qumasoft.server.BranchManager;
import com.qumasoft.server.ProjectBranch;
import com.qumasoft.server.ServerTransactionManager;
import java.util.Date;
import java.util.Properties;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test creating an archive file.
 * 
 * @author JimVoris
 */
public class ClientRequestCreateArchiveServerTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientRequestCreateArchiveServerTest.class);
    private static RemoteBranchProperties featureBranchProperties = null;
    private static RemoteBranchProperties trunkBranchProperties = null;
    private static ProjectBranch projectBranch = null;
    private static Object serverSyncObject = null;

    public ClientRequestCreateArchiveServerTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        TestHelper.deleteBranchStore();
        serverSyncObject = TestHelper.startServer();
        Properties fbProjectProperties = new Properties();
        featureBranchProperties = new RemoteBranchProperties(getProjectName(), getFeatureBranchName(), fbProjectProperties);
        featureBranchProperties.setIsReadOnlyBranchFlag(false);
        featureBranchProperties.setIsDateBasedBranchFlag(false);
        featureBranchProperties.setIsFeatureBranchFlag(true);
        featureBranchProperties.setIsOpaqueBranchFlag(false);
        featureBranchProperties.setBranchParent(QVCSConstants.QVCS_TRUNK_BRANCH);
        featureBranchProperties.setBranchDate(new Date());
        projectBranch = new ProjectBranch();
        projectBranch.setProjectName(getProjectName());
        projectBranch.setBranchName(getFeatureBranchName());
        projectBranch.setRemoteBranchProperties(featureBranchProperties);
        BranchManager.getInstance().addBranch(projectBranch);
    }

    @AfterClass
    public static void tearDownClass() {
        TestHelper.stopServer(serverSyncObject);
        TestHelper.deleteBranchStore();
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    static private String getProjectName() {
        return TestHelper.getTestProjectName();
    }

    static private String getFeatureBranchName() {
        return "2.2.3";
    }

    static private String getShortWorkfileNameForBranchCreateArchive() {
        return "FeatureBranchQVCSEnterpriseServer.java";
    }

    static private String getShortWorkfileNameForTrunkCreateArchive() {
        return "TrunkQVCSEnterpriseServer.java";
    }


    /**
     * Test of execute method, of class ClientRequestCreateArchive.
     */
    @Test
    public void testCreateTrunkArchive() {
        ClientRequestCreateArchiveData data = new ClientRequestCreateArchiveData();
        data.setAppendedPath("");
        data.setBranchName(QVCSConstants.QVCS_TRUNK_BRANCH);
        data.setProjectName(TestHelper.getTestProjectName());
        data.setBuffer(createBufferForTrunkCreateArchive());
        CreateArchiveCommandArgs commandArgs = new CreateArchiveCommandArgs();
        commandArgs.setUserName("JimVoris");
        commandArgs.setLockFlag(false);
        commandArgs.setWorkfileName(getShortWorkfileNameForTrunkCreateArchive());
        commandArgs.setArchiveDescription("Testing trunk archive create");
        commandArgs.setInputfileTimeStamp(new Date());
        commandArgs.setCommentPrefix("   ");
        data.setCommandArgs(commandArgs);
        ClientRequestCreateArchive instance = new ClientRequestCreateArchive(data);
        // Wrap this work in a server transaction so the DirectoryContents
        // stuff will behave in a useful way...
        ServerResponseFactoryInterface bogusResponseObject = new BogusResponseObject();
        // Keep track that we're in a transaction.
        ServerTransactionManager.getInstance().clientBeginTransaction(bogusResponseObject);
        ServerResponseInterface response = instance.execute(commandArgs.getUserName(), bogusResponseObject);
        if (response instanceof ServerResponseCreateArchive) {
            ServerResponseCreateArchive createResponse = (ServerResponseCreateArchive) response;
            LOGGER.info("Created archive. [{}] [{}] [{}]", createResponse.getSkinnyLogfileInfo().getLastEditBy(), createResponse.getSkinnyLogfileInfo().getFileID(),
                    createResponse.getSkinnyLogfileInfo().getDefaultRevisionString());
        } else {
            fail("Failed to create trunk archive.");
        }
    }

    private byte[] createBufferForTrunkCreateArchive() {
        String fileContent = "This is the contents of the trunk revision.";
        byte[] buffer = fileContent.getBytes();
        return buffer;
    }

    private byte[] createBufferForFeatureBranchCreateArchive() {
        String fileContent = "This is the contents of the feature branch revision.";
        byte[] buffer = fileContent.getBytes();
        return buffer;
    }

}
