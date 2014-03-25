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

import com.qumasoft.qvcslib.commandargs.UnlockRevisionCommandArgs;

/**
 * Unlock request data.
 * @author Jim Voris
 */
public class ClientRequestUnlockData extends ClientRequestClientData {
    private static final long serialVersionUID = 4488018589385531185L;

    private static final ValidRequestElementType[] VALID_ELEMENTS = {
        ValidRequestElementType.PROJECT_NAME,
        ValidRequestElementType.VIEW_NAME,
        ValidRequestElementType.APPENDED_PATH
    };
    private UnlockRevisionCommandArgs commandArgs;

    /**
     * Creates a new instance of ClientRequestUnlockData.
     */
    public ClientRequestUnlockData() {
    }

    /**
     * A copy constructor (sort of) that can build an instance using the given break lock data.
     * @param breakLockData the break lock data.
     */
    public ClientRequestUnlockData(ClientRequestBreakLockData breakLockData) {
        commandArgs = breakLockData.getCommandArgs();
        setProjectName(breakLockData.getProjectName());
        setViewName(breakLockData.getViewName());
        setAppendedPath(breakLockData.getAppendedPath());
    }

    /**
     * Get the command args.
     * @return the command args.
     */
    public UnlockRevisionCommandArgs getCommandArgs() {
        return commandArgs;
    }

    /**
     * Set the command args.
     * @param args the command args.
     */
    public void setCommandArgs(UnlockRevisionCommandArgs args) {
        commandArgs = args;
    }

    @Override
    public RequestOperationType getOperationType() {
        return RequestOperationType.UNLOCK;
    }

    @Override
    public ValidRequestElementType[] getValidElements() {
        return VALID_ELEMENTS;
    }

}
