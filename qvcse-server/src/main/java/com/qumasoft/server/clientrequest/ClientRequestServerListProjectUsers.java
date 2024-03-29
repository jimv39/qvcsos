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
import com.qumasoft.qvcslib.requestdata.ClientRequestServerListProjectUsersData;
import com.qumasoft.qvcslib.response.ServerResponseError;
import com.qumasoft.qvcslib.response.ServerResponseInterface;
import com.qumasoft.qvcslib.response.ServerResponseListProjectUsers;
import com.qumasoft.server.RoleManager;
import com.qumasoft.server.RolePrivilegesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * List project users.
 * @author Jim Voris
 */
public class ClientRequestServerListProjectUsers extends AbstractClientRequest {
    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientRequestServerListProjectUsers.class);

    /**
     * Creates a new instance of ClientRequestServerListUsers.
     *
     * @param data command line arguments, etc.
     */
    public ClientRequestServerListProjectUsers(ClientRequestServerListProjectUsersData data) {
        setRequest(data);
    }

    @Override
    public ServerResponseInterface execute(String userName, ServerResponseFactoryInterface response) {
        ServerResponseInterface returnObject;
        String projectName = getRequest().getProjectName();
        LOGGER.info("ClientRequestServerListProjectUsers.execute user: [" + userName + "] attempting to list project users for project [" + projectName + "]");
        if (RolePrivilegesManager.getInstance().isUserPrivileged(projectName, userName, RolePrivilegesManager.LIST_PROJECT_USERS)) {
            ServerResponseListProjectUsers listProjectUsersResponse = new ServerResponseListProjectUsers();
            listProjectUsersResponse.setServerName(getRequest().getServerName());
            listProjectUsersResponse.setProjectName(projectName);
            listProjectUsersResponse.setUserList(RoleManager.getRoleManager().listProjectUsers(projectName));
            listProjectUsersResponse.setSyncToken(getRequest().getSyncToken());
            returnObject = listProjectUsersResponse;
        } else {
            ServerResponseError error = new ServerResponseError("User [" + userName + "] is not authorized to list project users for this project.", null, null, null);
            error.setSyncToken(getRequest().getSyncToken());
            returnObject = error;
        }
        return returnObject;
    }
}
