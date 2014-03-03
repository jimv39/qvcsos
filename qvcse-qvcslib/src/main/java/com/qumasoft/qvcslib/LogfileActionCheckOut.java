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
 * Checkout revision action.
 * @author Jim Voris
 */
public class LogfileActionCheckOut extends LogfileActionType {

    private final LogFileOperationCheckOutCommandArgs commandArgs;

    /**
     * Creates a new instance of LogfileActionCheckOut.
     * @param args the checkout command args.
     */
    public LogfileActionCheckOut(LogFileOperationCheckOutCommandArgs args) {
        super("Check Out", LogfileActionType.CHECKOUT);
        commandArgs = args;
    }

    /**
     * Get the checkout command args.
     * @return the checkout command args.
     */
    public LogFileOperationCheckOutCommandArgs getCommandArgs() {
        return commandArgs;
    }
}
