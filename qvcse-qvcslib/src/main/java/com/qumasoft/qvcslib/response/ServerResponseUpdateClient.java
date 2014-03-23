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
 * Update client response object. This is the response from the server that supplies an updated jar file to the client.
 * @author Jim Voris
 */
public class ServerResponseUpdateClient implements ServerResponseInterface {
    private static final long serialVersionUID = -5383295311707810639L;

    // This is the name of the requested file.
    private String requestedFileName;

    // The restart flag.  If set, the client should restart after receiving this
    // response.
    private boolean restartFlag;

    // This is the file as a byte array;
    private byte[] buffer = null;

    /**
     * Creates a new instance of ServerResponseUpdateClient.
     */
    public ServerResponseUpdateClient() {
    }

    /**
     * Get the buffer containing the file sent from the server.
     * @return the buffer containing the file sent from the server.
     */
    public byte[] getBuffer() {
        return buffer;
    }

    /**
     * Set the buffer to be sent to the client.
     * @param buff the buffer to be sent to the client.
     */
    public void setBuffer(byte[] buff) {
        buffer = buff;
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
     * @param reqFileName the requested file name.
     */
    public void setRequestedFileName(String reqFileName) {
        requestedFileName = reqFileName;
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

    /**
     * This is a no-op for this message type.
     * @param directoryManagerProxy the directory manager.
     */
    @Override
    public void updateDirManagerProxy(ArchiveDirManagerProxy directoryManagerProxy) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseOperationType getOperationType() {
        return ResponseOperationType.SR_UPDATE_CLIENT_JAR;
    }
}
