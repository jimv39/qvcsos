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
import com.qumasoft.qvcslib.requestdata.ClientRequestServerGetRoleNamesData;
import com.qumasoft.qvcslib.response.ServerResponseError;
import com.qumasoft.qvcslib.response.ServerResponseInterface;
import com.qumasoft.qvcslib.response.ServerResponseListRoleNames;
import com.qumasoft.server.RoleManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Get role names.
 * @author Jim Voris
 */
public class ClientRequestServerGetRoleNames extends AbstractClientRequest {
    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientRequestServerGetRoleNames.class);

    /**
     * Creates a new instance of ClientRequestServerGetRoleNames.
     *
     * @param data command line arguments, etc.
     */
    public ClientRequestServerGetRoleNames(ClientRequestServerGetRoleNamesData data) {
        setRequest(data);
    }

    @Override
    public ServerResponseInterface execute(String userName, ServerResponseFactoryInterface response) {
        ServerResponseInterface returnObject;
        LOGGER.info("ClientRequestServerGetRoleNames.execute user: [" + userName + "] attempting to list role names for server [" + getRequest().getServerName() + "]");
        if (0 == userName.compareTo(RoleManager.ADMIN)) {
            ServerResponseListRoleNames listRoleNames = new ServerResponseListRoleNames();
            listRoleNames.setServerName(getRequest().getServerName());
            listRoleNames.setRoleList(RoleManager.getRoleManager().getAvailableRoles());
            listRoleNames.setSyncToken(getRequest().getSyncToken());
            returnObject = listRoleNames;
        } else {
            ServerResponseError error = new ServerResponseError("User [" + userName + "] is not authorized to list role names for this server.", null, null, null);
            error.setSyncToken(getRequest().getSyncToken());
            returnObject = error;
        }
        return returnObject;
    }
}
