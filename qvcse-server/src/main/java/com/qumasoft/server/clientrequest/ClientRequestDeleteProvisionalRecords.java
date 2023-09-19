/*
 * Copyright 2023 Jim Voris.
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
import com.qumasoft.qvcslib.requestdata.ClientRequestDeleteProvisionalRecordsData;
import com.qumasoft.qvcslib.response.AbstractServerResponse;
import com.qumasoft.qvcslib.response.ServerResponseInterface;
import com.qumasoft.qvcslib.response.ServerResponseMessage;
import com.qvcsos.server.DatabaseManager;
import com.qvcsos.server.SourceControlBehaviorManager;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Jim Voris.
 */
public class ClientRequestDeleteProvisionalRecords extends AbstractClientRequest {
    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientRequestDeleteProvisionalRecords.class);
    private final DatabaseManager databaseManager;
    private final String schemaName;

    /**
     * Creates a new instance of ClientRequestDeleteProvisionalRecords.
     *
     * @param data command line data, etc.
     */
    public ClientRequestDeleteProvisionalRecords(ClientRequestDeleteProvisionalRecordsData data) {
        this.databaseManager = DatabaseManager.getInstance();
        this.schemaName = databaseManager.getSchemaName();
        setRequest(data);
    }

    @Override
    public ServerResponseInterface execute(String userName, ServerResponseFactoryInterface response) {
        SourceControlBehaviorManager sourceControlBehaviorManager = SourceControlBehaviorManager.getInstance();
        sourceControlBehaviorManager.setUserAndResponse(userName, response);
        AbstractServerResponse returnObject;
        String projectName = getRequest().getProjectName();
        try {
            sourceControlBehaviorManager.deleteProvisionalRecords(userName, projectName);

            // Send a response message so the client can treat this as a synchronous request.
            ServerResponseMessage message = new ServerResponseMessage("Deleted provisional records.", projectName, "", "", ServerResponseMessage.LO_PRIORITY);
            message.setShortWorkfileName("");
            returnObject = message;
        } catch (SQLException e) {
            ServerResponseMessage message = new ServerResponseMessage(e.getLocalizedMessage(), projectName, "", "", ServerResponseMessage.HIGH_PRIORITY);
            message.setShortWorkfileName("");
            returnObject = message;
        }
        sourceControlBehaviorManager.clearThreadLocals();
        returnObject.setSyncToken(getRequest().getSyncToken());
        return returnObject;
    }

}
