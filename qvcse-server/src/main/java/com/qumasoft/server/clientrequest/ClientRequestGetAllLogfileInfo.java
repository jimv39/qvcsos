/*
 * Copyright 2021 Jim Voris.
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
import com.qumasoft.qvcslib.LogfileInfo;
import com.qumasoft.qvcslib.ServerResponseFactoryInterface;
import com.qumasoft.qvcslib.requestdata.ClientRequestGetAllLogfileInfoData;
import com.qumasoft.qvcslib.response.ServerResponseGetAllLogfileInfo;
import com.qumasoft.qvcslib.response.ServerResponseInterface;
import com.qumasoft.qvcslib.response.ServerResponseMessage;
import com.qvcsos.server.DatabaseManager;
import com.qvcsos.server.SourceControlBehaviorManager;
import com.qvcsos.server.dataaccess.FunctionalQueriesDAO;
import com.qvcsos.server.dataaccess.impl.FunctionalQueriesDAOImpl;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Jim Voris.
 */
public class ClientRequestGetAllLogfileInfo implements ClientRequestInterface {

    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientRequestGetAllLogfileInfo.class);
    private final ClientRequestGetAllLogfileInfoData request;
    private final DatabaseManager databaseManager;
    private final String schemaName;

    /**
     * Creates a new instance of ClientRequestGetAllLogfileInfo.
     *
     * @param data instance of super class that contains command line arguments,
     * etc.
     */
    public ClientRequestGetAllLogfileInfo(ClientRequestGetAllLogfileInfoData data) {
        this.databaseManager = DatabaseManager.getInstance();
        this.schemaName = databaseManager.getSchemaName();
        request = data;
    }

    @Override
    public ServerResponseInterface execute(String userName, ServerResponseFactoryInterface response) {
        SourceControlBehaviorManager sourceControlBehaviorManager = SourceControlBehaviorManager.getInstance();
        sourceControlBehaviorManager.setUserAndResponse(userName, response);
        ServerResponseGetAllLogfileInfo serverResponse;
        ServerResponseInterface returnObject;
        String projectName = request.getProjectName();
        String branchName = request.getBranchName();
        String appendedPath = request.getAppendedPath();
        String shortWorkfileName = request.getShortWorkfileName();
        try {
            DirectoryCoordinate directoryCoordinate = new DirectoryCoordinate(projectName, branchName, appendedPath);
            LogfileInfo builtFromDatabase = buildAllLogfileInfoFromDatabase(directoryCoordinate, shortWorkfileName);

            // Set up the response object to contain the information the client needs.
            serverResponse = new ServerResponseGetAllLogfileInfo();

            serverResponse.setProjectName(projectName);
            serverResponse.setBranchName(branchName);
            serverResponse.setAppendedPath(appendedPath);
            serverResponse.setShortWorkfileName(shortWorkfileName);
            serverResponse.setLogfileInfo(builtFromDatabase);
            returnObject = serverResponse;
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

    private LogfileInfo buildAllLogfileInfoFromDatabase(DirectoryCoordinate dc, String shortWorkfileName) {
        LogfileInfo builtFromDb = null;
        try {
            DatabaseManager.getInstance().getConnection();
            FunctionalQueriesDAO functionQueriesDAO = new FunctionalQueriesDAOImpl(schemaName);
            builtFromDb = functionQueriesDAO.getAllLogfileInfo(dc, shortWorkfileName, request.getFileID());
        } catch (SQLException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
            throw new RuntimeException(e);
        }
        return builtFromDb;
    }

}
