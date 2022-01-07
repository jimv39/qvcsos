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
import com.qumasoft.qvcslib.DirectoryCoordinateIds;
import com.qumasoft.qvcslib.ServerResponseFactoryInterface;
import com.qumasoft.qvcslib.SkinnyLogfileInfo;
import com.qumasoft.qvcslib.Utility;
import com.qumasoft.qvcslib.requestdata.ClientRequestDeleteDirectoryData;
import com.qumasoft.qvcslib.response.ServerResponseInterface;
import com.qumasoft.qvcslib.response.ServerResponseMessage;
import com.qumasoft.qvcslib.response.ServerResponseProjectControl;
import com.qumasoft.server.ActivityJournalManager;
import com.qumasoft.server.QVCSEnterpriseServer;
import com.qumasoft.server.RolePrivilegesManager;
import com.qvcsos.server.DatabaseManager;
import com.qvcsos.server.SourceControlBehaviorManager;
import com.qvcsos.server.dataaccess.DirectoryLocationDAO;
import com.qvcsos.server.dataaccess.FunctionalQueriesDAO;
import com.qvcsos.server.dataaccess.impl.DirectoryLocationDAOImpl;
import com.qvcsos.server.dataaccess.impl.FunctionalQueriesDAOImpl;
import com.qvcsos.server.datamodel.Branch;
import com.qvcsos.server.datamodel.DirectoryLocation;
import java.sql.SQLException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client request delete directory. Only allow the delete if the directory is empty.
 * @author Jim Voris
 */
public class ClientRequestDeleteDirectory implements ClientRequestInterface {
    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientRequestDeleteDirectory.class);
    private final ClientRequestDeleteDirectoryData request;
    private final DatabaseManager databaseManager;
    private final String schemaName;

    /**
     * Creates a new instance of ClientRequestDeleteDirectory.
     *
     * @param data the request data.
     */
    public ClientRequestDeleteDirectory(ClientRequestDeleteDirectoryData data) {
        this.databaseManager = DatabaseManager.getInstance();
        this.schemaName = databaseManager.getSchemaName();
        request = data;
    }

    @Override
    public ServerResponseInterface execute(String userName, ServerResponseFactoryInterface response) {
        SourceControlBehaviorManager sourceControlBehaviorManager = SourceControlBehaviorManager.getInstance();
        sourceControlBehaviorManager.setUserAndResponse(userName, response);
        ServerResponseProjectControl serverResponse;
        ServerResponseInterface returnObject = null;
        String projectName = request.getProjectName();
        String branchName = request.getBranchName();
        String appendedPath = request.getAppendedPath();
        try {
            DirectoryCoordinate directoryCoordinate = new DirectoryCoordinate(projectName, branchName, appendedPath);
            FunctionalQueriesDAO functionalQueriesDAO = new FunctionalQueriesDAOImpl(schemaName);
            DirectoryCoordinateIds dcIds = functionalQueriesDAO.getDirectoryCoordinateIds(directoryCoordinate);
            List<SkinnyLogfileInfo> skinnyList = functionalQueriesDAO.getSkinnyLogfileInfo(dcIds.getBranchId(), dcIds.getDirectoryId());
            // Make sure the directory is empty of files...
            if (skinnyList.isEmpty()) {
                // Make sure there are no child directories...
                List<Branch> branchArray = functionalQueriesDAO.getBranchAncestryList(dcIds.getBranchId());
                DirectoryLocationDAO directoryLocationDAO = new DirectoryLocationDAOImpl(schemaName);
                List<DirectoryLocation> dlList = functionalQueriesDAO.findChildDirectoryLocations(branchArray, dcIds.getDirectoryLocationId());
                if (dlList.isEmpty()) {
                    // The delete can proceed...
                    sourceControlBehaviorManager.deleteDirectory(dcIds.getBranchId(), dcIds.getDirectoryLocationId());
                    for (ServerResponseFactoryInterface responseFactory : QVCSEnterpriseServer.getConnectedUsers()) {
                        // And let users who have the privilege know about this deleted directory.
                        if (RolePrivilegesManager.getInstance().isUserPrivileged(projectName, responseFactory.getUserName(), RolePrivilegesManager.GET)) {
                            serverResponse = new ServerResponseProjectControl();
                            serverResponse.setAddFlag(false);
                            serverResponse.setRemoveFlag(true);
                            serverResponse.setProjectName(projectName);
                            serverResponse.setBranchName(branchName);
                            serverResponse.setDirectorySegments(Utility.getDirectorySegments(appendedPath));
                            serverResponse.setServerName(responseFactory.getServerName());
                            responseFactory.createServerResponse(serverResponse);
                            LOGGER.info("Sending deleted directory info to: [" + responseFactory.getUserName() + "]");
                        }
                    }
                    ActivityJournalManager.getInstance().addJournalEntry("User: [" + userName + "] deleted directory: [" + projectName + "//" + appendedPath + "]");
                } else {
                    // Oops. There are child directories. The delete is not allowed.
                    ServerResponseMessage message = new ServerResponseMessage("Directory has child directories for [" + appendedPath + "]", projectName, branchName,
                            appendedPath, ServerResponseMessage.HIGH_PRIORITY);
                    message.setShortWorkfileName("");
                    returnObject = message;
                }
            } else {
                // The directory is not empty. We won't allow the user to delete it.
                ServerResponseMessage message = new ServerResponseMessage("You cannot delete a directory unless it is empty.", projectName, branchName, appendedPath,
                        ServerResponseMessage.HIGH_PRIORITY);
                message.setShortWorkfileName("");
                returnObject = message;
            }
        } catch (SQLException e) {
            ServerResponseMessage message = new ServerResponseMessage(e.getLocalizedMessage(), projectName, branchName, appendedPath, ServerResponseMessage.HIGH_PRIORITY);
            message.setShortWorkfileName("");
            returnObject = message;
            LOGGER.warn(e.getLocalizedMessage(), e);
        }
        sourceControlBehaviorManager.clearThreadLocals();
        return returnObject;
    }

}
