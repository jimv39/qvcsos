/*   Copyright 2004-2021 Jim Voris
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
import com.qumasoft.qvcslib.requestdata.ClientRequestGetLogfileInfoData;
import com.qumasoft.qvcslib.response.ServerResponseGetLogfileInfo;
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
import com.qvcsos.server.datamodel.Project;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Get Logfile info.
 * @author Jim Voris
 */
public class ClientRequestGetLogfileInfo implements ClientRequestInterface {
    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientRequestGetLogfileInfo.class);
    private final ClientRequestGetLogfileInfoData request;
    private final DatabaseManager databaseManager;
    private final String schemaName;

    /**
     * Creates a new instance of ClientRequestGetLogfileInfo.
     *
     * @param data instance of super class that contains command line arguments, etc.
     */
    public ClientRequestGetLogfileInfo(ClientRequestGetLogfileInfoData data) {
        this.databaseManager = DatabaseManager.getInstance();
        this.schemaName = databaseManager.getSchemaName();
        request = data;
    }

    @Override
    public ServerResponseInterface execute(String userName, ServerResponseFactoryInterface response) {
        SourceControlBehaviorManager sourceControlBehaviorManager = SourceControlBehaviorManager.getInstance();
        sourceControlBehaviorManager.setUserAndResponse(userName, response);
        ServerResponseGetLogfileInfo serverResponse;
        ServerResponseInterface returnObject;
        String projectName = request.getProjectName();
        String branchName = request.getBranchName();
        String appendedPath = request.getAppendedPath();
        String shortWorkfileName = request.getShortWorkfileName();
        Integer fileId = request.getFileID();
        try {
            DirectoryCoordinate directoryCoordinate = new DirectoryCoordinate(projectName, branchName, appendedPath);
            LogfileInfo builtFromDatabase = buildLogfileInfoFromDatabase(directoryCoordinate, shortWorkfileName, fileId);
            if (builtFromDatabase != null) {
                // Set up the response object to contain the information the client needs.
                serverResponse = new ServerResponseGetLogfileInfo();

                serverResponse.setLogfileInfo(builtFromDatabase);
                serverResponse.setProjectName(projectName);
                serverResponse.setBranchName(branchName);
                serverResponse.setAppendedPath(appendedPath);
                serverResponse.setShortWorkfileName(shortWorkfileName);
                returnObject = serverResponse;
            } else {
                ServerResponseMessage message = new ServerResponseMessage("Data not found for " + shortWorkfileName, projectName, branchName, appendedPath,
                        ServerResponseMessage.HIGH_PRIORITY);
                message.setShortWorkfileName(shortWorkfileName);
                returnObject = message;
            }
        } catch (Exception e) {
            LOGGER.warn(e.getLocalizedMessage(), e);

            ServerResponseMessage message = new ServerResponseMessage("Caught exception trying to get detailed information for " + shortWorkfileName
                    + ". Exception string: " + e.getMessage(),
                    projectName, branchName, appendedPath, ServerResponseMessage.HIGH_PRIORITY);
            message.setShortWorkfileName(shortWorkfileName);
            returnObject = message;
        }
        sourceControlBehaviorManager.clearThreadLocals();
        return returnObject;
    }

    private LogfileInfo buildLogfileInfoFromDatabase(DirectoryCoordinate dc, String shortWorkfileName, Integer fileId) {
        LogfileInfo builtFromDb = null;
        try {
            DatabaseManager.getInstance().getConnection();

            ProjectDAO projectDAO = new ProjectDAOImpl(schemaName);
            Project project = projectDAO.findByProjectName(dc.getProjectName());

            BranchDAO branchDAO = new BranchDAOImpl(schemaName);
            Branch branch = branchDAO.findByProjectIdAndBranchName(project.getId(), dc.getBranchName());

            switch (branch.getBranchTypeId()) {
                case QVCSConstants.QVCS_TRUNK_BRANCH_TYPE:
                case QVCSConstants.QVCS_FEATURE_BRANCH_TYPE:
                case QVCSConstants.QVCS_TAG_BASED_BRANCH_TYPE:
                case QVCSConstants.QVCS_RELEASE_BRANCH_TYPE:
                    FunctionalQueriesDAO functionQueriesDAO = new FunctionalQueriesDAOImpl(schemaName);
                    builtFromDb = functionQueriesDAO.getLogfileInfo(dc, shortWorkfileName, fileId);
                    break;
                default:
                    break;
            }
        } catch (SQLException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
            throw new RuntimeException(e);
        }
        return builtFromDb;
    }
}
