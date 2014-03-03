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

import com.qumasoft.qvcslib.ServerResponseError;
import com.qumasoft.qvcslib.ServerResponseFactoryInterface;
import com.qumasoft.qvcslib.ServerResponseInterface;

/**
 * We use this class for a request that we can't process because of authorization problems. e.g. the user asks to check out a file,
 * but they only have a READER role defined.
 *
 * @author Jim Voris
 */
public class ClientRequestError implements ClientRequestInterface {

    private final String errorMessage;
    private final String operation;
    private ServerResponseInterface alternateResponseObject;

    /**
     * Creates a new instance of ClientRequestError.
     *
     * @param op the name of the operation with the error.
     * @param errMsg the error message.
     */
    public ClientRequestError(String op, String errMsg) {
        operation = op;
        errorMessage = errMsg;
    }

    @Override
    public ServerResponseInterface execute(String userName, ServerResponseFactoryInterface response) {
        ServerResponseInterface returnObject;
        if (getAlternateResponseObject() != null) {
            returnObject = getAlternateResponseObject();
        } else {
            // Return a command error.
            ServerResponseError error = new ServerResponseError("Authorization failed for operation: " + getOperation() + ": " + getErrorMessage(), null, null, null);
            returnObject = error;
        }
        return returnObject;
    }

    /**
     * Get the name of the operation that was the source of the error.
     * @return the name of the operation that was the source of the error.
     */
    String getOperation() {
        return operation;
    }

    /**
     * Get the error message.
     * @return the error message.
     */
    String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Get an alternate response object.
     * @return an alternate response object.
     */
    public ServerResponseInterface getAlternateResponseObject() {
        return alternateResponseObject;
    }

    /**
     * Set an alternate response object.
     * @param altResponseObject an alternate response object.
     */
    public void setAlternateResponseObject(ServerResponseInterface altResponseObject) {
        alternateResponseObject = altResponseObject;
    }
}
