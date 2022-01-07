/*   Copyright 2004-2015 Jim Voris
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
import com.qumasoft.qvcslib.requestdata.ClientRequestServerGetRolePrivilegesData;
import com.qumasoft.qvcslib.response.ServerResponseError;
import com.qumasoft.qvcslib.response.ServerResponseInterface;
import com.qumasoft.qvcslib.response.ServerResponseListRolePrivileges;
import com.qumasoft.server.RoleManager;
import com.qumasoft.server.RolePrivilegesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Get role privileges for a given role.
 * @author Jim Voris
 */
public class ClientRequestServerGetRolePrivileges implements ClientRequestInterface {
    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientRequestServerGetRolePrivileges.class);
    private final ClientRequestServerGetRolePrivilegesData request;

    /**
     * Creates a new instance of ClientRequestServerGetRolePrivileges.
     *
     * @param data command line arguments, etc.
     */
    public ClientRequestServerGetRolePrivileges(ClientRequestServerGetRolePrivilegesData data) {
        request = data;
    }

    @Override
    public ServerResponseInterface execute(String userName, ServerResponseFactoryInterface response) {
        ServerResponseInterface returnObject;
        LOGGER.info("ClientRequestServerGetRolePrivileges.execute user: [" + userName + "] attempting to get role privileges for role name ["
                + request.getRole() + "] for server [" + request.getServerName() + "]");
        if (0 == userName.compareTo(RoleManager.ADMIN)) {
            ServerResponseListRolePrivileges listRolePrivileges = new ServerResponseListRolePrivileges();
            listRolePrivileges.setRolePrivilegesList(RolePrivilegesManager.getInstance().getRolePrivilegesList());
            listRolePrivileges.setRoleFlagsList(RolePrivilegesManager.getInstance().getRolePrivilegesFlags(request.getRole()));
            returnObject = listRolePrivileges;
        } else {
            returnObject = new ServerResponseError("User [" + userName + "] is not authorized to list role privileges for this server.", null, null, null);
        }
        return returnObject;
    }
}
