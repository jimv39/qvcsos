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
 * Transaction begin response.
 * @author Jim Voris
 */
public class ServerResponseTransactionBegin implements ServerResponseInterface {
    private static final long serialVersionUID = -2454397515004404070L;

    // This is what gets serialized.
    private int transactionID;
    private String serverName;

    /**
     * Creates a new instance of ServerResponseLogin.
     */
    public ServerResponseTransactionBegin() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateDirManagerProxy(ArchiveDirManagerProxy directoryManagerProxy) {
    }

    /**
     * Set the transaction ID.
     * @param transID the transaction ID.
     */
    public void setTransactionID(int transID) {
        transactionID = transID;
    }

    /**
     * Get the transaction ID.
     * @return the transaction ID.
     */
    public int getTransactionID() {
        return transactionID;
    }

    /**
     * Set the server name.
     * @param server the server name.
     */
    public void setServerName(String server) {
        serverName = server;
    }

    /**
     * Get the server name.
     * @return the server name.
     */
    public String getServerName() {
        return serverName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseOperationType getOperationType() {
        return ResponseOperationType.SR_BEGIN_TRANSACTION;
    }
}
