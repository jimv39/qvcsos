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
package com.qumasoft.qvcslib.response;

import com.qumasoft.qvcslib.ArchiveDirManagerProxy;

/**
 * Success response. Used for operations that don't really return any data.
 * @author Jim Voris
 */
public class ServerResponseSuccess extends AbstractServerResponse {
    private static final long serialVersionUID = 7249010510499188466L;

    // This is serialized:
    private String message = null;

    /**
     * Creates a new instance of ServerResponseSuccess.
     * @param successMessage the success message.
     */
    public ServerResponseSuccess(String successMessage) {
        message = successMessage;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateDirManagerProxy(ArchiveDirManagerProxy directoryManagerProxy) {
        // Nothing to do here!!
    }

    /**
     * Get the message.
     * @return the message.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Set the message.
     * @param msg the message.
     */
    public void setMessage(String msg) {
        this.message = msg;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseOperationType getOperationType() {
        return ResponseOperationType.SR_RESPONSE_SUCCESS;
    }
}
