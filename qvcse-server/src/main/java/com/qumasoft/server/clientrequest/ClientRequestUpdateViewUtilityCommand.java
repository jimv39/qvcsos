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

import com.qumasoft.qvcslib.QVCSRuntimeException;
import com.qumasoft.qvcslib.ServerResponseFactoryInterface;
import com.qumasoft.qvcslib.Utility;
import com.qumasoft.qvcslib.requestdata.ClientRequestUpdateViewUtilityCommandData;
import com.qumasoft.qvcslib.response.AbstractServerResponse;
import com.qumasoft.qvcslib.response.ServerResponseError;
import com.qumasoft.qvcslib.response.ServerResponseInterface;
import com.qumasoft.qvcslib.response.ServerResponseUpdateViewUtilityCommandLine;
import com.qvcsos.server.DatabaseManager;
import com.qvcsos.server.SourceControlBehaviorManager;
import com.qvcsos.server.dataaccess.ViewUtilityByExtensionDAO;
import com.qvcsos.server.dataaccess.ViewUtilityCommandLineDAO;
import com.qvcsos.server.dataaccess.impl.ViewUtilityByExtensionDAOImpl;
import com.qvcsos.server.dataaccess.impl.ViewUtilityCommandLineDAOImpl;
import com.qvcsos.server.datamodel.ViewUtilityByExtension;
import com.qvcsos.server.datamodel.ViewUtilityCommandLine;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Jim Voris.
 */
public class ClientRequestUpdateViewUtilityCommand extends AbstractClientRequest {

    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientRequestUpdateViewUtilityCommand.class);
    private final String schemaName;
    private final DatabaseManager databaseManager;
    private final SourceControlBehaviorManager sourceControlBehaviorManager;
    private final ClientRequestUpdateViewUtilityCommandData clientRequestUpdateViewUtilityCommandData;

    public ClientRequestUpdateViewUtilityCommand(ClientRequestUpdateViewUtilityCommandData data) {
        this.databaseManager = DatabaseManager.getInstance();
        this.schemaName = databaseManager.getSchemaName();
        this.sourceControlBehaviorManager = SourceControlBehaviorManager.getInstance();
        this.clientRequestUpdateViewUtilityCommandData = data;
        setRequest(data);
    }

    @Override
    public ServerResponseInterface execute(String userName, ServerResponseFactoryInterface response) {
        AbstractServerResponse returnObject;
        sourceControlBehaviorManager.setUserAndResponse(userName, response);
        String key = Utility.createUserAndComputerKey(clientRequestUpdateViewUtilityCommandData.getUserName(), clientRequestUpdateViewUtilityCommandData.getClientComputerName());
        try {
            ServerResponseUpdateViewUtilityCommandLine srResponse = new ServerResponseUpdateViewUtilityCommandLine();
            String computerAndUser = Utility.createUserAndComputerKey(clientRequestUpdateViewUtilityCommandData.getUserName(),
                    clientRequestUpdateViewUtilityCommandData.getClientComputerName());
            populateResponse(srResponse);
            switch (clientRequestUpdateViewUtilityCommandData.getRequestType()) {
                case ClientRequestUpdateViewUtilityCommandData.ADD_COMMAND_LINE_REQUEST:
                    returnObject = addCommandLine(computerAndUser, srResponse);
                    break;
                case ClientRequestUpdateViewUtilityCommandData.REMOVE_UTILITY_ASSOCIATION_REQUEST:
                    returnObject = removeUtilityAssociation(computerAndUser, srResponse);
                    break;
                case ClientRequestUpdateViewUtilityCommandData.ADD_UTILITY_ASSOCIATION_REQUEST:
                    returnObject = addUtilityAssociation(computerAndUser, srResponse);
                    break;
                default:
                    throw new QVCSRuntimeException("Invalid request type.");
            }
        } catch (SQLException e) {
            ServerResponseError sqlError = new ServerResponseError(e.getLocalizedMessage(), "", "", "");
            returnObject = sqlError;
        }
        returnObject.setSyncToken(getRequest().getSyncToken());
        sourceControlBehaviorManager.clearThreadLocals();
        return returnObject;
    }

    private AbstractServerResponse addCommandLine(String computerAndUser, ServerResponseUpdateViewUtilityCommandLine srResponse) throws SQLException {
        ViewUtilityCommandLineDAO viewUtilityCommandLineDAO = new ViewUtilityCommandLineDAOImpl(this.schemaName);
        ViewUtilityCommandLine viewUtilityCommandLine = viewUtilityCommandLineDAO.findByCommandLine(computerAndUser, clientRequestUpdateViewUtilityCommandData.getCommandLine());
        if (viewUtilityCommandLine == null) {
            // We need to add it to the database.
            viewUtilityCommandLine = new ViewUtilityCommandLine();
            viewUtilityCommandLine.setCommandLine(clientRequestUpdateViewUtilityCommandData.getCommandLine());
            viewUtilityCommandLine.setUserAndComputer(computerAndUser);
            Integer id = viewUtilityCommandLineDAO.insert(viewUtilityCommandLine);
            viewUtilityCommandLine.setId(id);
            srResponse.setCommandLineId(id);
        }
        if (clientRequestUpdateViewUtilityCommandData.getAssociateCommandWithExtension()) {
            ViewUtilityByExtensionDAO viewUtilityByExtensionDAO = new ViewUtilityByExtensionDAOImpl(this.schemaName);
            ViewUtilityByExtension viewUtilityByExtension = new ViewUtilityByExtension();
            viewUtilityByExtension.setFileExtension(clientRequestUpdateViewUtilityCommandData.getExtension());
            viewUtilityByExtension.setCommandLineId(viewUtilityCommandLine.getId());
            viewUtilityByExtension.setUserAndComputer(computerAndUser);
            Integer id = viewUtilityByExtensionDAO.insert(viewUtilityByExtension);
            viewUtilityByExtension.setId(id);
            srResponse.setExtensionId(id);
        }
        return srResponse;
    }

    private AbstractServerResponse removeUtilityAssociation(String computerAndUser, ServerResponseUpdateViewUtilityCommandLine srResponse) throws SQLException {
        ViewUtilityCommandLineDAO viewUtilityCommandLineDAO = new ViewUtilityCommandLineDAOImpl(this.schemaName);
        // First make sure the command line exists...
        ViewUtilityCommandLine viewUtilityCommandLine = viewUtilityCommandLineDAO.findByCommandLine(computerAndUser, clientRequestUpdateViewUtilityCommandData.getCommandLine());
        if (viewUtilityCommandLine != null) {
            String extension = clientRequestUpdateViewUtilityCommandData.getExtension();
            ViewUtilityByExtensionDAO viewUtilityByExtensionDAO = new ViewUtilityByExtensionDAOImpl(this.schemaName);
            ViewUtilityByExtension viewUtilityByExtension = viewUtilityByExtensionDAO.findByExtensionAndCommandLineId(computerAndUser, extension, viewUtilityCommandLine.getId());
            if (viewUtilityByExtension != null) {
                viewUtilityByExtensionDAO.delete(viewUtilityByExtension.getId());
            }
        }

        return srResponse;
    }

    private AbstractServerResponse addUtilityAssociation(String computerAndUser, ServerResponseUpdateViewUtilityCommandLine srResponse) throws SQLException {
        ViewUtilityCommandLineDAO viewUtilityCommandLineDAO = new ViewUtilityCommandLineDAOImpl(this.schemaName);
        ViewUtilityCommandLine viewUtilityCommandLine = viewUtilityCommandLineDAO.findByCommandLine(computerAndUser, clientRequestUpdateViewUtilityCommandData.getCommandLine());
        if (clientRequestUpdateViewUtilityCommandData.getAssociateCommandWithExtension()) {
            ViewUtilityByExtensionDAO viewUtilityByExtensionDAO = new ViewUtilityByExtensionDAOImpl(this.schemaName);
            ViewUtilityByExtension viewUtilityByExtension = new ViewUtilityByExtension();
            viewUtilityByExtension.setFileExtension(clientRequestUpdateViewUtilityCommandData.getExtension());
            viewUtilityByExtension.setCommandLineId(viewUtilityCommandLine.getId());
            viewUtilityByExtension.setUserAndComputer(computerAndUser);
            Integer id = viewUtilityByExtensionDAO.insert(viewUtilityByExtension);
            viewUtilityByExtension.setId(id);
            srResponse.setExtensionId(id);
            srResponse.setCommandLineId(viewUtilityCommandLine.getId());
        }
        return srResponse;
    }

    private void populateResponse(ServerResponseUpdateViewUtilityCommandLine srResponse) {
        srResponse.setServerName(clientRequestUpdateViewUtilityCommandData.getServerName());
        srResponse.setRequestType(clientRequestUpdateViewUtilityCommandData.getRequestType());
        srResponse.setAssociateCommandWithExtension(clientRequestUpdateViewUtilityCommandData.getAssociateCommandWithExtension());
        srResponse.setCommandLine(clientRequestUpdateViewUtilityCommandData.getCommandLine());
        srResponse.setExtension(clientRequestUpdateViewUtilityCommandData.getExtension());
    }

}
