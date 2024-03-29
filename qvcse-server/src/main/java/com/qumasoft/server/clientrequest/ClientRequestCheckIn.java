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
package com.qumasoft.server.clientrequest;

import com.qumasoft.qvcslib.DirectoryCoordinate;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.QVCSRuntimeException;
import com.qumasoft.qvcslib.ServerResponseFactoryInterface;
import com.qumasoft.qvcslib.SkinnyLogfileInfo;
import com.qumasoft.qvcslib.Utility;
import com.qumasoft.qvcslib.commandargs.CheckInCommandArgs;
import com.qumasoft.qvcslib.logfileaction.CheckIn;
import com.qumasoft.qvcslib.requestdata.ClientRequestCheckInData;
import com.qumasoft.qvcslib.response.AbstractServerResponse;
import com.qumasoft.qvcslib.response.ServerResponseCheckIn;
import com.qumasoft.qvcslib.response.ServerResponseMessage;
import com.qumasoft.server.ActivityJournalManager;
import com.qumasoft.server.NotificationManager;
import com.qvcsos.server.DatabaseManager;
import com.qvcsos.server.SourceControlBehaviorManager;
import com.qvcsos.server.dataaccess.BranchDAO;
import com.qvcsos.server.dataaccess.FunctionalQueriesDAO;
import com.qvcsos.server.dataaccess.ProjectDAO;
import com.qvcsos.server.dataaccess.impl.BranchDAOImpl;
import com.qvcsos.server.dataaccess.impl.FunctionalQueriesDAOImpl;
import com.qvcsos.server.dataaccess.impl.ProjectDAOImpl;
import com.qvcsos.server.datamodel.Branch;
import com.qvcsos.server.datamodel.Project;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client request check in.
 * @author Jim Voris
 */
public class ClientRequestCheckIn extends AbstractClientRequest {
    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientRequestCheckIn.class);
    private final DatabaseManager databaseManager;
    private final String schemaName;

    /**
     * Creates a new instance of ClientRequestCheckIn.
     *
     * @param data the request data.
     */
    public ClientRequestCheckIn(ClientRequestCheckInData data) {
        this.databaseManager = DatabaseManager.getInstance();
        this.schemaName = databaseManager.getSchemaName();
        setRequest(data);
    }

    /**
     * Check in a new revision.
     *
     * @param userName the user's user name.
     * @param response identify the client.
     * @return an object to tell the user how things went.
     */
    @Override
    public AbstractServerResponse execute(String userName, ServerResponseFactoryInterface response) {
        SourceControlBehaviorManager sourceControlBehaviorManager = SourceControlBehaviorManager.getInstance();
        sourceControlBehaviorManager.setUserAndResponse(userName, response);
        ClientRequestCheckInData clientRequestCheckInData = (ClientRequestCheckInData) getRequest();
        java.io.File tempFile = null;
        ServerResponseCheckIn serverResponse;
        AbstractServerResponse returnObject = null;
        CheckInCommandArgs commandArgs = clientRequestCheckInData.getCommandArgs();
        String projectName = getRequest().getProjectName();
        String branchName = getRequest().getBranchName();
        String appendedPath = getRequest().getAppendedPath();
        DirectoryCoordinate dc = new DirectoryCoordinate(getRequest().getProjectName(), getRequest().getBranchName(), getRequest().getAppendedPath());
        FileOutputStream outputStream = null;
        Integer fileRevisionId;
        try {
            // Add revision to postgres database.
            tempFile = java.io.File.createTempFile("qvcsos-ci-", ".tmp");
            outputStream = new java.io.FileOutputStream(tempFile);
            Utility.writeDataToStream(clientRequestCheckInData.getBuffer(), outputStream);
            fileRevisionId = addRevisionToPostgres(commandArgs, tempFile);
            if (fileRevisionId != null) {
                // Things worked.  Set up the response object to contain the information the client needs.
                serverResponse = new ServerResponseCheckIn();
                serverResponse.setShortWorkfileName(commandArgs.getShortWorkfileName());
                serverResponse.setClientWorkfileName(commandArgs.getFullWorkfileName());
                serverResponse.setProjectName(projectName);
                serverResponse.setBranchName(branchName);
                serverResponse.setAppendedPath(appendedPath);
                serverResponse.setProtectWorkfileFlag(commandArgs.getProtectWorkfileFlag());
                serverResponse.setNewRevisionString(commandArgs.getNewRevisionString());
                serverResponse.setIndex(clientRequestCheckInData.getIndex());
                serverResponse.setSyncToken(getRequest().getSyncToken());
                FunctionalQueriesDAO functionalQueriesDAO = new FunctionalQueriesDAOImpl(schemaName);
                SkinnyLogfileInfo skinnyInfo = functionalQueriesDAO.getSkinnyLogfileInfo(fileRevisionId);
                commandArgs.setNewRevisionString(skinnyInfo.getDefaultRevisionString());
                skinnyInfo.setCacheIndex(clientRequestCheckInData.getIndex());
                serverResponse.setSkinnyLogfileInfo(skinnyInfo);

                returnObject = serverResponse;

                // Notify listeners.
                NotificationManager.getNotificationManager().notifySkinnyInfoListeners(dc, skinnyInfo, new CheckIn(clientRequestCheckInData.getCommandArgs()));

                // Add an entry to the server journal file.
                ActivityJournalManager.getInstance().addJournalEntry(buildJournalEntry(userName, commandArgs.getShortWorkfileName()));
            } else {
                if (fileRevisionId == null) {
                    // Explain the error.
                    ServerResponseMessage message = new ServerResponseMessage("No database row found for " + commandArgs.getShortWorkfileName(), projectName,
                            branchName, appendedPath,
                            ServerResponseMessage.HIGH_PRIORITY);
                    message.setShortWorkfileName(commandArgs.getShortWorkfileName());
                    returnObject = message;
                }
            }
        } catch (IOException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);

            ServerResponseMessage message = new ServerResponseMessage(e.getLocalizedMessage(), projectName, branchName, appendedPath,
                    ServerResponseMessage.HIGH_PRIORITY);
            message.setShortWorkfileName(commandArgs.getShortWorkfileName());
            returnObject = message;
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    LOGGER.warn(e.getLocalizedMessage(), e);
                }
            }
            if (tempFile != null) {
                tempFile.delete();
            }
        }
        sourceControlBehaviorManager.clearThreadLocals();
        if (returnObject != null) {
            returnObject.setSyncToken(getRequest().getSyncToken());
        }
        return returnObject;
    }

    private String buildJournalEntry(final String userName, final String shortWorkfileName) {
        ClientRequestCheckInData clientRequestCheckInData = (ClientRequestCheckInData) getRequest();
        CheckInCommandArgs commandArgs = clientRequestCheckInData.getCommandArgs();
        return "User: [" + userName + "] checked-in revision [" + commandArgs.getNewRevisionString() + "] of ["
                + Utility.formatFilenameForActivityJournal(getRequest().getProjectName(), getRequest().getBranchName(), getRequest().getAppendedPath(), shortWorkfileName) + "]";
    }

    private Integer addRevisionToPostgres(CheckInCommandArgs commandArgs, File tempFile) {
        Integer fileRevisionId;
        SourceControlBehaviorManager sourceControlBehaviorManager = SourceControlBehaviorManager.getInstance();
        try {
            ProjectDAO projectDAO = new ProjectDAOImpl(schemaName);
            Project project = projectDAO.findByProjectName(commandArgs.getProjectName());

            BranchDAO branchDAO = new BranchDAOImpl(schemaName);
            String branchName = commandArgs.getBranchName();
            if (branchName == null) {
                branchName = QVCSConstants.QVCS_TRUNK_BRANCH;
            }
            Branch branch = branchDAO.findByProjectIdAndBranchName(project.getId(), branchName);
            if (branch.getBranchTypeId() == QVCSConstants.QVCS_TAG_BASED_BRANCH_TYPE) {
                throw new QVCSRuntimeException("Checkins are not allowed on read-only branches!");
            }

            Integer fileId = getRequest().getFileID();
            Date workfileEditDate = commandArgs.getInputfileTimeStamp();
            Timestamp workfileEditTimestamp = new Timestamp(workfileEditDate.getTime());
            fileRevisionId = sourceControlBehaviorManager.addRevision(branch.getId(), fileId, tempFile, null,
                    workfileEditTimestamp, commandArgs.getCheckInComment());
            LOGGER.info("Added revision id: [{}] to file id: [{}]", fileRevisionId, fileId);
        } catch (SQLException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
            throw new RuntimeException(e);
        }
        return fileRevisionId;
    }
}
