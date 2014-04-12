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
import com.qumasoft.qvcslib.response.ServerResponseInterface;

/**
 * Handlers of client requests must implement this interface. It should probably have a better name.
 * @author Jim Voris
 */
public interface ClientRequestInterface {

    /**
     * Execute the requested operation.
     * @param userName the user name making the request.
     * @param response link to the client.
     * @return the response that will be sent back to the client.
     */
    ServerResponseInterface execute(String userName, ServerResponseFactoryInterface response);
}
