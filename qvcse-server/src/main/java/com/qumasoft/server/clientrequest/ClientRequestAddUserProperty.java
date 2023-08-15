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
import com.qumasoft.qvcslib.UserPropertyData;
import com.qumasoft.qvcslib.requestdata.ClientRequestAddUserPropertyData;
import com.qumasoft.qvcslib.response.AbstractServerResponse;
import com.qumasoft.qvcslib.response.ServerResponseAddUserProperty;
import com.qumasoft.qvcslib.response.ServerResponseError;
import com.qumasoft.qvcslib.response.ServerResponseInterface;
import com.qvcsos.server.DatabaseManager;
import com.qvcsos.server.SourceControlBehaviorManager;
import com.qvcsos.server.dataaccess.UserPropertyDAO;
import com.qvcsos.server.dataaccess.impl.UserPropertyDAOImpl;
import com.qvcsos.server.datamodel.UserProperty;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Jim Voris.
 */
public class ClientRequestAddUserProperty extends AbstractClientRequest {

    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientRequestAddUserProperty.class);
    private final String schemaName;
    private final DatabaseManager databaseManager;
    private final SourceControlBehaviorManager sourceControlBehaviorManager;
    private final ClientRequestAddUserPropertyData clientRequestAddUserPropertyData;

    public ClientRequestAddUserProperty(ClientRequestAddUserPropertyData data) {
        this.databaseManager = DatabaseManager.getInstance();
        this.schemaName = databaseManager.getSchemaName();
        this.sourceControlBehaviorManager = SourceControlBehaviorManager.getInstance();
        this.clientRequestAddUserPropertyData = data;
        setRequest(data);
    }

    @Override
    public ServerResponseInterface execute(String userName, ServerResponseFactoryInterface response) {
        AbstractServerResponse returnObject;
        sourceControlBehaviorManager.setUserAndResponse(userName, response);
        try {
            UserPropertyDAO userPropertyDAO = new UserPropertyDAOImpl(schemaName);
            UserPropertyData upData = clientRequestAddUserPropertyData.getUserPropertyData();
            // See if this property already exists...
            UserProperty existingUserProperty = userPropertyDAO.findByUserAndComputerAndPropertyName(upData.getUserAndComputer(), upData.getPropertyName());
            Integer rowId;
            if (existingUserProperty != null) {
                // Update existing row.
                existingUserProperty.setPropertyValue(upData.getPropertyValue());
                rowId = userPropertyDAO.updateUserProperty(existingUserProperty);
            } else {
                // Insert a new row.
                UserProperty userProperty = new UserProperty();
                userProperty.setUserAndComputer(upData.getUserAndComputer());
                userProperty.setPropertyName(upData.getPropertyName());
                userProperty.setPropertyValue(upData.getPropertyValue());
                rowId = userPropertyDAO.insert(userProperty);
            }
            databaseManager.getConnection().commit();
            upData.setId(rowId);
            ServerResponseAddUserProperty aupResponse = new ServerResponseAddUserProperty();
            aupResponse.setUserPropertyData(upData);
            aupResponse.setPropertiesKey(clientRequestAddUserPropertyData.getPropertiesKey());
            returnObject = aupResponse;
        } catch (SQLException e) {
            ServerResponseError sqlError = new ServerResponseError(e.getLocalizedMessage(), "", "", "");
            returnObject = sqlError;
        }
        returnObject.setSyncToken(getRequest().getSyncToken());
        sourceControlBehaviorManager.clearThreadLocals();
        return returnObject;
    }
}
