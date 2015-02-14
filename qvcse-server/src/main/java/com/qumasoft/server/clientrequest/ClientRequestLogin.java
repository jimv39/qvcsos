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

import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.ServerResponseFactoryInterface;
import com.qumasoft.qvcslib.requestdata.ClientRequestLoginData;
import com.qumasoft.qvcslib.response.ServerResponseInterface;
import com.qumasoft.qvcslib.response.ServerResponseLogin;
import com.qumasoft.server.AuthenticationManager;
import com.qumasoft.server.LicenseManager;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle client login requests.
 *
 * @author Jim Voris
 */
public class ClientRequestLogin implements ClientRequestInterface {
    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientRequestLogin.class);
    private final ClientRequestLoginData request;
    private boolean authenticationFailedFlag = false;
    private String message = null;

    /**
     * Creates a new instance of ClientLoginRequest.
     *
     * @param data an instance of the super class that contains command line arguments, etc.
     */
    public ClientRequestLogin(ClientRequestLoginData data) {
        request = data;
    }

    @Override
    public ServerResponseInterface execute(String userName, ServerResponseFactoryInterface response) {
        ServerResponseInterface returnObject;
        LOGGER.info("ClientRequestLogin.execute user name: [{}]", request.getUserName());
        ServerResponseLogin serverResponseLogin = new ServerResponseLogin();
        serverResponseLogin.setUserName(request.getUserName());
        serverResponseLogin.setServerName(request.getServerName());
        if (AuthenticationManager.getAuthenticationManager().authenticateUser(request.getUserName(), request.getPassword())) {
            AtomicReference<String> mutableMessage = new AtomicReference<>();
            if (LicenseManager.getInstance().loginUser(mutableMessage, request.getUserName(), response.getClientIPAddress())) {
                // Make sure the client version is one we support.
                // For now, we only support the same version as the server.
                if (request.getVersion().equals(QVCSConstants.QVCS_RELEASE_VERSION)) {
                    serverResponseLogin.setLoginResult(true);
                    serverResponseLogin.setVersionsMatchFlag(true);
                } else {
                    LOGGER.warn("Login for: " + request.getUserName() + ". Client version [" + request.getVersion() + "] not supported.");
                    serverResponseLogin.setLoginResult(true);
                    serverResponseLogin.setVersionsMatchFlag(false);
                    serverResponseLogin.setFailureReason("Server version: '" + QVCSConstants.QVCS_RELEASE_VERSION + "' does not support client version: '"
                            + request.getVersion() + "'.");
                }
            } else {
                serverResponseLogin.setLoginResult(false);
                serverResponseLogin.setFailureReason(mutableMessage.get());
            }
            message = mutableMessage.get();
        } else {
            LOGGER.info("Login failed for: [" + request.getUserName() + "]. Invalid username/password.");
            serverResponseLogin.setLoginResult(false);
            serverResponseLogin.setFailureReason("Invalid username/password.");
            authenticationFailedFlag = true;
        }
        returnObject = serverResponseLogin;
        return returnObject;
    }

    /**
     * Get the authentication failed flag.
     * @return the authentication failed flag.
     */
    public boolean getAuthenticationFailedFlag() {
        return authenticationFailedFlag;
    }

    /**
     * Get the message (for a login failure).
     * @return the message for a login failure.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Get the server name.
     * @return the server name.
     */
    public String getServerName() {
        return request.getServerName();
    }
}
