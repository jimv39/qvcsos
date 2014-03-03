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
 * Set revision description request data.
 * @author Jim Voris
 */
public class ClientRequestSetRevisionDescriptionData extends ClientRequestClientData {
    private static final long serialVersionUID = 1966240986582563257L;

    private static final ValidRequestElementType[] VALID_ELEMENTS = {
        ValidRequestElementType.PROJECT_NAME,
        ValidRequestElementType.VIEW_NAME,
        ValidRequestElementType.APPENDED_PATH
    };
    private LogFileOperationSetRevisionDescriptionCommandArgs commandArgs;

    /**
     * Creates a new instance of ClientRequestSetRevisionDescriptionData.
     */
    public ClientRequestSetRevisionDescriptionData() {
    }

    /**
     * Get the command args.
     * @return the command args.
     */
    public LogFileOperationSetRevisionDescriptionCommandArgs getCommandArgs() {
        return commandArgs;
    }

    /**
     * Set the command args.
     * @param args the command args.
     */
    public void setCommandArgs(LogFileOperationSetRevisionDescriptionCommandArgs args) {
        commandArgs = args;
    }

    @Override
    public RequestOperationType getOperationType() {
        return RequestOperationType.SET_REVISION_DESCRIPTION;
    }

    @Override
    public ValidRequestElementType[] getValidElements() {
        return VALID_ELEMENTS;
    }
}
