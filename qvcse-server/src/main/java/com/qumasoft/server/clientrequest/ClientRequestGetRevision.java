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
import com.qumasoft.qvcslib.LogfileInfo;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.ServerResponseFactoryInterface;
import com.qumasoft.qvcslib.SkinnyLogfileInfo;
import com.qumasoft.qvcslib.Utility;
import com.qumasoft.qvcslib.commandargs.GetRevisionCommandArgs;
import com.qumasoft.qvcslib.requestdata.ClientRequestGetRevisionData;
import com.qumasoft.qvcslib.response.ServerResponseError;
import com.qumasoft.qvcslib.response.ServerResponseGetRevision;
import com.qumasoft.qvcslib.response.ServerResponseInterface;
import com.qumasoft.qvcslib.response.ServerResponseMessage;
import com.qvcsos.server.DatabaseManager;
import com.qvcsos.server.SourceControlBehaviorManager;
import com.qvcsos.server.dataaccess.BranchDAO;
import com.qvcsos.server.dataaccess.FunctionalQueriesDAO;
import com.qvcsos.server.dataaccess.ProjectDAO;
import com.qvcsos.server.dataaccess.impl.BranchDAOImpl;
import com.qvcsos.server.dataaccess.impl.FunctionalQueriesDAOImpl;
import com.qvcsos.server.dataaccess.impl.ProjectDAOImpl;
import com.qvcsos.server.datamodel.Branch;
import com.qvcsos.server.datamodel.FileRevision;
import com.qvcsos.server.datamodel.Project;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Get a revision.
 * @author Jim Voris
 */
public class ClientRequestGetRevision implements ClientRequestInterface {
    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientRequestGetRevision.class);
    private final ClientRequestGetRevisionData request;
    private final DatabaseManager databaseManager;
    private final String schemaName;

    /**
     * Creates a new instance of ClientRequestFetchFileRevision.
     *
     * @param data the request data.
     */
    public ClientRequestGetRevision(ClientRequestGetRevisionData data) {
        this.databaseManager = DatabaseManager.getInstance();
        this.schemaName = databaseManager.getSchemaName();
        request = data;
    }

    @Override
    public ServerResponseInterface execute(String userName, ServerResponseFactoryInterface response) {
        SourceControlBehaviorManager sourceControlBehaviorManager = SourceControlBehaviorManager.getInstance();
        sourceControlBehaviorManager.setUserAndResponse(userName, response);
        ServerResponseGetRevision serverResponse;
        ServerResponseInterface returnObject;
        String projectName = request.getProjectName();
        String branchName = request.getBranchName();
        String appendedPath = request.getAppendedPath();
        GetRevisionCommandArgs commandArgs = request.getCommandArgs();
        FileInputStream fileInputStream = null;
        try {
            DirectoryCoordinate directoryCoordinate = new DirectoryCoordinate(projectName, branchName, appendedPath);
            FunctionalQueriesDAO functionalQueriesDAO = new FunctionalQueriesDAOImpl(schemaName);
            java.io.File postgresFetchedFile = getRevisionFromPostgres(commandArgs);
            if (postgresFetchedFile != null) {
                SkinnyLogfileInfo skinnyInfo = functionalQueriesDAO.getSkinnyLogfileInfoForGet(commandArgs.getFileRevisionId());
                serverResponse = new ServerResponseGetRevision();

                // Need to read the resulting file into a buffer that we can send to the client.
                fileInputStream = new FileInputStream(postgresFetchedFile);
                byte[] buffer = new byte[(int) postgresFetchedFile.length()];
                Utility.readDataFromStream(buffer, fileInputStream);
                serverResponse.setBuffer(buffer);

                serverResponse.setSkinnyLogfileInfo(skinnyInfo);
                serverResponse.setClientWorkfileName(commandArgs.getOutputFileName());
                serverResponse.setShortWorkfileName(commandArgs.getShortWorkfileName());
                serverResponse.setProjectName(projectName);
                serverResponse.setBranchName(branchName);
                serverResponse.setAppendedPath(appendedPath);
                serverResponse.setRevisionString(commandArgs.getRevisionString());
                serverResponse.setOverwriteBehavior(commandArgs.getOverwriteBehavior());
                serverResponse.setTimestampBehavior(commandArgs.getTimestampBehavior());
                if (request.getSyncToken() != null) {
                    serverResponse.setSyncToken(request.getSyncToken());
                }

                // Send back more info.
                LogfileInfo logfileInfo = functionalQueriesDAO.getLogfileInfo(directoryCoordinate, commandArgs.getShortWorkfileName(), request.getFileID());
                serverResponse.setLogfileInfo(logfileInfo);
                returnObject = serverResponse;
            } else {
                // Return a command error.
                LOGGER.warn("Failed to fetch revision for: [{}]", commandArgs.getShortWorkfileName());
                ServerResponseError error;
                if ((commandArgs.getFailureReason() != null) && (commandArgs.getFailureReason().length() > 0)) {
                    error = new ServerResponseError("Failed to get revision for " + commandArgs.getShortWorkfileName() + ". " + commandArgs.getFailureReason(), projectName,
                            branchName, appendedPath);
                } else {
                    error = new ServerResponseError("Failed to get revision " + commandArgs.getRevisionString() + " for " + commandArgs.getShortWorkfileName(), projectName,
                            branchName, appendedPath);
                }
                returnObject = error;
            }
            if (postgresFetchedFile != null) {
                postgresFetchedFile.delete();
            }
        } catch (IOException e) {
            ServerResponseMessage message = new ServerResponseMessage(e.getLocalizedMessage(), projectName, branchName, appendedPath, ServerResponseMessage.HIGH_PRIORITY);
            message.setShortWorkfileName(commandArgs.getShortWorkfileName());
            returnObject = message;
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    LOGGER.warn(e.getLocalizedMessage(), e);
                }
            }
        }
        sourceControlBehaviorManager.clearThreadLocals();
        return returnObject;
    }

    private File getRevisionFromPostgres(GetRevisionCommandArgs commandArgs) {
        java.io.File fetchedRevisionFile = null;
        SourceControlBehaviorManager sourceControlBehaviorManager = SourceControlBehaviorManager.getInstance();

        ProjectDAO projectDAO = new ProjectDAOImpl(schemaName);
        Project project = projectDAO.findByProjectName(request.getProjectName());

        BranchDAO branchDAO = new BranchDAOImpl(schemaName);
        String branchName = request.getBranchName();
        if (branchName == null) {
            branchName = QVCSConstants.QVCS_TRUNK_BRANCH;
        }
        Branch branch = branchDAO.findByProjectIdAndBranchName(project.getId(), branchName);

        List<FileRevision> fileRevisionList = sourceControlBehaviorManager.getFileRevisionList(branch, request.getFileID());

        int fetchIndex = -1;
        if (0 == commandArgs.getRevisionString().compareTo(QVCSConstants.QVCS_DEFAULT_REVISION)) {
            fetchIndex = 0;
        } else {
            int index = 0;
            String[] branchRevIdList = commandArgs.getRevisionString().split("\\.");
            Integer requestedRevisionId = Integer.valueOf(branchRevIdList[1]);
            for (FileRevision fileRevision : fileRevisionList) {
                if (fileRevision.getId().intValue() == requestedRevisionId.intValue()) {
                    fetchIndex = index;
                    break;
                }
                index++;
            }
            if (fetchIndex == -1) {
                LOGGER.warn("Requested revision: [{}] not found!", commandArgs.getRevisionString());
                commandArgs.setFailureReason("Requested revision: [" + commandArgs.getRevisionString() + "] not found!");
            }
        }

        if (fetchIndex >= 0) {
            try {
                FileRevision fetchingRevision = fileRevisionList.get(fetchIndex);
                String fetchedRevisionString = String.format("%d.%d", fetchingRevision.getBranchId(), fetchingRevision.getId());
                commandArgs.setRevisionString(fetchedRevisionString);
                commandArgs.setFileRevisionId(fetchingRevision.getId());
                fetchedRevisionFile = sourceControlBehaviorManager.getFileRevision(fetchingRevision.getId());
                LOGGER.info("File revision: [{}] for file: [{}] fetched from postgres returned in file: [{}]",
                        fetchedRevisionString, commandArgs.getShortWorkfileName(), fetchedRevisionFile.getAbsolutePath());
            } catch (SQLException e) {
                LOGGER.warn(e.getLocalizedMessage(), e);
                throw new RuntimeException(e);
            }
        }
        return fetchedRevisionFile;
    }
}
