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

import com.qumasoft.qvcslib.ClientRequestChangePasswordData;
import com.qumasoft.qvcslib.ServerResponseChangePassword;
import com.qumasoft.qvcslib.ServerResponseError;
import com.qumasoft.qvcslib.ServerResponseFactoryInterface;
import com.qumasoft.qvcslib.ServerResponseInterface;
import com.qumasoft.qvcslib.Utility;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Client request change password.
 * @author Jim Voris
 */
public class ClientRequestChangePassword implements ClientRequestInterface {

    // Create our logger object
    private static final Logger LOGGER = Logger.getLogger("com.qumasoft.server");
    private final ClientRequestChangePasswordData request;

    /**
     * Creates a new instance of ClientLoginRequest.
     * @param data the request data.
     */
    public ClientRequestChangePassword(ClientRequestChangePasswordData data) {
        request = data;
    }

    @Override
    public ServerResponseInterface execute(String userName, ServerResponseFactoryInterface response) {
        ServerResponseInterface returnObject;
        try {
            LOGGER.log(Level.FINE, "ClientRequestChangePassword.execute user name: " + request.getUserName());
            ServerResponseChangePassword serverResponseChangePassword = new ServerResponseChangePassword();
            serverResponseChangePassword.setUserName(request.getUserName());
            serverResponseChangePassword.setServerName(request.getServerName());

            // The user had the correct old password... change to the new one.
            if (AuthenticationManager.getAuthenticationManager().updateUser(userName, request.getUserName(), request.getOldPassword(), request.getNewPassword())) {
                serverResponseChangePassword.setResult("Password changed.");
                serverResponseChangePassword.setSuccess(true);
                ActivityJournalManager.getInstance().addJournalEntry("User: '" + request.getUserName() + "' changed user password.");
            } else {
                serverResponseChangePassword.setResult("Password NOT changed!!");
                serverResponseChangePassword.setSuccess(false);
            }
            returnObject = serverResponseChangePassword;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, Utility.expandStackTraceToString(e));

            // Return a command error.
            ServerResponseError error = new ServerResponseError("Caught exception trying to login user " + request.getUserName(), null, null, null);
            returnObject = error;
        }
        return returnObject;
    }
}
