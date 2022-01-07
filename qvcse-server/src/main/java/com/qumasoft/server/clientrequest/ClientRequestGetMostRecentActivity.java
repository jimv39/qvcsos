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
package com.qumasoft.server.clientrequest;

import com.qumasoft.qvcslib.DirectoryCoordinate;
import com.qumasoft.qvcslib.DirectoryCoordinateIds;
import com.qumasoft.qvcslib.ServerResponseFactoryInterface;
import com.qumasoft.qvcslib.requestdata.ClientRequestGetMostRecentActivityData;
import com.qumasoft.qvcslib.response.ServerResponseError;
import com.qumasoft.qvcslib.response.ServerResponseGetMostRecentActivity;
import com.qumasoft.qvcslib.response.ServerResponseInterface;
import com.qvcsos.server.DatabaseManager;
import com.qvcsos.server.SourceControlBehaviorManager;
import com.qvcsos.server.dataaccess.FunctionalQueriesDAO;
import com.qvcsos.server.dataaccess.impl.FunctionalQueriesDAOImpl;
import com.qvcsos.server.datamodel.Commit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Get most recent activity.
 * @author Jim Voris
 */
public class ClientRequestGetMostRecentActivity implements ClientRequestInterface {
    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientRequestGetMostRecentActivity.class);
    private final ClientRequestGetMostRecentActivityData request;
    private final DatabaseManager databaseManager;
    private final String schemaName;

    /**
     * Creates a new instance of ClientRequestGetMostRecentActivity.
     *
     * @param data instance of the super class that contains command line arguments, etc.
     */
    public ClientRequestGetMostRecentActivity(ClientRequestGetMostRecentActivityData data) {
        this.databaseManager = DatabaseManager.getInstance();
        this.schemaName = databaseManager.getSchemaName();
        request = data;
    }

    @Override
    public ServerResponseInterface execute(String userName, ServerResponseFactoryInterface response) {
        SourceControlBehaviorManager sourceControlBehaviorManager = SourceControlBehaviorManager.getInstance();
        sourceControlBehaviorManager.setUserAndResponse(userName, response);
        ServerResponseGetMostRecentActivity serverResponse;
        ServerResponseInterface returnObject;
        String projectName = request.getProjectName();
        String branchName = request.getBranchName();
        String appendedPath = request.getAppendedPath();
        DirectoryCoordinate directoryCoordinate = new DirectoryCoordinate(projectName, branchName, appendedPath);
        FunctionalQueriesDAO functionalQueriesDAO = new FunctionalQueriesDAOImpl(schemaName);
        DirectoryCoordinateIds directoryCoordinateIds = functionalQueriesDAO.getDirectoryCoordinateIds(directoryCoordinate);
        Commit newestFileRevisionCommitOnBranch = functionalQueriesDAO.findNewestFileRevisionCommitOnBranch(directoryCoordinateIds.getBranchId());
        if (newestFileRevisionCommitOnBranch != null) {
            serverResponse = new ServerResponseGetMostRecentActivity();
            serverResponse.setProjectName(projectName);
            serverResponse.setBranchName(branchName);
            serverResponse.setAppendedPath(appendedPath);
            serverResponse.setMostRecentActivityDate(newestFileRevisionCommitOnBranch.getCommitDate());
            returnObject = serverResponse;
        } else {
            // Return a command error.
            ServerResponseError error = new ServerResponseError("Directory not found for  ", projectName, branchName, appendedPath);
            returnObject = error;
        }
        sourceControlBehaviorManager.clearThreadLocals();
        return returnObject;
    }
}
