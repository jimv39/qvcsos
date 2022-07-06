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
package com.qumasoft.qvcslib.logfileaction;

import com.qumasoft.qvcslib.commandargs.CheckInCommandArgs;

/**
 * Check in revision action.
 * @author Jim Voris
 */
public class CheckIn extends ActionType {

    private final CheckInCommandArgs commandArgs;

    /**
     * Creates a new instance of LogfileActionCheckIn.
     * @param args the check in revision command args.
     */
    public CheckIn(CheckInCommandArgs args) {
        super("Check In", ActionType.CHECKIN_FILE);
        commandArgs = args;
    }

    /**
     * Get the check in revision command args.
     * @return the check in revision command args.
     */
    CheckInCommandArgs getCommandArgs() {
        return commandArgs;
    }
}
