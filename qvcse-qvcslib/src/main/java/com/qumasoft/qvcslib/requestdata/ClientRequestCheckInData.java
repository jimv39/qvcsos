/*   Copyright 2004-2019 Jim Voris
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

import com.qumasoft.qvcslib.SynchronizationManager;
import com.qumasoft.qvcslib.commandargs.CheckInCommandArgs;

/**
 * Client request checkin data.
 * @author Jim Voris
 */
public class ClientRequestCheckInData extends ClientRequestClientData {
    private static final long serialVersionUID = 360117150297146530L;

    private static final ValidRequestElementType[] VALID_ELEMENTS = {
        ValidRequestElementType.PROJECT_NAME,
        ValidRequestElementType.BRANCH_NAME,
        ValidRequestElementType.APPENDED_PATH,
        ValidRequestElementType.FILE_ID,
        ValidRequestElementType.SYNC_TOKEN
    };
    private int index;
    private CheckInCommandArgs commandArgs;
    // This is the actual file revision as a byte array;
    private byte[] buffer = null;

    /**
     * Creates new ClientRequestCheckInData.
     */
    public ClientRequestCheckInData() {
        setSyncToken(SynchronizationManager.getSynchronizationManager().getSynchronizationToken());
    }

    /**
     * Get the buffer.
     * @return the buffer.
     */
    public byte[] getBuffer() {
        return buffer;
    }

    /**
     * Set the buffer.
     * @param buff the buffer.
     */
    public void setBuffer(byte[] buff) {
        buffer = buff;
    }

    /**
     * Get the command arguments.
     * @return the command arguments.
     */
    public CheckInCommandArgs getCommandArgs() {
        return commandArgs;
    }

    /**
     * Set the command arguments.
     * @param args the command arguments.
     */
    public void setCommandArgs(CheckInCommandArgs args) {
        commandArgs = args;
    }

    /**
     * Get the index.
     * @return the index.
     */
    public int getIndex() {
        return index;
    }

    /**
     * Set the index.
     * @param idx the index.
     */
    public void setIndex(int idx) {
        index = idx;
    }

    @Override
    public RequestOperationType getOperationType() {
        return RequestOperationType.CHECK_IN;
    }

    @Override
    public ValidRequestElementType[] getValidElements() {
        return VALID_ELEMENTS;
    }
}
