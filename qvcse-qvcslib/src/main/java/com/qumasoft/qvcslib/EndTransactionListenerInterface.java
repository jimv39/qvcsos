/*   Copyright 2022 Jim Voris
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
package com.qumasoft.qvcslib;

import com.qumasoft.qvcslib.response.ServerResponseTransactionEnd;

/**
 * End transaction listener interface. Implemented by those clients that need
 * notification on receipt of a end transaction message.
 *
 * @author Jim Voris
 */
public interface EndTransactionListenerInterface {

    /**
     * Notify receipt of a ServerResponseTransactionEnd message.
     *
     * @param response the ServerResponseTransactionEnd from the server.
     */
    void notifyEndTransaction(ServerResponseTransactionEnd response);

}
