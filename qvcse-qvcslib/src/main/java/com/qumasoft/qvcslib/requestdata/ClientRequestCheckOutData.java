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

import com.qumasoft.qvcslib.commandargs.CheckOutCommandArgs;

/**
 * Client request checkout data.
 * @author Jim Voris
 */
public class ClientRequestCheckOutData extends ClientRequestClientData {
    private static final long serialVersionUID = 5357836090853805929L;

    private static final ValidRequestElementType[] VALID_ELEMENTS = {
        ValidRequestElementType.PROJECT_NAME,
        ValidRequestElementType.BRANCH_NAME,
        ValidRequestElementType.APPENDED_PATH
    };
    private CheckOutCommandArgs commandArgs;

    /**
     * Creates new ClientRequestCheckOutRevisionData.
     */
    public ClientRequestCheckOutData() {
    }

    /**
     * Set the command arguments.
     * @param args the command arguments.
     */
    public void setCommandArgs(CheckOutCommandArgs args) {
        commandArgs = args;
    }

    /**
     * Get the command arguments.
     * @return the command arguments.
     */
    public CheckOutCommandArgs getCommandArgs() {
        return commandArgs;
    }

    @Override
    public RequestOperationType getOperationType() {
        return RequestOperationType.CHECK_OUT;
    }

    @Override
    public ValidRequestElementType[] getValidElements() {
        return VALID_ELEMENTS;
    }
}
