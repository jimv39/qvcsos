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
import com.qumasoft.qvcslib.ArchiveDirManagerInterface;
import com.qumasoft.qvcslib.ArchiveInfoInterface;
import com.qumasoft.qvcslib.ClientRequestResolveConflictFromParentBranchData;
import com.qumasoft.qvcslib.DirectoryCoordinate;
import com.qumasoft.qvcslib.LogFileOperationCheckInCommandArgs;
import com.qumasoft.qvcslib.LogFileOperationLockRevisionCommandArgs;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.QVCSException;
import com.qumasoft.qvcslib.RemoteViewProperties;
import com.qumasoft.qvcslib.ServerResponseFactoryInterface;
import com.qumasoft.qvcslib.ServerResponseInterface;
import com.qumasoft.qvcslib.ServerResponseResolveConflictFromParentBranch;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Unit test for client request resolve conflict from parent.
 *
 * @author Jim Voris
 */
public class ClientRequestResolveConflictFromParentBranchServerTest {

    private static ProjectView projectView = null;
    private static RemoteViewProperties translucentBranchProperties = null;

    /**
     * Default constructor.
     */
    public ClientRequestResolveConflictFromParentBranchServerTest() {
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
        TestHelper.stopServer();
        TestHelper.deleteViewStore();
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
            Logger.getLogger(ClientRequestResolveConflictFromParentBranchServerTest.class.getName()).log(Level.SEVERE, null, ex);
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

    static private String getShortWorkfileNameForBranchCheckIn() {
        return "TranslucentBranchQVCSEnterpriseServer.java";
    }

    static private String getShortWorkfileNameForTrunkCheckIn() {
        return "TrunkQVCSEnterpriseServer.java";
    }

    /**
     * Test of execute method, of class ClientRequestResolveConflictFromParentBranch.
     */
    @Test
    public void testExecute() {
        System.out.println("execute");
        try {
            lockRevisionOnTrunk();
            checkInRevisionOnTrunk(getShortWorkfileName());
            int fileId = checkInRevisionOnBranch();

            lockRevisionOnTrunk();
            checkInRevisionOnTrunk(getShortWorkfileNameForTrunkCheckIn());

            ClientRequestResolveConflictFromParentBranchData data = new ClientRequestResolveConflictFromParentBranchData();
            data.setProjectName(getProjectName());
            data.setViewName(getBranchName());
            data.setFileID(fileId);
            ClientRequestResolveConflictFromParentBranch instance = new ClientRequestResolveConflictFromParentBranch(data);
            // Wrap this work in a server transaction so the DirectoryContents
            // stuff will behave in a useful way...
            ServerResponseFactoryInterface bogusResponseObject = new BogusResponseObject();
            // Keep track that we're in a transaction.
            ServerTransactionManager.getInstance().clientBeginTransaction(bogusResponseObject);
            ServerResponseInterface serverResponse = instance.execute("JimVoris", bogusResponseObject);
            ServerTransactionManager.getInstance().clientEndTransaction(bogusResponseObject);
            if (serverResponse instanceof ServerResponseResolveConflictFromParentBranch) {
                ServerResponseResolveConflictFromParentBranch serverResponseResolveConflictFromParentBranch = (ServerResponseResolveConflictFromParentBranch) serverResponse;
                assertNotNull(serverResponseResolveConflictFromParentBranch.getMergedResultBuffer());
            } else {
                fail("Did not return ServerResponseResolveConflictFromParentBranch object.");
            }
        } catch (QVCSException e) {
            Logger.getLogger(ClientRequestResolveConflictFromParentBranchServerTest.class.getName()).log(Level.SEVERE, null, e);
            fail("Caught unexpected exception.");
        }
    }

    private int checkInRevisionOnBranch() throws QVCSException {
        String checkInFilename = getShortWorkfileNameForBranchCheckIn();
        LogFileOperationCheckInCommandArgs commandArgs = new LogFileOperationCheckInCommandArgs();
        commandArgs.setApplyLabelFlag(false);
        commandArgs.setCheckInComment("A test checkin comment");
        commandArgs.setCreateNewRevisionIfEqual(true);
        commandArgs.setFullWorkfileName(checkInFilename);
        commandArgs.setProjectName(getProjectName());
        commandArgs.setShortWorkfileName(checkInFilename);
        commandArgs.setUserName("JimVoris");
        commandArgs.setInputfileTimeStamp(new Date());
        // Wrap this work in a server transaction so the DirectoryContents
        // stuff will behave in a useful way...
        ServerResponseFactoryInterface bogusResponseObject = new BogusResponseObject();
        // Keep track that we're in a transaction.
        ServerTransactionManager.getInstance().clientBeginTransaction(bogusResponseObject);
        DirectoryCoordinate directoryCoordinate = new DirectoryCoordinate(getProjectName(), getBranchName(), "");
        ArchiveDirManagerInterface archiveDirManager = ArchiveDirManagerFactoryForServer.getInstance().getDirectoryManager(QVCSConstants.QVCS_SERVER_SERVER_NAME,
                directoryCoordinate, QVCSConstants.QVCS_SERVED_PROJECT_TYPE, QVCSConstants.QVCS_SERVER_USER, bogusResponseObject, true);
        ArchiveInfoForTranslucentBranch instance = (ArchiveInfoForTranslucentBranch) archiveDirManager.getArchiveInfo(getShortWorkfileName());
        boolean expResult = true;
        int beforeRevisionCount = instance.getRevisionCount();
        assertEquals(0, instance.getLockCount());
        boolean result = instance.checkInRevision(commandArgs, checkInFilename, false);
        ServerTransactionManager.getInstance().clientEndTransaction(bogusResponseObject);
        String tipRevision = instance.getDefaultRevisionString();
        assertEquals(tipRevision, "1.92.1.1");
        assertEquals(expResult, result);
        assertEquals(0, instance.getLockCount());
        assertEquals(beforeRevisionCount + 1, instance.getRevisionCount());

        // Capture the association of this file to this directory.
        FileIDDictionary.getInstance().saveFileIDInfo(getProjectName(), getBranchName(), instance.getFileID(), "", instance.getShortWorkfileName(),
                archiveDirManager.getDirectoryID());
        return instance.getFileID();
    }

    private void lockRevisionOnTrunk() throws QVCSException {
        String checkInFilename = getShortWorkfileNameForTrunkCheckIn();
        LogFileOperationLockRevisionCommandArgs commandArgs = new LogFileOperationLockRevisionCommandArgs();
        commandArgs.setFullWorkfileName(checkInFilename);
        commandArgs.setShortWorkfileName(getShortWorkfileName());
        commandArgs.setOutputFileName(checkInFilename);
        commandArgs.setRevisionString(QVCSConstants.QVCS_DEFAULT_REVISION);
        commandArgs.setUserName("JimVoris");
        // Wrap this work in a server transaction so the DirectoryContents
        // stuff will behave in a useful way...
        ServerResponseFactoryInterface bogusResponseObject = new BogusResponseObject();
        // Keep track that we're in a transaction.
        ServerTransactionManager.getInstance().clientBeginTransaction(bogusResponseObject);
        DirectoryCoordinate directoryCoordinate = new DirectoryCoordinate(getProjectName(), QVCSConstants.QVCS_TRUNK_VIEW, "");
        ArchiveDirManagerInterface archiveDirManager = ArchiveDirManagerFactoryForServer.getInstance().getDirectoryManager(QVCSConstants.QVCS_SERVER_SERVER_NAME,
                directoryCoordinate, QVCSConstants.QVCS_SERVED_PROJECT_TYPE, QVCSConstants.QVCS_SERVER_USER, bogusResponseObject, true);
        ArchiveInfoInterface instance = archiveDirManager.getArchiveInfo(getShortWorkfileName());
        boolean expResult = true;
        assertEquals(0, instance.getLockCount());
        boolean result = instance.lockRevision(commandArgs);
        ServerTransactionManager.getInstance().clientEndTransaction(bogusResponseObject);
        assertEquals(expResult, result);
        assertEquals(1, instance.getLockCount());
    }

    private void checkInRevisionOnTrunk(String checkInFilename) throws QVCSException {
        LogFileOperationCheckInCommandArgs commandArgs = new LogFileOperationCheckInCommandArgs();
        commandArgs.setApplyLabelFlag(false);
        commandArgs.setCheckInComment("A test checkin comment");
        commandArgs.setCreateNewRevisionIfEqual(true);
        commandArgs.setFullWorkfileName(checkInFilename);
        commandArgs.setProjectName(getProjectName());
        commandArgs.setShortWorkfileName(checkInFilename);
        commandArgs.setUserName("JimVoris");
        commandArgs.setInputfileTimeStamp(new Date());
        commandArgs.setLockedRevisionString(QVCSConstants.QVCS_DEFAULT_REVISION);
        // Wrap this work in a server transaction so the DirectoryContents
        // stuff will behave in a useful way...
        ServerResponseFactoryInterface bogusResponseObject = new BogusResponseObject();
        // Keep track that we're in a transaction.
        ServerTransactionManager.getInstance().clientBeginTransaction(bogusResponseObject);
        DirectoryCoordinate directoryCoordinate = new DirectoryCoordinate(getProjectName(), QVCSConstants.QVCS_TRUNK_VIEW, "");
        ArchiveDirManagerInterface archiveDirManager = ArchiveDirManagerFactoryForServer.getInstance().getDirectoryManager(QVCSConstants.QVCS_SERVER_SERVER_NAME,
                directoryCoordinate, QVCSConstants.QVCS_SERVED_PROJECT_TYPE, QVCSConstants.QVCS_SERVER_USER, bogusResponseObject, true);
        ArchiveInfoInterface instance = archiveDirManager.getArchiveInfo(getShortWorkfileName());
        boolean expResult = true;
        int beforeRevisionCount = instance.getRevisionCount();
        assertEquals(1, instance.getLockCount());
        boolean result = instance.checkInRevision(commandArgs, checkInFilename, false);
        ServerTransactionManager.getInstance().clientEndTransaction(bogusResponseObject);
        assertEquals(expResult, result);
        assertEquals(0, instance.getLockCount());
        assertEquals(beforeRevisionCount + 1, instance.getRevisionCount());

        // Capture the association of this file to this directory.
        FileIDDictionary.getInstance().saveFileIDInfo(getProjectName(), QVCSConstants.QVCS_TRUNK_VIEW, instance.getFileID(), "", instance.getShortWorkfileName(), archiveDirManager.getDirectoryID());
    }
}
