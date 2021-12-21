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

import com.qumasoft.qvcslib.ServerResponseFactoryInterface;
import com.qumasoft.qvcslib.requestdata.ClientRequestGetUserCommitCommentsData;
import com.qumasoft.qvcslib.response.ServerResponseGetUserCommitComments;
import com.qumasoft.qvcslib.response.ServerResponseInterface;
import com.qvcsos.server.DatabaseManager;
import com.qvcsos.server.SourceControlBehaviorManager;
import com.qvcsos.server.dataaccess.FunctionalQueriesDAO;
import com.qvcsos.server.dataaccess.impl.FunctionalQueriesDAOImpl;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Jim Voris
 */
public class ClientRequestGetUserCommitComments implements ClientRequestInterface {
    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientRequestGetUserCommitComments.class);
    private final DatabaseManager databaseManager;
    private final String schemaName;
    private final ClientRequestGetUserCommitCommentsData request;
    /**
     * Creates a new instance of ClientRequestGetDirectory.
     *
     * @param data the request data.
     */
    public ClientRequestGetUserCommitComments(ClientRequestGetUserCommitCommentsData data) {
        this.databaseManager = DatabaseManager.getInstance();
        this.schemaName = databaseManager.getSchemaName();
        request = data;
    }

    @Override
    public ServerResponseInterface execute(String userName, ServerResponseFactoryInterface response) {
        SourceControlBehaviorManager sourceControlBehaviorManager = SourceControlBehaviorManager.getInstance();
        sourceControlBehaviorManager.setUserAndResponse(userName, response);
        ServerResponseInterface returnObject = null;

        FunctionalQueriesDAO functionalQueriesDAO = new FunctionalQueriesDAOImpl(schemaName);
        // <editor-fold>
        List<String> userComments = functionalQueriesDAO.getMostRecentUserCommits(userName, 100);
        // </editor-fold>

        ServerResponseGetUserCommitComments serverResponseGetUserCommitComments = new ServerResponseGetUserCommitComments();
        serverResponseGetUserCommitComments.setCommitComments(userComments);
        returnObject = serverResponseGetUserCommitComments;

        sourceControlBehaviorManager.clearThreadLocals();
        return returnObject;
    }

}
