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

import com.qumasoft.qvcslib.commandargs.CreateArchiveCommandArgs;

/**
 * Client request create archive data.
 * @author Jim Voris
 */
public class ClientRequestCreateArchiveData extends ClientRequestClientData {
    private static final long serialVersionUID = 957322350619675567L;

    private static final ValidRequestElementType[] VALID_ELEMENTS = {
        ValidRequestElementType.PROJECT_NAME,
        ValidRequestElementType.VIEW_NAME,
        ValidRequestElementType.APPENDED_PATH
    };

    private int index;
    private CreateArchiveCommandArgs commandArgs;
    // This is the actual file revision as a byte array;
    private byte[] buffer = null;

    /**
     * Creates a new instance of ClientRequestCreateArchiveData.
     */
    public ClientRequestCreateArchiveData() {
    }

    /**
     * Get the command args.
     * @return the command args.
     */
    public CreateArchiveCommandArgs getCommandArgs() {
        return commandArgs;
    }

    /**
     * Set the command args.
     * @param args the command args.
     */
    public void setCommandArgs(CreateArchiveCommandArgs args) {
        commandArgs = args;
    }


    /**
     * Get the buffer. This contains the first revision of the file.
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
        return RequestOperationType.CREATE_ARCHIVE;
    }

    @Override
    public ValidRequestElementType[] getValidElements() {
        return VALID_ELEMENTS;
    }
}
