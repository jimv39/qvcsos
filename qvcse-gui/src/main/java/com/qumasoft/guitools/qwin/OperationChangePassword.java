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
package com.qumasoft.guitools.qwin;

import com.qumasoft.qvcslib.ClientRequestChangePasswordData;
import com.qumasoft.qvcslib.ClientTransactionManager;
import com.qumasoft.qvcslib.ServerProperties;
import com.qumasoft.qvcslib.ServerResponseChangePassword;
import com.qumasoft.qvcslib.TransportProxyFactory;
import com.qumasoft.qvcslib.TransportProxyInterface;
import com.qumasoft.qvcslib.TransportProxyType;
import com.qumasoft.qvcslib.Utility;

/**
 * Change password operation.
 * @author Jim Voris
 */
public final class OperationChangePassword extends OperationBaseClass {

    private final String userName;
    private final String oldPassword;
    private final String newPassword;

    /**
     * Operation change password.
     *
     * @param serverName the server name.
     * @param user the user name.
     * @param oldPwd the old password.
     * @param newPwd the new password.
     */
    public OperationChangePassword(String serverName, String user, String oldPwd, String newPwd) {
        super(null, serverName, null, null, null);
        userName = user;
        oldPassword = oldPwd;
        newPassword = newPwd;
    }

    @Override
    void executeOperation() {
        ServerProperties serverProperties = new ServerProperties(getServerName());

        TransportProxyType transportType = serverProperties.getClientTransport();
        int port = serverProperties.getClientPort();

        // Hash the password.
        byte[] hashedPassword = Utility.getInstance().hashPassword(oldPassword);

        // Look up the transport for this server.
        TransportProxyInterface transportProxy = TransportProxyFactory.getInstance().getTransportProxy(transportType, serverProperties, port, userName, hashedPassword,
                QWinFrame.getQWinFrame(), QWinFrame.getQWinFrame());

        if (transportProxy != null) {
            if (transportProxy.getIsLoggedInToServer()) {
                byte[] newHashedPassword = Utility.getInstance().hashPassword(newPassword);
                ClientRequestChangePasswordData changePasswordData = new ClientRequestChangePasswordData();
                changePasswordData.setNewPassword(newHashedPassword);
                changePasswordData.setOldPassword(hashedPassword);
                changePasswordData.setServerName(getServerName());
                changePasswordData.setUserName(userName);

                // Make sure this is synchronized
                synchronized (transportProxy) {
                    int transactionID = ClientTransactionManager.getInstance().sendBeginTransaction(transportProxy);
                    transportProxy.write(changePasswordData);
                    ClientTransactionManager.getInstance().sendEndTransaction(transportProxy, transactionID);
                }
            } else {
                // Let the user know that things did not work.
                ServerResponseChangePassword serverResponse = new ServerResponseChangePassword();
                serverResponse.setSuccess(false);
                serverResponse.setResult("You must first login to the server: '" + getServerName() + "' in order to change your password.");
                QWinFrame.getQWinFrame().notifyPasswordChange(serverResponse);
            }
        } else {
            // Let the user know that things did not work.
            ServerResponseChangePassword serverResponse = new ServerResponseChangePassword();
            serverResponse.setSuccess(false);
            serverResponse.setResult("Failed to connect to server: " + getServerName());
            QWinFrame.getQWinFrame().notifyPasswordChange(serverResponse);
        }
    }
}
