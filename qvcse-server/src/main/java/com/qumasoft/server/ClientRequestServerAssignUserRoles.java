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

import com.qumasoft.qvcslib.ClientRequestServerAssignUserRolesData;
import com.qumasoft.qvcslib.ServerResponseError;
import com.qumasoft.qvcslib.ServerResponseFactoryInterface;
import com.qumasoft.qvcslib.ServerResponseInterface;
import com.qumasoft.qvcslib.ServerResponseListProjectUsers;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Assign user roles.
 * @author Jim Voris
 */
public class ClientRequestServerAssignUserRoles implements ClientRequestInterface {
    // Create our logger object
    private static final Logger LOGGER = Logger.getLogger("com.qumasoft.server");
    private final ClientRequestServerAssignUserRolesData request;

    /**
     * Creates a new instance of ClientRequestServerAssignUserRoles.
     *
     * @param data an instance of the super class that contains command line arguments, etc.
     */
    public ClientRequestServerAssignUserRoles(ClientRequestServerAssignUserRolesData data) {
        request = data;
    }

    @Override
    public ServerResponseInterface execute(String userName, ServerResponseFactoryInterface response) {
        ServerResponseInterface returnObject;
        String projectName = request.getProjectName();
        String requestUserName = request.getUserName();

        LOGGER.log(Level.INFO, "ClientRequestServerAssignUserRoles.execute user: " + userName + " attempting to assign user roles for user: " + requestUserName);
        if (RolePrivilegesManager.getInstance().isUserPrivileged(projectName, userName, RolePrivilegesManager.ASSIGN_USER_ROLES)) {
            if (RoleManager.getRoleManager().assignUserRoles(userName, projectName, requestUserName, request.getAssignedRoles())) {
                ServerResponseListProjectUsers listProjectUsersResponse = new ServerResponseListProjectUsers();
                listProjectUsersResponse.setServerName(request.getServerName());
                listProjectUsersResponse.setProjectName(projectName);
                listProjectUsersResponse.setUserList(RoleManager.getRoleManager().listProjectUsers(projectName));
                returnObject = listProjectUsersResponse;

                // Add an entry to the server journal file.
                String[] assignedRoles = request.getAssignedRoles();
                StringBuilder reportAssignedRoles = new StringBuilder();
                for (int i = 0; i < assignedRoles.length; i++) {
                    if (i > 0) {
                        reportAssignedRoles.append(",");
                    }
                    reportAssignedRoles.append(assignedRoles[i]);
                }
                ActivityJournalManager.getInstance().addJournalEntry("Assigned roles '" + reportAssignedRoles.toString() + "' for user '" + requestUserName
                        + "' for project '" + projectName + "'.");
            } else {
                returnObject = new ServerResponseError("Failed to assign roles for user: " + requestUserName + " for project: " + projectName, null, null, null);
            }
        } else {
            returnObject = new ServerResponseError("User '" + userName + "' is not authorized to assign roles for project: " + projectName, null, null, null);
        }
        return returnObject;
    }
}
