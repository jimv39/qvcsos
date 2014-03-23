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
 * Change password response.
 * @author Jim Voris
 */
public class ServerResponseChangePassword implements ServerResponseInterface {
    private static final long serialVersionUID = 1236419043500031895L;

    // This is what gets serialized.
    private String serverName;
    private String userName;
    private String result;
    private boolean successFlag;

    /**
     * Creates a new instance of ServerResponseLogin.
     */
    public ServerResponseChangePassword() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateDirManagerProxy(ArchiveDirManagerProxy directoryManagerProxy) {
    }

    /**
     * Get the server name.
     * @return the server name.
     */
    public String getServerName() {
        return serverName;
    }

    /**
     * Set the server name.
     * @param server the server name.
     */
    public void setServerName(String server) {
        serverName = server;
    }

    /**
     * Get the user name.
     * @return the user name.
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Set the user name.
     * @param user the user name.
     */
    public void setUserName(String user) {
        userName = user;
    }

    /**
     * Get the result.
     * @return the result.
     */
    public String getResult() {
        return result;
    }

    /**
     * Set the result.
     * @param arg the result.
     */
    public void setResult(String arg) {
        result = arg;
    }

    /**
     * Get the success flag.
     * @return the success flag.
     */
    public boolean getSuccess() {
        return successFlag;
    }

    /**
     * Set the success flag.
     * @param flag the success flag.
     */
    public void setSuccess(boolean flag) {
        successFlag = flag;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseOperationType getOperationType() {
        return ResponseOperationType.SR_CHANGE_USER_PASSWORD;
    }
}
