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
package com.qumasoft.server.clientrequest;

import com.qumasoft.qvcslib.ServerResponseFactoryInterface;
import com.qumasoft.qvcslib.requestdata.ClientRequestTransactionEndData;
import com.qumasoft.qvcslib.response.ServerResponseInterface;
import com.qumasoft.qvcslib.response.ServerResponseTransactionEnd;
import com.qumasoft.server.ServerTransactionManager;

/**
 * Transaction end.
 * @author Jim Voris
 */
public class ClientRequestTransactionEnd implements ClientRequestInterface {
    private final ClientRequestTransactionEndData request;

    /**
     * Creates a new instance of ClientRequestTransactionEnd.
     *
     * @param data request data.
     */
    public ClientRequestTransactionEnd(ClientRequestTransactionEndData data) {
        request = data;
    }

    @Override
    public ServerResponseInterface execute(String userName, ServerResponseFactoryInterface response) {
        ServerResponseTransactionEnd returnObject = new ServerResponseTransactionEnd();
        returnObject.setTransactionID(request.getTransactionID().intValue());
        returnObject.setServerName(request.getServerName());

        // Keep track that we ended this transaction.
        ServerTransactionManager.getInstance().clientEndTransaction(response);

        return returnObject;
    }
}
