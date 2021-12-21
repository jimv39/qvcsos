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
package com.qumasoft.qvcslib;

/**
 * A bogus response object.
 * @author Jim Voris
 */
public class BogusResponseObject implements ServerResponseFactoryInterface {
    private static final int BOGUS_CLIENT_PORT = 1010;

    /**
     * Creates a new instance of BogusResponseObject.
     */
    public BogusResponseObject() {
    }

    @Override
    public void addArchiveDirManager(ArchiveDirManagerInterface archiveDirManager) {
    }

    @Override
    public void createServerResponse(java.io.Serializable responseObject) {
    }

    @Override
    public String getServerName() {
        return QVCSConstants.QVCS_SERVER_SERVER_NAME;
    }

    @Override
    public String getUserName() {
        return QVCSConstants.QVCS_SERVER_USER;
    }

    @Override
    public int getClientPort() {
        return BOGUS_CLIENT_PORT;
    }

    @Override
    public String getClientIPAddress() {
        return "localhost";
    }

    @Override
    public void clientIsAlive() {
        // Nothing to do here.
    }

    @Override
    public boolean getConnectionAliveFlag() {
        return false;
    }
}
