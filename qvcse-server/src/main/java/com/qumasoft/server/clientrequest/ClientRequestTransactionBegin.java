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

import com.qumasoft.qvcslib.QVCSRuntimeException;
import com.qumasoft.qvcslib.ServerResponseFactoryInterface;
import com.qumasoft.qvcslib.requestdata.ClientRequestTransactionBeginData;
import com.qumasoft.qvcslib.response.AbstractServerResponse;
import com.qumasoft.qvcslib.response.ServerResponseTransactionBegin;
import com.qvcsos.server.DatabaseManager;
import com.qvcsos.server.ServerTransactionManager;
import java.sql.Connection;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Begin a transaction.
 * @author Jim Voris
 */
public class ClientRequestTransactionBegin extends AbstractClientRequest {
    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientRequestTransactionBegin.class);

    /**
     * Creates a new instance of ClientRequestTransactionBegin.
     *
     * @param data request data.
     */
    public ClientRequestTransactionBegin(ClientRequestTransactionBeginData data) {
        setRequest(data);
    }

    @Override
    public AbstractServerResponse execute(String userName, ServerResponseFactoryInterface response) {
        ServerResponseTransactionBegin returnObject = new ServerResponseTransactionBegin();
        returnObject.setTransactionID(getRequest().getTransactionID());
        returnObject.setServerName(getRequest().getServerName());

        // Keep track that we're in a transaction.
        ServerTransactionManager.getInstance().clientBeginTransaction(response);
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            if (connection.getAutoCommit()) {
                connection.setAutoCommit(false);
            }
        } catch (SQLException e) {
            LOGGER.warn("Failed to set auto commit to false", e);
            throw new QVCSRuntimeException("Failed to set auto commit to false");
        }

        returnObject.setSyncToken(getRequest().getSyncToken());
        return returnObject;
    }
}
