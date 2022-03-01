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
import com.qumasoft.qvcslib.commandargs.GetRevisionCommandArgs;
import com.qumasoft.qvcslib.requestdata.ClientRequestGetForVisualCompareData;
import com.qumasoft.qvcslib.response.ServerResponseError;
import com.qumasoft.qvcslib.response.ServerResponseGetForVisualCompare;
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
 * Get for visual compare.
 * @author Jim Voris
 */
public class ClientRequestGetForVisualCompare implements ClientRequestInterface {
    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientRequestGetForVisualCompare.class);
    private final ClientRequestGetForVisualCompareData request;
    private final DatabaseManager databaseManager;
    private final String schemaName;

    /**
     * Creates a new instance of ClientRequestFetchFileRevision.
     *
     * @param data the request data.
     */
    public ClientRequestGetForVisualCompare(ClientRequestGetForVisualCompareData data) {
        this.databaseManager = DatabaseManager.getInstance();
        this.schemaName = databaseManager.getSchemaName();
        request = data;
    }

    @Override
    public ServerResponseInterface execute(String userName, ServerResponseFactoryInterface response) {
        SourceControlBehaviorManager sourceControlBehaviorManager = SourceControlBehaviorManager.getInstance();
        sourceControlBehaviorManager.setUserAndResponse(userName, response);
        ServerResponseGetForVisualCompare serverResponse;
        ServerResponseInterface returnObject;
        GetRevisionCommandArgs commandArgs = request.getCommandArgs();
        String projectName = request.getProjectName();
        String branchName = request.getBranchName();
        String shortWorkfileName = commandArgs.getShortWorkfileName();
        String appendedPath = request.getAppendedPath();
        java.io.File postgresFetchedFile = null;
        FileInputStream fileInputStream = null;
        try {
            DirectoryCoordinate directoryCoordinate = new DirectoryCoordinate(projectName, branchName, appendedPath);
            FunctionalQueriesDAO functionalQueriesDAO = new FunctionalQueriesDAOImpl(schemaName);
            postgresFetchedFile = getRevisionFromPostgres(commandArgs);
            if (postgresFetchedFile != null) {
                    // Things worked.  Set up the response object to contain the information the client needs.
                    serverResponse = new ServerResponseGetForVisualCompare();

                    // Need to read the resulting file into a buffer that we can send to the client.
                    fileInputStream = new FileInputStream(postgresFetchedFile);
                    byte[] buffer = new byte[(int) postgresFetchedFile.length()];
                    fileInputStream.read(buffer);
                    serverResponse.setBuffer(buffer);

                // Send back more info.
                    LogfileInfo logfileInfo = functionalQueriesDAO.getLogfileInfo(directoryCoordinate, shortWorkfileName, request.getFileID());

                    serverResponse.setLogfileInfo(logfileInfo);
                    serverResponse.setClientOutputFileName(commandArgs.getOutputFileName());
                    serverResponse.setFullWorkfileName(commandArgs.getFullWorkfileName());
                    serverResponse.setProjectName(projectName);
                    serverResponse.setBranchName(branchName);
                    serverResponse.setAppendedPath(appendedPath);
                    serverResponse.setRevisionString(commandArgs.getRevisionString());
                    returnObject = serverResponse;
            } else {
                // Return a command error.
                ServerResponseError error = new ServerResponseError("Failed to get revision " + commandArgs.getRevisionString() + " for " + shortWorkfileName,
                        projectName, branchName, appendedPath);
                returnObject = error;
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
            if (postgresFetchedFile != null) {
                postgresFetchedFile.delete();
            }
        }
        sourceControlBehaviorManager.clearThreadLocals();
        return returnObject;
    }

    /**
     * Copied from the private method of the same name in ClientRequestGetRevisionForCompare.The code is almost identical.
     * The ideal would be to refactor both into a single method shared by both classes.
     * @param commandArgs the command arguments.
     * @return A java.io.File containing the fetched file revision.
     */
    public File getRevisionFromPostgres(GetRevisionCommandArgs commandArgs) {
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
            }
        }

        if (fetchIndex >= 0) {
            try {
                FileRevision fetchingRevision = fileRevisionList.get(fetchIndex);
                String fetchedRevisionString = String.format("%d.%d", fetchingRevision.getBranchId(), fetchingRevision.getId());
                commandArgs.setRevisionString(fetchedRevisionString);
                fetchedRevisionFile = sourceControlBehaviorManager.getFileRevision(fileRevisionList.get(fetchIndex).getId());
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
