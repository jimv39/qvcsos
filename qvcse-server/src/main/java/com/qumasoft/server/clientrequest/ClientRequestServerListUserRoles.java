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
import com.qumasoft.qvcslib.requestdata.ClientRequestServerListUserRolesData;
import com.qumasoft.qvcslib.response.ServerResponseError;
import com.qumasoft.qvcslib.response.ServerResponseInterface;
import com.qumasoft.qvcslib.response.ServerResponseListUserRoles;
import com.qumasoft.server.RoleManager;
import com.qumasoft.server.RolePrivilegesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * List user roles.
 * @author Jim Voris
 */
public class ClientRequestServerListUserRoles extends AbstractClientRequest {
    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientRequestServerListUserRoles.class);

    /**
     * Creates a new instance of ClientRequestServerListUserRoles.
     *
     * @param data command line arguments, etc.
     */
    public ClientRequestServerListUserRoles(ClientRequestServerListUserRolesData data) {
        setRequest(data);
    }

    @Override
    public ServerResponseInterface execute(String userName, ServerResponseFactoryInterface response) {
        ServerResponseInterface returnObject = null;
        String projectName = getRequest().getProjectName();
        String requestUserName = getRequest().getUserName();

        try {
            LOGGER.info("ClientRequestServerListUserRoles.execute user: [" + userName + "] attempting to list user roles.");

            // Make sure the caller (userName) is authorized to perform this kind of operation.
            if (RolePrivilegesManager.getInstance().isUserPrivileged(projectName, userName, RolePrivilegesManager.LIST_USER_ROLES)) {
                ServerResponseListUserRoles listUserRolesResponse = new ServerResponseListUserRoles();
                listUserRolesResponse.setUserName(requestUserName);
                listUserRolesResponse.setProjectName(projectName);
                listUserRolesResponse.setUserRolesList(RoleManager.getRoleManager().listUserRoles(projectName, requestUserName));
                listUserRolesResponse.setAvailableRoles(RoleManager.getRoleManager().getAvailableRoles());
                listUserRolesResponse.setSyncToken(getRequest().getSyncToken());
                returnObject = listUserRolesResponse;
            } else {
                ServerResponseError error = new ServerResponseError("User [" + userName + "] is not authorized to list user roles for project [" + projectName + "].", null, null, null);
                error.setSyncToken(getRequest().getSyncToken());
                returnObject = error;
            }
        } catch (Exception e) {
            LOGGER.warn("Caught exception: " + e.getClass().getName() + ": " + e.getLocalizedMessage());
        }
        return returnObject;
    }
}
