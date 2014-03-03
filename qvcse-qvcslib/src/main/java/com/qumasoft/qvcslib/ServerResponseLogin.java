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
 * Login response.
 * @author Jim Voris
 */
public class ServerResponseLogin implements ServerResponseInterface {
    private static final long serialVersionUID = 5238846121851252581L;

    // This is what gets serialized.
    private String serverName;
    private String userName;
    private boolean loginResult;
    private boolean versionsMatchFlag;
    private String failureReason;

    /**
     * Creates a new instance of ServerResponseLogin.
     */
    public ServerResponseLogin() {
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
     * Get the login result.
     * @return the login result. true if the login succeeded; false otherwise.
     */
    public boolean getLoginResult() {
        return loginResult;
    }

    /**
     * Set the login result.
     * @param flag the login result.
     */
    public void setLoginResult(boolean flag) {
        loginResult = flag;
    }

    /**
     * Get the version match flag.
     * @return the version match flag. true if client and server versions match; false otherwise.
     */
    public boolean getVersionsMatchFlag() {
        return versionsMatchFlag;
    }

    /**
     * Set the version match flag.
     * @param flag the version match flag.
     */
    public void setVersionsMatchFlag(boolean flag) {
        versionsMatchFlag = flag;
    }

    /**
     * Get the reason for login failure.
     * @return the reason for login failure.
     */
    public String getFailureReason() {
        return failureReason;
    }

    /**
     * Set the reason for login failure.
     * @param reason the reason for login failure.
     */
    public void setFailureReason(String reason) {
        failureReason = reason;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseOperationType getOperationType() {
        return ResponseOperationType.SR_LOGIN;
    }
}
