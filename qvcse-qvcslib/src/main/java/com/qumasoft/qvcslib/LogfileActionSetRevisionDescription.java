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
 * Set the revision description action.
 * @author Jim Voris
 */
public class LogfileActionSetRevisionDescription extends LogfileActionType {

    private final LogFileOperationSetRevisionDescriptionCommandArgs commandArgs;

    /**
     * Creates a new instance of LogfileActionSetRevisionDescription.
     * @param args set revision command args.
     */
    public LogfileActionSetRevisionDescription(LogFileOperationSetRevisionDescriptionCommandArgs args) {
        super("Set Revision Description", LogfileActionType.SET_REVISION_DESCRIPTION);
        commandArgs = args;
    }

    /**
     * Get the set revision description command args.
     * @return the set revision description command args.
     */
    public LogFileOperationSetRevisionDescriptionCommandArgs getCommandArgs() {
        return commandArgs;
    }
}
