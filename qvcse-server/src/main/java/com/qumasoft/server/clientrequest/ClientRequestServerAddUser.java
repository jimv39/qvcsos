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

import com.qumasoft.qvcslib.ServerResponseFactoryInterface;
import com.qumasoft.qvcslib.requestdata.ClientRequestServerAddUserData;
import com.qumasoft.qvcslib.response.ServerResponseError;
import com.qumasoft.qvcslib.response.ServerResponseInterface;
import com.qumasoft.qvcslib.response.ServerResponseListUsers;
import com.qumasoft.server.ActivityJournalManager;
import com.qumasoft.server.AuthenticationManager;
import com.qumasoft.server.RoleManager;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Add user to server.
 * @author Jim Voris
 */
public class ClientRequestServerAddUser extends AbstractClientRequest {
    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientRequestServerAddUser.class);

    /**
     * Creates a new instance of ClientRequestServerAddUser.
     *
     * @param data an instance of the super class that contains command line arguments, etc.
     */
    public ClientRequestServerAddUser(ClientRequestServerAddUserData data) {
        setRequest(data);
    }

    @Override
    public ServerResponseInterface execute(String userName, ServerResponseFactoryInterface response) {
        ServerResponseInterface returnObject;

        LOGGER.info("ClientRequestServerAddUser.execute user: [{}] attempting to add user: [{}]", userName, getRequest().getUserName());

        // Make sure the caller (userName) is authorized to perform this kind of operation.
        // They must have be the ADMIN user.
        if (0 == userName.compareTo(RoleManager.ADMIN)) {
            try {
                if (AuthenticationManager.getAuthenticationManager().addUser(userName, getRequest().getUserName(), getRequest().getPassword())) {
                    ServerResponseListUsers listUsersResponse = new ServerResponseListUsers();
                    listUsersResponse.setServerName(getRequest().getServerName());
                    listUsersResponse.setUserList(AuthenticationManager.getAuthenticationManager().listUsers());
                    listUsersResponse.setSyncToken(getRequest().getSyncToken());
                    returnObject = listUsersResponse;

                    // Add entry to journal file.
                    ActivityJournalManager.getInstance().addJournalEntry("User: [" + userName + "] added user [" + getRequest().getUserName() + "]");
                } else {
                    ServerResponseError error = new ServerResponseError("Failed to add [" + getRequest().getUserName() + "]. [" + userName + "] is not authorized to add a user!!", null, null, null);
                    error.setSyncToken(getRequest().getSyncToken());
                    returnObject = error;
                }
            } catch (SQLException e) {
                ServerResponseError error = new ServerResponseError("SQLException in ClientRequestServerAddUser", null, null, null);
                error.setSyncToken(getRequest().getSyncToken());
                returnObject = error;
            }
        } else {
            ServerResponseError error = new ServerResponseError("User [" + userName + "] is not authorized to add a user!!", null, null, null);
            error.setSyncToken(getRequest().getSyncToken());
            returnObject = error;
        }
        return returnObject;
    }
}
