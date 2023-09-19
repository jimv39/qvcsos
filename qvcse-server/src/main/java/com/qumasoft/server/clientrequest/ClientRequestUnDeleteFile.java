/*
 * Copyright 2022 Jim Voris.
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

import com.qumasoft.qvcslib.DirectoryCoordinate;
import com.qumasoft.qvcslib.DirectoryCoordinateIds;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.ServerResponseFactoryInterface;
import com.qumasoft.qvcslib.SkinnyLogfileInfo;
import com.qumasoft.qvcslib.Utility;
import com.qumasoft.qvcslib.logfileaction.AddFile;
import com.qumasoft.qvcslib.requestdata.ClientRequestUnDeleteFileData;
import com.qumasoft.qvcslib.response.AbstractServerResponse;
import com.qumasoft.qvcslib.response.ServerResponseError;
import com.qumasoft.qvcslib.response.ServerResponseInterface;
import com.qumasoft.qvcslib.response.ServerResponseMoveFile;
import com.qumasoft.server.ActivityJournalManager;
import com.qumasoft.server.MergeTypeHelper;
import com.qumasoft.server.NotificationManager;
import com.qvcsos.server.DatabaseManager;
import com.qvcsos.server.SourceControlBehaviorManager;
import com.qvcsos.server.dataaccess.BranchDAO;
import com.qvcsos.server.dataaccess.FileNameHistoryDAO;
import com.qvcsos.server.dataaccess.FunctionalQueriesDAO;
import com.qvcsos.server.dataaccess.ProjectDAO;
import com.qvcsos.server.dataaccess.impl.BranchDAOImpl;
import com.qvcsos.server.dataaccess.impl.FileNameHistoryDAOImpl;
import com.qvcsos.server.dataaccess.impl.FunctionalQueriesDAOImpl;
import com.qvcsos.server.dataaccess.impl.ProjectDAOImpl;
import com.qvcsos.server.datamodel.Branch;
import com.qvcsos.server.datamodel.FileNameHistory;
import com.qvcsos.server.datamodel.FileRevision;
import com.qvcsos.server.datamodel.Project;
import java.sql.SQLException;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Jim Voris
 */
public class ClientRequestUnDeleteFile extends AbstractClientRequest {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientRequestMoveFile.class);
    private final DatabaseManager databaseManager;
    private final String schemaName;

    public ClientRequestUnDeleteFile(ClientRequestUnDeleteFileData data) {
        this.databaseManager = DatabaseManager.getInstance();
        this.schemaName = databaseManager.getSchemaName();
        setRequest(data);
    }

    /**
     * Undelete the given file.
     *
     * @param userName the user making the request.
     * @param response the response object that identifies the client
     * connection.
     * @return an object we'll send back to the client.
     */
    @Override
    public ServerResponseInterface execute(String userName, ServerResponseFactoryInterface response) {
        SourceControlBehaviorManager sourceControlBehaviorManager = SourceControlBehaviorManager.getInstance();
        sourceControlBehaviorManager.setUserAndResponse(userName, response);
        ClientRequestUnDeleteFileData clientRequestUnDeleteFileData = (ClientRequestUnDeleteFileData) getRequest();
        AbstractServerResponse returnObject;

        String projectName = getRequest().getProjectName();
        String branchName = getRequest().getBranchName();
        String shortWorkfileName = getRequest().getShortWorkfileName();
        try {
            FunctionalQueriesDAO functionalQueriesDAO = new FunctionalQueriesDAOImpl(schemaName);

            DirectoryCoordinate originCoordinate = new DirectoryCoordinate(projectName, branchName, QVCSConstants.QVCSOS_CEMETERY_FAKE_APPENDED_PATH);
            DirectoryCoordinateIds originIds = functionalQueriesDAO.getDirectoryCoordinateIds(originCoordinate);

            DirectoryCoordinate destinationCoordinate = deduceDestinationDirectoryCoordinate(clientRequestUnDeleteFileData);

            // And UnDelete the file...
            Integer fileNameId = sourceControlBehaviorManager.unDeleteFile(originIds.getBranchId(), getRequest().getFileID());

            // Find the file's tip revision...
            FileRevision fileRevision = functionalQueriesDAO.findBranchTipRevisionByBranchIdAndFileId(originIds.getBranchId(), getRequest().getFileID());

            // Send a response to the user (note that a notification will be sent before this response).
            if (fileNameId != null) {
                Properties fakeProperties = new Properties();
                fakeProperties.setProperty("QVCS_IGNORECASEFLAG", QVCSConstants.QVCS_NO);
                ServerResponseMoveFile serverResponseMoveFile = new ServerResponseMoveFile();
                serverResponseMoveFile.setServerName(response.getServerName());
                serverResponseMoveFile.setProjectName(projectName);
                serverResponseMoveFile.setBranchName(branchName);
                serverResponseMoveFile.setProjectProperties(fakeProperties);
                serverResponseMoveFile.setOriginAppendedPath(QVCSConstants.QVCSOS_CEMETERY_FAKE_APPENDED_PATH);
                serverResponseMoveFile.setDestinationAppendedPath(destinationCoordinate.getAppendedPath());
                serverResponseMoveFile.setShortWorkfileName(shortWorkfileName);

                SkinnyLogfileInfo skinnyInfo = functionalQueriesDAO.getSkinnyLogfileInfo(fileRevision.getId());
                serverResponseMoveFile.setSkinnyLogfileInfo(skinnyInfo);
                returnObject = serverResponseMoveFile;

                // Add an entry to the server journal file.
                String logMessage = buildJournalEntry(userName, destinationCoordinate.getAppendedPath());

                // We have to do the notifies as a remove then an add, instead of a move notify, since child branches
                // won't have an entry in their Cemetery. First send the remove from the cemetery...
//                NotificationManager.getNotificationManager().notifySkinnyInfoListeners(originCoordinate, skinnyInfo, new Remove(shortWorkfileName));

                // Notify listeners. Then add to the destination directory.
                NotificationManager.getNotificationManager().notifySkinnyInfoListeners(destinationCoordinate, skinnyInfo, new AddFile());

                ActivityJournalManager.getInstance().addJournalEntry(logMessage);
                LOGGER.info(logMessage);
            } else {
                // Return a command error.
                ServerResponseError error = new ServerResponseError("Data not found for " + shortWorkfileName, projectName, branchName, QVCSConstants.QVCSOS_CEMETERY_FAKE_APPENDED_PATH);
                returnObject = error;
            }

        } catch (SQLException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);

            // Return a command error.
            ServerResponseError error = new ServerResponseError("Caught exception trying to undelete " + shortWorkfileName + ". Exception string: " + e.getMessage(), projectName, branchName, "");
            returnObject = error;
        }
        sourceControlBehaviorManager.clearThreadLocals();
        returnObject.setSyncToken(getRequest().getSyncToken());
        return returnObject;
    }

    private DirectoryCoordinate deduceDestinationDirectoryCoordinate(ClientRequestUnDeleteFileData unDeleteFileData) {
        String appendedPath = "";

        // If the file's original location still exists, that's where it will go.
        // If the file's original location has been deleted, we'll put the file in the branch's root directory.
        ProjectDAO projectDAO = new ProjectDAOImpl(schemaName);
        Project project = projectDAO.findByProjectName(getRequest().getProjectName());

        BranchDAO branchDAO = new BranchDAOImpl(schemaName);
        Branch branch = branchDAO.findByProjectIdAndBranchName(project.getId(), getRequest().getBranchName());

        FileNameHistoryDAO fileNameHistoryDAO = new FileNameHistoryDAOImpl(schemaName);
        FileNameHistory fileNameHistory = fileNameHistoryDAO.findNewestFileNameOnBranchWithFileId(branch.getId(), getRequest().getFileID());
        if (fileNameHistory != null) {
            // Find the newest FileNameHistory record in the filename_history table for the given branchId, fileId;
            MergeTypeHelper mh = new MergeTypeHelper(getRequest().getUserName(), getRequest().getProjectName(), getRequest().getBranchName());
            appendedPath = mh.buildAppendedPath(unDeleteFileData.getFileID(), fileNameHistory.getCommitId());
        }

        DirectoryCoordinate dc = new DirectoryCoordinate(getRequest().getProjectName(), getRequest().getBranchName(), appendedPath);
        return dc;
    }

    private String buildJournalEntry(final String userName, String appendedPath) {
        return "User: [" + userName + "] UnDeleted file ["
                + Utility.formatFilenameForActivityJournal(getRequest().getProjectName(), getRequest().getBranchName(), appendedPath,
                        getRequest().getShortWorkfileName()) + "].";
    }

}
