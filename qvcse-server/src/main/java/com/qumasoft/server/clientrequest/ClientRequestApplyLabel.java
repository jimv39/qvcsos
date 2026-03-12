/*
 * Copyright 2026 Jim Voris.
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
import com.qumasoft.qvcslib.requestdata.ClientRequestApplyLabelData;
import com.qumasoft.qvcslib.response.AbstractServerResponse;
import com.qumasoft.qvcslib.response.ServerResponseError;
import com.qumasoft.qvcslib.response.ServerResponseInterface;
import com.qumasoft.qvcslib.response.ServerResponseMessage;
import com.qvcsos.server.DatabaseManager;
import com.qvcsos.server.SourceControlBehaviorManager;
import com.qvcsos.server.dataaccess.LabelFileJoinDAO;
import com.qvcsos.server.dataaccess.impl.LabelFileJoinDAOImpl;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Jim Voris
 */
public class ClientRequestApplyLabel extends AbstractClientRequest {
    /**
     * Create our logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientRequestApplyLabel.class);

    private final String schemaName;
    private final DatabaseManager databaseManager;
    private final SourceControlBehaviorManager sourceControlBehaviorManager;
    private final ClientRequestApplyLabelData requestData;

    public ClientRequestApplyLabel(ClientRequestApplyLabelData data) {
        this.requestData = data;
        this.databaseManager = DatabaseManager.getInstance();
        this.sourceControlBehaviorManager = SourceControlBehaviorManager.getInstance();
        this.schemaName = databaseManager.getSchemaName();

        setRequest(data);
    }

    @Override
    public ServerResponseInterface execute(String userName, ServerResponseFactoryInterface response) {
        Integer joinId;
        sourceControlBehaviorManager.setUserAndResponse(userName, response);
        AbstractServerResponse returnObject;
        LabelFileJoinDAO labelFileJoinDAO = new LabelFileJoinDAOImpl(schemaName);
        try {
            joinId = labelFileJoinDAO.insert(this.requestData.getLabelId(), this.requestData.getFileID());
            LOGGER.info("Inserted row in label file join table. Row id: [{}], Label id: [{}], file id: [{}]",
                    joinId, this.requestData.getLabelId(), this.requestData.getFileID());
            // Send back success message.
            ServerResponseMessage serverResponseMessage = new ServerResponseMessage("Added join row with id: " + joinId, null, null, null, ServerResponseMessage.LO_PRIORITY);
            returnObject = serverResponseMessage;
        } catch (SQLException e) {
            ServerResponseError serverResponseError = new ServerResponseError(e.getLocalizedMessage(), null, null, null);
            returnObject = serverResponseError;
        }

        sourceControlBehaviorManager.clearThreadLocals();
        returnObject.setSyncToken(getRequest().getSyncToken());
        return returnObject;
    }

}
