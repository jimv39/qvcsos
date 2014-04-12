/*   Copyright 2004-2014 Jim Voris
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
import com.qumasoft.qvcslib.requestdata.ClientRequestServerRemoveUserData;
import com.qumasoft.qvcslib.response.ServerResponseError;
import com.qumasoft.qvcslib.response.ServerResponseInterface;
import com.qumasoft.qvcslib.response.ServerResponseListUsers;
import com.qumasoft.server.ActivityJournalManager;
import com.qumasoft.server.AuthenticationManager;
import com.qumasoft.server.RoleManager;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Remove a user.
 * @author Jim Voris
 */
public class ClientRequestServerRemoveUser implements ClientRequestInterface {
    // Create our logger object
    private static final Logger LOGGER = Logger.getLogger("com.qumasoft.server");
    private final ClientRequestServerRemoveUserData request;

    /**
     * Creates a new instance of ClientRequestServerRemoveUser.
     *
     * @param data command line arguments, etc.
     */
    public ClientRequestServerRemoveUser(ClientRequestServerRemoveUserData data) {
        request = data;
    }

    @Override
    public ServerResponseInterface execute(String userName, ServerResponseFactoryInterface response) {
        ServerResponseInterface returnObject;
        String requestUserName = request.getUserName();
        LOGGER.log(Level.INFO, "ClientRequestServerRemoveUser.execute user: " + userName + " attempting to remove user: " + requestUserName);
        if (0 == userName.compareTo(RoleManager.ADMIN)) {
            if (AuthenticationManager.getAuthenticationManager().removeUser(userName, requestUserName)) {
                // Remove any roles for this user.
                RoleManager.getRoleManager().removeAllUserRolesInAllProjects(userName, requestUserName);

                // And return success.
                ServerResponseListUsers listUsersResponse = new ServerResponseListUsers();
                listUsersResponse.setServerName(request.getServerName());
                listUsersResponse.setUserList(AuthenticationManager.getAuthenticationManager().listUsers());
                returnObject = listUsersResponse;

                // Add entry to journal file.
                ActivityJournalManager.getInstance().addJournalEntry("User: '" + userName + "' removed user: '" + requestUserName + "'");
            } else {
                returnObject = new ServerResponseError("Failed to remove " + requestUserName + ". " + userName + " is not authorized to remove a user!!", null, null, null);
            }
        } else {
            returnObject = new ServerResponseError("User '" + userName + "' is not authorized to remove a user.", null, null, null);
        }
        return returnObject;
    }
}
