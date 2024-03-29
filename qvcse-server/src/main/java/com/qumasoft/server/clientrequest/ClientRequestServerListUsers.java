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
import com.qumasoft.qvcslib.requestdata.ClientRequestServerListUsersData;
import com.qumasoft.qvcslib.response.ServerResponseInterface;
import com.qumasoft.qvcslib.response.ServerResponseListUsers;
import com.qumasoft.server.AuthenticationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * List server users.
 * @author Jim Voris
 */
public class ClientRequestServerListUsers extends AbstractClientRequest {
    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientRequestServerListUsers.class);

    /**
     * Creates a new instance of ClientRequestServerListUsers.
     *
     * @param data command line arguments, etc.
     */
    public ClientRequestServerListUsers(ClientRequestServerListUsersData data) {
        setRequest(data);
    }

    @Override
    public ServerResponseInterface execute(String userName, ServerResponseFactoryInterface response) {
        ServerResponseInterface returnObject;

        LOGGER.info("ClientRequestServerListUsers.execute user: [" + userName + "] attempting to list users.");

        ServerResponseListUsers listUsersResponse = new ServerResponseListUsers();
        listUsersResponse.setServerName(getRequest().getServerName());
        listUsersResponse.setUserList(AuthenticationManager.getAuthenticationManager().listUsers());
        listUsersResponse.setSyncToken(getRequest().getSyncToken());
        returnObject = listUsersResponse;
        return returnObject;
    }
}
