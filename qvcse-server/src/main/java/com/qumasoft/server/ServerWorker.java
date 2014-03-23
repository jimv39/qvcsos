/*   Copyright 2004-2014 Jim Voris
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
package com.qumasoft.server;

import com.qumasoft.qvcslib.ArchiveDirManagerInterface;
import com.qumasoft.qvcslib.ServerResponseFactory;
import com.qumasoft.qvcslib.response.ServerResponseLogin;
import com.qumasoft.qvcslib.response.ServerResponseMessage;
import com.qumasoft.qvcslib.Utility;
import java.io.IOException;
import java.net.Socket;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

class ServerWorker implements Runnable {
    // Create our logger object
    private static final Logger LOGGER = Logger.getLogger("com.qumasoft.server");
    /*
     * Socket to client we're handling
     */
    private final Socket workerSocket;

    ServerWorker(Socket socket) {
        this.workerSocket = socket;
    }

    @Override
    public void run() {
        handleClientRequests();
        LOGGER.log(Level.INFO, "Returned from handleClientRequests for thread: [" + Thread.currentThread().getName() + "]");
    }

    private void handleClientRequests() {
        String connectedTo = null;

        ServerResponseFactory responseFactory = null;
        ClientRequestFactory requestFactory;
        try {
            requestFactory = new ClientRequestFactory(workerSocket.getInputStream());
            responseFactory = new ServerResponseFactory(workerSocket.getOutputStream(), workerSocket.getPort(), workerSocket.getInetAddress().getHostAddress());
            connectedTo = workerSocket.getInetAddress().getHostAddress();
            LOGGER.log(Level.INFO, "Connected to: [" + connectedTo + "]");

            while (!ServerResponseFactory.getShutdownInProgress() && responseFactory.getConnectionAliveFlag()) {
                try {
                    ClientRequestInterface clientRequest = requestFactory.createClientRequest(responseFactory);
                    if (clientRequest != null) {
                        java.io.Serializable returnObject = clientRequest.execute(requestFactory.getUserName(), responseFactory);

                        if (clientRequest instanceof ClientRequestLogin) {
                            ServerResponseLogin serverResponseLogin = (ServerResponseLogin) returnObject;
                            if (serverResponseLogin.getLoginResult()) {
                                requestFactory.setIsUserLoggedIn(true);
                                requestFactory.setUserName(serverResponseLogin.getUserName());

                                responseFactory.setIsUserLoggedIn(true);
                                responseFactory.setUserName(serverResponseLogin.getUserName());
                                ClientRequestLogin loginRequest = (ClientRequestLogin) clientRequest;
                                responseFactory.setServerName(loginRequest.getServerName());
                                requestFactory.setClientVersionMatchesFlag(serverResponseLogin.getVersionsMatchFlag());

                                QVCSEnterpriseServer.getConnectedUsersCollection().add(responseFactory);
                            }
                        }

                        // Send the response back to the client.
                        responseFactory.createServerResponse(returnObject);

                        // If this was a login request that succeeded, we also
                        // need to send the list of projects for this user.
                        if (clientRequest instanceof ClientRequestLogin) {
                            ClientRequestLogin clientRequestLogin = (ClientRequestLogin) clientRequest;
                            ServerResponseMessage message;

                            if (!responseFactory.getIsUserLoggedIn()) {
                                // The user failed to login.  Report the problem to the user.
                                if (clientRequestLogin.getAuthenticationFailedFlag()) {
                                    message = new ServerResponseMessage("Invalid username/password", null, null, null, ServerResponseMessage.HIGH_PRIORITY);
                                    responseFactory.createServerResponse(message);
                                }
                            } else {
                                // Report any status information back to the user.
                                if (clientRequestLogin.getMessage() != null) {
                                    message = new ServerResponseMessage(clientRequestLogin.getMessage(), null, null, null, ServerResponseMessage.HIGH_PRIORITY);
                                    responseFactory.createServerResponse(message);
                                }
                            }
                        }
                    } else {
                        LOGGER.log(Level.INFO, "clientRequest is null!!");
                        LOGGER.log(Level.INFO, "Breaking connection to: [" + connectedTo + "]");
                        break;
                    }
                } catch (QVCSShutdownException e) {
                    // We are shutting down this server.
                    LOGGER.log(Level.INFO, "Shutting down server at request from: [" + connectedTo + "]");
                    break;
                } catch (RuntimeException e) {
                    LOGGER.log(Level.INFO, "Runtime exception -- breaking connection to: [" + connectedTo + "]");
                    LOGGER.log(Level.WARNING, Utility.expandStackTraceToString(e));
                    break;
                } catch (Exception e) {
                    LOGGER.log(Level.INFO, "Exception -- breaking connection to: [" + connectedTo + "]");
                    LOGGER.log(Level.WARNING, Utility.expandStackTraceToString(e));
                    break;
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.INFO, "Breaking connection to: [" + connectedTo + "]");
            LOGGER.log(Level.WARNING, Utility.expandStackTraceToString(e));
        } finally {
            try {
                LOGGER.log(Level.INFO, "Server closing socket for: [" + connectedTo + "]");
                workerSocket.close();

                // The connection to the client is gone.  Remove the response
                // factory as a listener for any archive directory managers
                // so we don't waste time trying to inform a client that we
                // can no longer talk to.
                if (responseFactory != null) {
                    Set<ArchiveDirManagerInterface> directoryManagers = responseFactory.getDirectoryManagers();
                    Iterator<ArchiveDirManagerInterface> it = directoryManagers.iterator();
                    while (it.hasNext()) {
                        ArchiveDirManagerInterface directoryManagerInterface = it.next();
                        directoryManagerInterface.removeLogFileListener(responseFactory);
                    }

                    QVCSEnterpriseServer.getConnectedUsersCollection().remove(responseFactory);

                    // Decrement the number of logged on users with the
                    // license manager.
                    if (responseFactory.getIsUserLoggedIn()) {
                        ServerTransactionManager.getInstance().flushClientTransaction(responseFactory);
                        LicenseManager.getInstance().logoutUser(responseFactory.getUserName(), responseFactory.getClientIPAddress());
                    }
                }
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, Utility.expandStackTraceToString(e));
            }
        }
    }
}
