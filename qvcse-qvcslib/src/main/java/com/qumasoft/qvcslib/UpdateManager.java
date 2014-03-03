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
package com.qumasoft.qvcslib;

/**
 * Update manager. Helper class for sending update requests to the server.
 * @author Jim Voris
 */
public final class UpdateManager {

    /**
     * Creates a new instance of UpdateManager. This class has only static methods, so we hide the default constructor.
     */
    private UpdateManager() {
    }

    /**
     * Set the update client request to the server.
     * @param currentVersionString the current version string.
     * @param fileName the file name that we're requesting from the server.
     * @param serverProperties the server properties.
     * @param restartFlag is a restart of the client required.
     */
    public static void updateClient(String currentVersionString, String fileName, ServerProperties serverProperties, boolean restartFlag) {
        TransportProxyInterface transportProxy = TransportProxyFactory.getInstance().getTransportProxy(serverProperties);
        if (transportProxy != null) {
            ClientRequestUpdateClientData request = new ClientRequestUpdateClientData();
            request.setClientVersionString(currentVersionString);
            request.setRequestedFileName(fileName);
            request.setRestartFlag(restartFlag);

            synchronized (transportProxy) {
                int transactionID = ClientTransactionManager.getInstance().sendBeginTransaction(transportProxy);
                transportProxy.write(request);
                ClientTransactionManager.getInstance().sendEndTransaction(transportProxy, transactionID);
            }
        }
    }

    /**
     * Set the update admin client request to the server.
     * @param currentVersionString the current version string.
     * @param fileName the file name that we're requesting from the server.
     * @param serverProperties the server properties.
     * @param restartFlag is a restart of the client required.
     */
    public static void updateAdminClient(String currentVersionString, String fileName, ServerProperties serverProperties, boolean restartFlag) {
        TransportProxyInterface transportProxy = TransportProxyFactory.getInstance().getAdminTransportProxy(serverProperties);
        if (transportProxy != null) {
            ClientRequestUpdateClientData request = new ClientRequestUpdateClientData();
            request.setClientVersionString(currentVersionString);
            request.setRequestedFileName(fileName);
            request.setRestartFlag(restartFlag);

            synchronized (transportProxy) {
                transportProxy.write(request);
            }
        }
    }
}
