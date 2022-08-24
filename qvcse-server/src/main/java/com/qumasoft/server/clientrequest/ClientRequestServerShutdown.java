/*   Copyright 2004-2022 Jim Voris
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

import com.qumasoft.qvcslib.ServerResponseFactory;
import com.qumasoft.qvcslib.ServerResponseFactoryInterface;
import com.qumasoft.qvcslib.requestdata.ClientRequestServerShutdownData;
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
 * Shutdown server.
 * @author Jim Voris
 */
public class ClientRequestServerShutdown extends AbstractClientRequest {
    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientRequestServerShutdown.class);

    /**
     * Creates a new instance of ClientRequestServerShutdown.
     *
     * @param data command line arguments, etc.
     */
    public ClientRequestServerShutdown(ClientRequestServerShutdownData data) {
        setRequest(data);
    }

    @Override
    public ServerResponseInterface execute(String userName, ServerResponseFactoryInterface response) {
        ServerResponseInterface returnObject = null;
        String requestUserName = getRequest().getUserName();
        try {
            LOGGER.info("User name: [{}]", requestUserName);

            // Need to re-authenticate this guy.
            if (AuthenticationManager.getAuthenticationManager().authenticateUser(requestUserName, getRequest().getPassword())) {
                // The user is authenticated.  Make sure they are the ADMIN user -- that is the only
                // user allowed to shutdown a server.
                if (RoleManager.ADMIN.equals(requestUserName)) {
                    // We authenticated this guy, and he is the ADMIN user for this server.
                    // So it is okay to shutdown.
                    // We won't accept any more client requests, and when the current set
                    // of client requests are complete, we'll pull the plug on the server.
                    ServerResponseFactory.setShutdownInProgress(true);

                    // Add an entry to the server journal file.
                    ActivityJournalManager.getInstance().addJournalEntry("Shutting down server via request from ADMIN user.");

                    QVCSEnterpriseServer.setShutdownInProgress(true);

                    throw new QVCSShutdownException();
                } else {
                    // Return a command error.
                    ServerResponseError error = new ServerResponseError(requestUserName + " is not authorized to shutdown this server", null, null, null);
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
