//   Copyright 2004-2014 Jim Voris
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.
//
package com.qumasoft.server;

import com.qumasoft.qvcslib.requestdata.ClientRequestServerListProjectUsersData;
import com.qumasoft.qvcslib.response.ServerResponseError;
import com.qumasoft.qvcslib.ServerResponseFactoryInterface;
import com.qumasoft.qvcslib.response.ServerResponseInterface;
import com.qumasoft.qvcslib.response.ServerResponseListProjectUsers;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * List project users.
 * @author Jim Voris
 */
public class ClientRequestServerListProjectUsers implements ClientRequestInterface {
    // Create our logger object
    private static final Logger LOGGER = Logger.getLogger("com.qumasoft.server");
    private final ClientRequestServerListProjectUsersData request;

    /**
     * Creates a new instance of ClientRequestServerListUsers.
     *
     * @param data command line arguments, etc.
     */
    public ClientRequestServerListProjectUsers(ClientRequestServerListProjectUsersData data) {
        request = data;
    }

    @Override
    public ServerResponseInterface execute(String userName, ServerResponseFactoryInterface response) {
        ServerResponseInterface returnObject;
        String projectName = request.getProjectName();
        LOGGER.log(Level.INFO, "ClientRequestServerListProjectUsers.execute user: " + userName + " attempting to list project users for project " + projectName);
        if (RolePrivilegesManager.getInstance().isUserPrivileged(projectName, userName, RolePrivilegesManager.LIST_PROJECT_USERS)) {
            ServerResponseListProjectUsers listProjectUsersResponse = new ServerResponseListProjectUsers();
            listProjectUsersResponse.setServerName(request.getServerName());
            listProjectUsersResponse.setProjectName(projectName);
            listProjectUsersResponse.setUserList(RoleManager.getRoleManager().listProjectUsers(projectName));
            returnObject = listProjectUsersResponse;
        } else {
            returnObject = new ServerResponseError("User '" + userName + "' is not authorized to list project users for this project.", null, null, null);
        }
        return returnObject;
    }
}
