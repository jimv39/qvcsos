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

import com.qumasoft.qvcslib.ClientRequestServerUpdatePrivilegesData;
import com.qumasoft.qvcslib.ServerResponseError;
import com.qumasoft.qvcslib.ServerResponseFactoryInterface;
import com.qumasoft.qvcslib.ServerResponseInterface;
import com.qumasoft.qvcslib.ServerResponseListRoleNames;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Update privileges.
 * @author Jim Voris
 */
public class ClientRequestServerUpdatePrivileges implements ClientRequestInterface {
    // Create our logger object
    private static final Logger LOGGER = Logger.getLogger("com.qumasoft.server");
    private final ClientRequestServerUpdatePrivilegesData request;

    /**
     * Creates a new instance of ClientRequestServerUpdatePrivileges.
     *
     * @param data command line arguments, etc.
     */
    public ClientRequestServerUpdatePrivileges(ClientRequestServerUpdatePrivilegesData data) {
        request = data;
    }

    @Override
    public ServerResponseInterface execute(String userName, ServerResponseFactoryInterface response) {
        ServerResponseInterface returnObject = null;

        try {
            LOGGER.log(Level.INFO, "ClientRequestServerUpdatePrivileges.execute user: " + userName + " attempting to update role privileges for role name "
                    + request.getRole().getRoleType() + " for server " + request.getServerName());

            // Make sure the caller (userName) is authorized to perform this kind of operation.
            if (0 == userName.compareTo(RoleManager.ADMIN)) {
                // Update the role...
                RolePrivilegesManager.getInstance().updatePrivileges(request.getRole().getRoleType(), request.getPrivileges(), request.getPrivilegesFlags());

                // And return a list of the current roles.
                ServerResponseListRoleNames listRoleNames = new ServerResponseListRoleNames();
                listRoleNames.setServerName(request.getServerName());
                listRoleNames.setRoleList(RolePrivilegesManager.getInstance().getAvailableRoles());
                returnObject = listRoleNames;
            } else {
                returnObject = new ServerResponseError("User '" + userName + "' is not authorized to update role privileges for this server.", null, null, null);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Caught exception: " + e.getClass().toString() + ": " + e.getLocalizedMessage());
        }
        return returnObject;
    }
}
