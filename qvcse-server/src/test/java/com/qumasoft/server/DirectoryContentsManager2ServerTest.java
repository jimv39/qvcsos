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
import com.qumasoft.qvcslib.ArchiveInfoInterface;
import com.qumasoft.qvcslib.DirectoryCoordinate;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.QVCSException;
import com.qumasoft.qvcslib.RemoteBranchProperties;
import com.qumasoft.qvcslib.ServerResponseFactoryInterface;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.Properties;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * A second set of tests for the DirectoryContentsManager class. This set of tests focuses on testing file moves on the trunk to make sure that those moves are correctly propagated
 * to any feature branches.
 *
 * @author Jim Voris
 */
public class DirectoryContentsManager2ServerTest {

    private static ProjectBranch feature1BranchProjectBranch = null;
    private static ProjectBranch feature2BranchProjectBranch = null;
    private ServerResponseFactoryInterface bogusResponseObject = null;
    private DirectoryContentsManager directoryContentsManager = null;
    private static Object serverSyncObject = null;

    /**
     * Set up stuff once for these tests.
     *
     * @throws java.lang.Exception if anything goes wrong.
     */
    @BeforeClass
    public static void setUpClass() throws Exception {
        TestHelper.deleteBranchStore();
        TestHelper.initProjectProperties();
        TestHelper.initializeArchiveFiles();
        serverSyncObject = TestHelper.startServer();
        initializeFeature1Branch();
        initializeFeature2Branch();
    }

    /**
     * Tear down the class level stuff.
     *
     * @throws java.lang.Exception if anything goes wrong.
     */
    @AfterClass
    public static void tearDownClass() throws Exception {
        TestHelper.stopServer(serverSyncObject);
        TestHelper.deleteBranchStore();
        TestHelper.removeArchiveFiles();
    }

    /**
     * Set up before each test.
     */
    public void setUp() {
        directoryContentsManager = DirectoryContentsManagerFactory.getInstance().getDirectoryContentsManager(TestHelper.getTestProjectName());
        bogusResponseObject = new BogusResponseObject();
        ServerTransactionManager.getInstance().flushClientTransaction(bogusResponseObject);
        ServerTransactionManager.getInstance().clientBeginTransaction(bogusResponseObject);
    }

    static private void initializeFeature1Branch() throws QVCSException {
        Properties projectProperties = new Properties();
        RemoteBranchProperties feature1BranchProperties = new RemoteBranchProperties(getProjectName(), getFeature1BranchName(), projectProperties);
        feature1BranchProperties.setIsReadOnlyBranchFlag(false);
        feature1BranchProperties.setIsDateBasedBranchFlag(false);
        feature1BranchProperties.setIsFeatureBranchFlag(true);
        feature1BranchProperties.setIsOpaqueBranchFlag(false);
        feature1BranchProperties.setBranchParent(QVCSConstants.QVCS_TRUNK_BRANCH);
        feature1BranchProperties.setBranchDate(new Date());
        feature1BranchProjectBranch = new ProjectBranch();
        feature1BranchProjectBranch.setProjectName(getProjectName());
        feature1BranchProjectBranch.setBranchName(getFeature1BranchName());
        feature1BranchProjectBranch.setRemoteBranchProperties(feature1BranchProperties);
        BranchManager.getInstance().addBranch(feature1BranchProjectBranch);
    }

    static private void initializeFeature2Branch() throws QVCSException {
        Properties projectProperties = new Properties();
        RemoteBranchProperties feature2BranchProperties = new RemoteBranchProperties(getProjectName(), getFeature2BranchName(), projectProperties);
        feature2BranchProperties.setIsReadOnlyBranchFlag(false);
        feature2BranchProperties.setIsDateBasedBranchFlag(false);
        feature2BranchProperties.setIsFeatureBranchFlag(true);
        feature2BranchProperties.setIsOpaqueBranchFlag(false);
        feature2BranchProperties.setBranchParent(QVCSConstants.QVCS_TRUNK_BRANCH);
        feature2BranchProperties.setBranchDate(new Date());
        feature2BranchProjectBranch = new ProjectBranch();
        feature2BranchProjectBranch.setProjectName(getProjectName());
        feature2BranchProjectBranch.setBranchName(getFeature2BranchName());
        feature2BranchProjectBranch.setRemoteBranchProperties(feature2BranchProperties);
        BranchManager.getInstance().addBranch(feature2BranchProjectBranch);

    }

    static private String getProjectName() {
        return TestHelper.getTestProjectName();
    }

    static private String getFeature1BranchName() {
        return "2.2.2-Feature1";
    }

    static private String getFeature2BranchName() {
        return "2.2.2-Feature2";
    }

    @Test
    public void testDirectoryContentsManager() throws QVCSException, IOException {
        test1VerifyTestSetup();
        test2MoveOnOneBranchThenMoveOnTrunk();
    }

    /**
     * Verify test setup. This test just verifies that we have set things up in a useful way for the tests in this unit test.
     *
     * @throws QVCSException if there is a QVCS problem.
     * @throws IOException for IO problems.
     */
    public void test1VerifyTestSetup() throws QVCSException, IOException {
        setUp();
        Collection<ProjectBranch> projectBranchCollection = BranchManager.getInstance().getBranches(getProjectName());
        ProjectBranch featureBranch = BranchManager.getInstance().getBranch(getProjectName(), getFeature1BranchName());
        assertEquals("Wrong number of branches", 2, projectBranchCollection.size());

        DirectoryCoordinate firstDirectoryCoordinate = new DirectoryCoordinate(getProjectName(), getFeature1BranchName(), TestHelper.SUBPROJECT_APPENDED_PATH);
        ArchiveDirManagerInterface firstDirManager = ArchiveDirManagerFactoryForServer.getInstance().getDirectoryManager(QVCSConstants.QVCS_SERVER_SERVER_NAME,
                firstDirectoryCoordinate, QVCSConstants.QVCS_SERVED_PROJECT_TYPE, QVCSConstants.QVCS_SERVER_USER, bogusResponseObject);
        assertTrue(firstDirManager != null);
        ArchiveInfoInterface firstDirArchiveInfo = firstDirManager.getArchiveInfo(TestHelper.SUBPROJECT_FIRST_SHORTWORKFILENAME);
        assertTrue(firstDirArchiveInfo != null);

        DirectoryCoordinate secondDirectoryCoordinate = new DirectoryCoordinate(getProjectName(), getFeature1BranchName(), TestHelper.SUBPROJECT2_APPENDED_PATH);
        ArchiveDirManagerInterface secondDirManager = ArchiveDirManagerFactoryForServer.getInstance().getDirectoryManager(QVCSConstants.QVCS_SERVER_SERVER_NAME,
                secondDirectoryCoordinate, QVCSConstants.QVCS_SERVED_PROJECT_TYPE, QVCSConstants.QVCS_SERVER_USER, bogusResponseObject);
        assertTrue(secondDirManager != null);
        ArchiveInfoInterface secondDirArchiveInfo = secondDirManager.getArchiveInfo(TestHelper.SUBPROJECT2_FIRST_SHORTWORKFILENAME);
        assertTrue(secondDirArchiveInfo != null);

        DirectoryContents trunkContents = directoryContentsManager.getDirectoryContentsForTrunk(TestHelper.SUBPROJECT_APPENDED_PATH, firstDirManager.getDirectoryID(), bogusResponseObject);
        DirectoryContents feature1BranchContents = directoryContentsManager.getDirectoryContentsForFeatureBranch(featureBranch, TestHelper.SUBPROJECT_APPENDED_PATH,
                firstDirManager.getDirectoryID(), bogusResponseObject);
        assertEquals(trunkContents.getFiles().size(), feature1BranchContents.getFiles().size());

        ServerTransactionManager.getInstance().clientEndTransaction(bogusResponseObject);
    }

    /**
     * Move a file on one branch, and then move into that same directory on the trunk.
     *
     * @throws QVCSException if there is a QVCS problem.
     * @throws IOException for IO problems.
     */
    public void test2MoveOnOneBranchThenMoveOnTrunk() throws QVCSException, IOException {
        setUp();
        DirectoryCoordinate originTrunkDirectoryCoordinate = new DirectoryCoordinate(getProjectName(), getFeature1BranchName(), "");
        ArchiveDirManagerInterface originTrunkDirManager = ArchiveDirManagerFactoryForServer.getInstance().getDirectoryManager(QVCSConstants.QVCS_SERVER_SERVER_NAME,
                originTrunkDirectoryCoordinate, QVCSConstants.QVCS_SERVED_PROJECT_TYPE, QVCSConstants.QVCS_SERVER_USER, bogusResponseObject);
        assertTrue(originTrunkDirManager != null);
        int trunkRootDirFileCount = originTrunkDirManager.getArchiveInfoCollection().size();
        ArchiveInfoInterface originArchiveInfo = originTrunkDirManager.getArchiveInfo(TestHelper.SECOND_SHORTWORKFILENAME);
        ArchiveInfoInterface origin3ArchiveInfo = originTrunkDirManager.getArchiveInfo(TestHelper.THIRD_SHORTWORKFILENAME);
        assertNotNull(originArchiveInfo);

        DirectoryCoordinate destinationDirectoryCoordinate = new DirectoryCoordinate(getProjectName(), getFeature1BranchName(), TestHelper.SUBPROJECT_APPENDED_PATH);
        ArchiveDirManagerInterface destinationTrunkDirManager = ArchiveDirManagerFactoryForServer.getInstance().getDirectoryManager(QVCSConstants.QVCS_SERVER_SERVER_NAME,
                destinationDirectoryCoordinate, QVCSConstants.QVCS_SERVED_PROJECT_TYPE, QVCSConstants.QVCS_SERVER_USER, bogusResponseObject);
        assertNotNull(destinationTrunkDirManager);
        int trunkDestinationDirOriginalFileCount = destinationTrunkDirManager.getArchiveInfoCollection().size();

        // Move the file on the branch.
        directoryContentsManager.moveFileOnFeatureBranch(getFeature1BranchName(), originTrunkDirManager.getDirectoryID(), destinationTrunkDirManager.getDirectoryID(),
                originArchiveInfo.getFileID(), bogusResponseObject);
        ServerTransactionManager.getInstance().clientEndTransaction(bogusResponseObject);

        // Move a different file on the trunk, and make sure that we update the branch's directory contents.
        ServerTransactionManager.getInstance().clientBeginTransaction(bogusResponseObject);
        DirectoryCoordinate origin2TrunkDirCoordinate = new DirectoryCoordinate(getProjectName(), getFeature1BranchName(), TestHelper.SUBPROJECT2_APPENDED_PATH);
        ArchiveDirManagerInterface origin2TrunkDirManager = ArchiveDirManagerFactoryForServer.getInstance().getDirectoryManager(QVCSConstants.QVCS_SERVER_SERVER_NAME,
                origin2TrunkDirCoordinate, QVCSConstants.QVCS_SERVED_PROJECT_TYPE, QVCSConstants.QVCS_SERVER_USER, bogusResponseObject);
        assertNotNull(origin2TrunkDirManager);
        ArchiveInfoInterface origin2ArchiveInfo = origin2TrunkDirManager.getArchiveInfo(TestHelper.SUBPROJECT2_FIRST_SHORTWORKFILENAME);
        assertNotNull(origin2ArchiveInfo);

        directoryContentsManager.moveFileOnTrunk(QVCSConstants.QVCS_TRUNK_BRANCH, origin2TrunkDirManager.getDirectoryID(), destinationTrunkDirManager.getDirectoryID(), origin2ArchiveInfo.getFileID(),
                bogusResponseObject);
        ServerTransactionManager.getInstance().clientEndTransaction(bogusResponseObject);

        // Verify that the directory contents contain what we expect.
        ServerTransactionManager.getInstance().clientBeginTransaction(bogusResponseObject);
        DirectoryContents trunkContents = directoryContentsManager.getDirectoryContentsForTrunk("", originTrunkDirManager.getDirectoryID(), bogusResponseObject);
        assertEquals("Unexpected file count for root directory on trunk", trunkRootDirFileCount, trunkContents.getFiles().size());

        ProjectBranch featureBranch = BranchManager.getInstance().getBranch(getProjectName(), getFeature1BranchName());
        DirectoryContents feature1BranchRooDirContents = directoryContentsManager.getDirectoryContentsForFeatureBranch(featureBranch, "", originTrunkDirManager.getDirectoryID(),
                bogusResponseObject);
        assertEquals("Unexpected file count for branch root directory", trunkRootDirFileCount - 1, feature1BranchRooDirContents.getFiles().size());

        DirectoryContents feature1BranchDestDirContents = directoryContentsManager.getDirectoryContentsForFeatureBranch(featureBranch, TestHelper.SUBPROJECT_APPENDED_PATH,
                destinationTrunkDirManager.getDirectoryID(), bogusResponseObject);
        assertEquals("Unexpected file count for branch destination directory", trunkDestinationDirOriginalFileCount + 2, feature1BranchDestDirContents.getFiles().size());
        ServerTransactionManager.getInstance().clientEndTransaction(bogusResponseObject);

        // Move another file on the trunk.
        ServerTransactionManager.getInstance().clientBeginTransaction(bogusResponseObject);
        directoryContentsManager.moveFileOnTrunk(QVCSConstants.QVCS_TRUNK_BRANCH, originTrunkDirManager.getDirectoryID(), destinationTrunkDirManager.getDirectoryID(), origin3ArchiveInfo.getFileID(),
                bogusResponseObject);
        ServerTransactionManager.getInstance().clientEndTransaction(bogusResponseObject);

        // Verify that the results are what we expect.
        ServerTransactionManager.getInstance().clientBeginTransaction(bogusResponseObject);
        feature1BranchDestDirContents = directoryContentsManager.getDirectoryContentsForFeatureBranch(featureBranch, TestHelper.SUBPROJECT_APPENDED_PATH,
                destinationTrunkDirManager.getDirectoryID(), bogusResponseObject);
        assertEquals("Unexpected file count for branch destination directory", trunkDestinationDirOriginalFileCount + 3, feature1BranchDestDirContents.getFiles().size());
        assertTrue("Does not contain file moved on trunk", feature1BranchDestDirContents.containsFileID(origin3ArchiveInfo.getFileID()));
        ServerTransactionManager.getInstance().clientEndTransaction(bogusResponseObject);
    }
}
