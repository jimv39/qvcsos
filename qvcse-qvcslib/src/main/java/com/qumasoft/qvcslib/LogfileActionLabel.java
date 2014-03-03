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
 * Label action.
 * @author Jim Voris
 */
public class LogfileActionLabel extends LogfileActionType {

    private final LogFileOperationLabelRevisionCommandArgs commandArgs;

    /**
     * Creates a new instance of LogfileActionLabel.
     * @param args the label revision command args.
     */
    public LogfileActionLabel(LogFileOperationLabelRevisionCommandArgs args) {
        super("Label", LogfileActionType.LABEL);
        commandArgs = args;
    }

    /**
     * Get the label revision command args.
     * @return the label revision command args.
     */
    LogFileOperationLabelRevisionCommandArgs getCommandArgs() {
        return commandArgs;
    }
}
