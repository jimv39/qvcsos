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
package com.qumasoft.qvcslib.requestdata;

/**
 * Update client request data.
 * @author Jim Voris
 */
public class ClientRequestUpdateClientData extends ClientRequestClientData {
    private static final long serialVersionUID = -5974989174277493448L;

    private String clientVersionString;
    private String requestedFileName;
    private boolean restartFlag;

    /**
     * Creates a new instance of ClientRequestUpdateClientData.
     */
    public ClientRequestUpdateClientData() {
    }

    /**
     * Get the client version string.
     * @return the client version string.
     */
    public String getClientVersionString() {
        return clientVersionString;
    }

    /**
     * Set the client version string.
     * @param cvs the client version string.
     */
    public void setClientVersionString(String cvs) {
        clientVersionString = cvs;
    }

    /**
     * Get the requested file name.
     * @return the requested file name.
     */
    public String getRequestedFileName() {
        return requestedFileName;
    }

    /**
     * Set the requested file name.
     * @param rfn the requested file name.
     */
    public void setRequestedFileName(String rfn) {
        requestedFileName = rfn;
    }

    /**
     * Get the restart flag.
     * @return the restart flag.
     */
    public boolean getRestartFlag() {
        return restartFlag;
    }

    /**
     * Set the restart flag.
     * @param flag the restart flag.
     */
    public void setRestartFlag(boolean flag) {
        restartFlag = flag;
    }

    @Override
    public RequestOperationType getOperationType() {
        return RequestOperationType.UPDATE_CLIENT_JAR;
    }

    @Override
    public ValidRequestElementType[] getValidElements() {
        return null;
    }
}
