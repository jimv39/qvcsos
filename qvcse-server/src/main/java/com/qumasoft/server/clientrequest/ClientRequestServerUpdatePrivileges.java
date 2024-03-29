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
import com.qumasoft.qvcslib.requestdata.ClientRequestServerUpdatePrivilegesData;
import com.qumasoft.qvcslib.response.ServerResponseError;
import com.qumasoft.qvcslib.response.ServerResponseInterface;
import com.qumasoft.qvcslib.response.ServerResponseListRoleNames;
import com.qumasoft.server.RoleManager;
import com.qumasoft.server.RolePrivilegesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Update privileges.
 * @author Jim Voris
 */
public class ClientRequestServerUpdatePrivileges extends AbstractClientRequest {
    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientRequestServerUpdatePrivileges.class);

    /**
     * Creates a new instance of ClientRequestServerUpdatePrivileges.
     *
     * @param data command line arguments, etc.
     */
    public ClientRequestServerUpdatePrivileges(ClientRequestServerUpdatePrivilegesData data) {
        setRequest(data);
    }

    @Override
    public ServerResponseInterface execute(String userName, ServerResponseFactoryInterface response) {
        ServerResponseInterface returnObject = null;

        try {
            LOGGER.info("ClientRequestServerUpdatePrivileges.execute user: [" + userName + "] attempting to update role privileges for role name ["
                    + getRequest().getRole() + "] for server [" + getRequest().getServerName() + "]");

            ClientRequestServerUpdatePrivilegesData clientRequestServerUpdatePrivilegesData = (ClientRequestServerUpdatePrivilegesData) getRequest();

            // Make sure the caller (userName) is authorized to perform this kind of operation.
            if (0 == userName.compareTo(RoleManager.ADMIN)) {
                // Update the role...
                RolePrivilegesManager.getInstance().updatePrivileges(getRequest().getRole(), clientRequestServerUpdatePrivilegesData.getPrivileges(),
                        clientRequestServerUpdatePrivilegesData.getPrivilegesFlags());

                // And return a list of the current roles.
                ServerResponseListRoleNames listRoleNames = new ServerResponseListRoleNames();
                listRoleNames.setServerName(getRequest().getServerName());
                listRoleNames.setRoleList(RoleManager.getRoleManager().getAvailableRoles());
                listRoleNames.setSyncToken(getRequest().getSyncToken());
                returnObject = listRoleNames;
            } else {
                ServerResponseError error = new ServerResponseError("User [" + userName + "] is not authorized to update role privileges for this server.", null, null, null);
                error.setSyncToken(getRequest().getSyncToken());
                returnObject = error;
            }
        } catch (Exception e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
        }
        return returnObject;
    }
}
