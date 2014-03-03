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
 * Create archive action.
 * @author Jim Voris
 */
public class LogfileActionCreate extends LogfileActionType {

    private final LogFileOperationCreateArchiveCommandArgs commandArgs;

    /**
     * Creates a new instance of LogfileActionCreate.
     * @param args the create archive command args.
     */
    public LogfileActionCreate(LogFileOperationCreateArchiveCommandArgs args) {
        super("Create", LogfileActionType.CREATE);
        commandArgs = args;
    }

    /**
     * Creates a new instance of LogfileActionCreate.
     */
    public LogfileActionCreate() {
        super("Create", LogfileActionType.CREATE);
        commandArgs = null;
    }

    /**
     * Get the create archive command args.
     * @return the create archive command args.
     */
    LogFileOperationCreateArchiveCommandArgs getCommandArgs() {
        return commandArgs;
    }
}
