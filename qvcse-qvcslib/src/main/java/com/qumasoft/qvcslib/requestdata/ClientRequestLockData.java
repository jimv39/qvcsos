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

import com.qumasoft.qvcslib.commandargs.LockRevisionCommandArgs;

/**
 * Lock request data.
 * @author Jim Voris
 */
public class ClientRequestLockData extends ClientRequestClientData {
    private static final long serialVersionUID = 921567236935816902L;

    private static final ValidRequestElementType[] VALID_ELEMENTS = {
        ValidRequestElementType.PROJECT_NAME,
        ValidRequestElementType.BRANCH_NAME,
        ValidRequestElementType.APPENDED_PATH
    };
    private LockRevisionCommandArgs commandArgs;

    /**
     * Creates a new instance of ClientRequestLockData.
     */
    public ClientRequestLockData() {
    }

    /**
     * Get the command arguments.
     * @return the command arguments
     */
    public LockRevisionCommandArgs getCommandArgs() {
        return commandArgs;
    }

    /**
     * Set the command arguments.
     * @param args the command arguments
     */
    public void setCommandArgs(LockRevisionCommandArgs args) {
        commandArgs = args;
    }

    @Override
    public RequestOperationType getOperationType() {
        return RequestOperationType.LOCK;
    }

    @Override
    public ValidRequestElementType[] getValidElements() {
        return VALID_ELEMENTS;
    }
}
