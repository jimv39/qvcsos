/*   Copyright 2004-2021 Jim Voris
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
import com.qumasoft.qvcslib.NotificationManager;
import com.qumasoft.qvcslib.ServerResponseFactory;
import com.qumasoft.qvcslib.response.ServerResponseLogin;
import com.qumasoft.qvcslib.response.ServerResponseMessage;
import com.qumasoft.server.clientrequest.ClientRequestFactory;
import com.qumasoft.server.clientrequest.ClientRequestInterface;
import com.qumasoft.server.clientrequest.ClientRequestLogin;
import com.qvcsos.server.ServerTransactionManager;
import java.io.IOException;
import java.net.Socket;
import java.util.Iterator;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ServerWorker implements Runnable {
    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerWorker.class);
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
        LOGGER.info("Returned from handleClientRequests for thread: [{}]", Thread.currentThread().getName());
    }

    private void handleClientRequests() {
        String connectedTo = null;

        ServerResponseFactory responseFactory = null;
        ClientRequestFactory requestFactory;
        try {
            requestFactory = new ClientRequestFactory(workerSocket.getInputStream());
            responseFactory = new ServerResponseFactory(workerSocket.getOutputStream(), workerSocket.getPort(), workerSocket.getInetAddress().getHostAddress());
            connectedTo = workerSocket.getInetAddress().getHostAddress();
            LOGGER.info("Connected to: [{}]", connectedTo);

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

                        // Send any queued notifications.
                        NotificationManager.getNotificationManager().sendQueuedNotifications();

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
                        LOGGER.info("ClientRequest is null!! Breaking connection to: [{}]", connectedTo);
                        break;
                    }
                } catch (QVCSShutdownException e) {
                    // We are shutting down this server.
                    LOGGER.info("Shutting down server at request from: [{}]", connectedTo);
                    break;
                } catch (RuntimeException e) {
                    LOGGER.info("Runtime exception -- breaking connection to: [{}]", connectedTo);
                    LOGGER.warn(e.getLocalizedMessage(), e);
                    break;
                } catch (Exception e) {
                    LOGGER.info("Exception -- breaking connection to: [{}]", connectedTo);
                    LOGGER.warn(e.getLocalizedMessage(), e);
                    break;
                }
            }
        } catch (IOException e) {
            LOGGER.info("Breaking connection to: [{}]", connectedTo);
            LOGGER.warn(e.getLocalizedMessage(), e);
        } finally {
            try {
                LOGGER.info("Server closing socket for: [{}]", connectedTo);
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

                    // Disconnect any directory coordinate listeners.
                    NotificationManager.getNotificationManager().removeServerResponseFactory(responseFactory);

                    // Decrement the number of logged on users with the
                    // license manager.
                    if (responseFactory.getIsUserLoggedIn()) {
                        ServerTransactionManager.getInstance().flushClientTransaction(responseFactory);
                        LicenseManager.getInstance().logoutUser(responseFactory.getUserName(), responseFactory.getClientIPAddress());
                    }
                }
            } catch (IOException e) {
                LOGGER.warn(e.getLocalizedMessage(), e);
            }
        }
    }
}
