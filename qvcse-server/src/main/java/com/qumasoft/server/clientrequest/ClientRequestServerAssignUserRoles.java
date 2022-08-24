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
import com.qumasoft.qvcslib.requestdata.ClientRequestServerAssignUserRolesData;
import com.qumasoft.qvcslib.response.ServerResponseError;
import com.qumasoft.qvcslib.response.ServerResponseInterface;
import com.qumasoft.qvcslib.response.ServerResponseListProjectUsers;
import com.qumasoft.server.ActivityJournalManager;
import com.qumasoft.server.RoleManager;
import com.qumasoft.server.RolePrivilegesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Assign user roles.
 * @author Jim Voris
 */
public class ClientRequestServerAssignUserRoles extends AbstractClientRequest {
    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientRequestServerAssignUserRoles.class);

    /**
     * Creates a new instance of ClientRequestServerAssignUserRoles.
     *
     * @param data an instance of the super class that contains command line arguments, etc.
     */
    public ClientRequestServerAssignUserRoles(ClientRequestServerAssignUserRolesData data) {
        setRequest(data);
    }

    @Override
    public ServerResponseInterface execute(String userName, ServerResponseFactoryInterface response) {
        ServerResponseInterface returnObject;
        String projectName = getRequest().getProjectName();
        String requestUserName = getRequest().getUserName();

        LOGGER.info("ClientRequestServerAssignUserRoles.execute user: [" + userName + "] attempting to assign user roles for user: [" + requestUserName + "]");
        ClientRequestServerAssignUserRolesData clientRequestServerAssignUserRolesData = (ClientRequestServerAssignUserRolesData) getRequest();
        if (RolePrivilegesManager.getInstance().isUserPrivileged(projectName, userName, RolePrivilegesManager.ASSIGN_USER_ROLES)) {
            if (RoleManager.getRoleManager().assignUserRoles(userName, projectName, requestUserName, clientRequestServerAssignUserRolesData.getAssignedRoles())) {
                ServerResponseListProjectUsers listProjectUsersResponse = new ServerResponseListProjectUsers();
                listProjectUsersResponse.setServerName(getRequest().getServerName());
                listProjectUsersResponse.setProjectName(projectName);
                listProjectUsersResponse.setUserList(RoleManager.getRoleManager().listProjectUsers(projectName));
                listProjectUsersResponse.setSyncToken(getRequest().getSyncToken());
                returnObject = listProjectUsersResponse;

                // Add an entry to the server journal file.
                String[] assignedRoles = clientRequestServerAssignUserRolesData.getAssignedRoles();
                StringBuilder reportAssignedRoles = new StringBuilder();
                for (int i = 0; i < assignedRoles.length; i++) {
                    if (i > 0) {
                        reportAssignedRoles.append(",");
                    }
                    reportAssignedRoles.append(assignedRoles[i]);
                }
                ActivityJournalManager.getInstance().addJournalEntry("Assigned roles [" + reportAssignedRoles.toString() + "] for user [" + requestUserName
                        + "] for project [" + projectName + "].");
            } else {
                ServerResponseError error = new ServerResponseError("Failed to assign roles for user: [" + requestUserName + "] for project: [" + projectName + "]", null, null, null);
                error.setSyncToken(getRequest().getSyncToken());
                returnObject = error;
            }
        } else {
            ServerResponseError error = new ServerResponseError("User [" + userName + "] is not authorized to assign roles for project: [" + projectName + "]", null, null, null);
            error.setSyncToken(getRequest().getSyncToken());
            returnObject = error;
        }
        return returnObject;
    }
}
