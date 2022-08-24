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
import com.qumasoft.qvcslib.requestdata.ClientRequestChangePasswordData;
import com.qumasoft.qvcslib.response.AbstractServerResponse;
import com.qumasoft.qvcslib.response.ServerResponseChangePassword;
import com.qumasoft.qvcslib.response.ServerResponseError;
import com.qumasoft.server.ActivityJournalManager;
import com.qumasoft.server.AuthenticationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client request change password.
 * @author Jim Voris
 */
public class ClientRequestChangePassword extends AbstractClientRequest {

    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientRequestChangePassword.class);

    /**
     * Creates a new instance of ClientLoginRequest.
     * @param data the request data.
     */
    public ClientRequestChangePassword(ClientRequestChangePasswordData data) {
        setRequest(data);
    }

    @Override
    public AbstractServerResponse execute(String userName, ServerResponseFactoryInterface response) {
        AbstractServerResponse returnObject;
        try {
            LOGGER.trace("ClientRequestChangePassword.execute user name: [{}]", getRequest().getUserName());
            ServerResponseChangePassword serverResponseChangePassword = new ServerResponseChangePassword();
            serverResponseChangePassword.setUserName(getRequest().getUserName());
            serverResponseChangePassword.setServerName(getRequest().getServerName());

            // The user had the correct old password... change to the new one.
            ClientRequestChangePasswordData clientRequestChangePasswordData = (ClientRequestChangePasswordData) getRequest();
            if (AuthenticationManager.getAuthenticationManager().updateUser(userName, clientRequestChangePasswordData.getUserName(), clientRequestChangePasswordData.getOldPassword(),
                    clientRequestChangePasswordData.getNewPassword())) {
                serverResponseChangePassword.setResult("Password changed.");
                serverResponseChangePassword.setSuccess(true);
                ActivityJournalManager.getInstance().addJournalEntry("User: [" + getRequest().getUserName() + "] changed user password.");
            } else {
                serverResponseChangePassword.setResult("Password NOT changed!!");
                serverResponseChangePassword.setSuccess(false);
            }
            returnObject = serverResponseChangePassword;
        } catch (Exception e) {
            LOGGER.warn(e.getLocalizedMessage(), e);

            // Return a command error.
            ServerResponseError error = new ServerResponseError("Caught exception trying to login user " + getRequest().getUserName(), null, null, null);
            returnObject = error;
        }
        returnObject.setSyncToken(getRequest().getSyncToken());
        return returnObject;
    }
}
