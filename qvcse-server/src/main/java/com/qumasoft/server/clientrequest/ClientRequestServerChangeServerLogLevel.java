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
import com.qumasoft.qvcslib.requestdata.ClientRequestServerChangeServerLogLevelData;
import com.qumasoft.qvcslib.response.ServerResponseError;
import com.qumasoft.qvcslib.response.ServerResponseInterface;
import com.qumasoft.server.ActivityJournalManager;
import com.qumasoft.server.AuthenticationManager;
import com.qumasoft.server.QVCSEnterpriseServer;
import com.qumasoft.server.QVCSShutdownException;
import com.qumasoft.server.RoleManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Jim Voris
 */
public class ClientRequestServerChangeServerLogLevel extends AbstractClientRequest {
    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientRequestServerChangeServerLogLevel.class);

    /**
     * Creates a new instance of ClientRequestServerChangeServerLogLevel.
     *
     * @param data command line arguments, etc.
     */
    public ClientRequestServerChangeServerLogLevel(ClientRequestServerChangeServerLogLevelData data) {
        setRequest(data);
    }

    @Override
    public ServerResponseInterface execute(String userName, ServerResponseFactoryInterface response) {
        ServerResponseInterface returnObject = null;
        String requestUserName = getRequest().getUserName();
        ClientRequestServerChangeServerLogLevelData request = (ClientRequestServerChangeServerLogLevelData) getRequest();
        try {
            LOGGER.info("User name: [{}]", requestUserName);

            // Need to re-authenticate this guy.
            if (AuthenticationManager.getAuthenticationManager().authenticateUser(requestUserName, getRequest().getPassword())) {
                // The user is authenticated.  Make sure they are the ADMIN user -- that is the only
                // user allowed to change the log level.
                if (RoleManager.ADMIN.equals(requestUserName)) {
                    // We authenticated this guy, and he is the ADMIN user for this server.
                    // So it is okay to change the log level.
                    QVCSEnterpriseServer.setServerLogLevel(request.getLogLevel());

                    // Add an entry to the server journal file.
                    ActivityJournalManager.getInstance().addJournalEntry("Changed server log level via request from ADMIN user to: " + request.getLogLevel());

                } else {
                    // Return a command error.
                    ServerResponseError error = new ServerResponseError(requestUserName + " is not authorized to change the log level for this server", null, null, null);
                    error.setSyncToken(getRequest().getSyncToken());
                    returnObject = error;
                }
            } else {
                // Return a command error.
                ServerResponseError error = new ServerResponseError("Failed to authenticate: [" + requestUserName + "]", null, null, null);
                error.setSyncToken(getRequest().getSyncToken());
                returnObject = error;
            }
        } catch (QVCSShutdownException e) {
            // Re-throw this.
            throw e;
        }
        return returnObject;
    }

}
