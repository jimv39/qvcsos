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

import com.qumasoft.qvcslib.commandargs.UnLabelDirectoryCommandArgs;

/**
 * Unlabel directory request data.
 * @author Jim Voris
 */
public class ClientRequestUnLabelDirectoryData extends ClientRequestClientData {
    private static final long serialVersionUID = -8092273001812038048L;

    private static final ValidRequestElementType[] VALID_ELEMENTS = {
        ValidRequestElementType.PROJECT_NAME,
        ValidRequestElementType.BRANCH_NAME,
        ValidRequestElementType.APPENDED_PATH
    };
    private UnLabelDirectoryCommandArgs commandArgs;

    /**
     * Creates a new instance of ClientRequestUnLabelDirectoryData.
     */
    public ClientRequestUnLabelDirectoryData() {
    }

    /**
     * Get the command arguments.
     * @return the command arguments.
     */
    public UnLabelDirectoryCommandArgs getCommandArgs() {
        return commandArgs;
    }

    /**
     * Set the command arguments.
     * @param args the command arguments.
     */
    public void setCommandArgs(UnLabelDirectoryCommandArgs args) {
        commandArgs = args;
    }

    @Override
    public RequestOperationType getOperationType() {
        return RequestOperationType.REMOVE_LABEL_DIRECTORY;
    }

    @Override
    public ValidRequestElementType[] getValidElements() {
        return VALID_ELEMENTS;
    }
}
