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
 * An unlock action.
 * @author Jim Voris
 */
public class LogfileActionUnlock extends LogfileActionType {

    private final LogFileOperationUnlockRevisionCommandArgs commandArgs;

    /**
     * Creates a new instance of LogfileActionUnlock.
     * @param args the unlock revision command arguments.
     */
    public LogfileActionUnlock(LogFileOperationUnlockRevisionCommandArgs args) {
        super("Unlock", LogfileActionType.UNLOCK);
        commandArgs = args;
    }

    /**
     * Get the unlock command arguments.
     * @return the unlock command arguments.
     */
    public LogFileOperationUnlockRevisionCommandArgs getCommandArgs() {
        return commandArgs;
    }
}
