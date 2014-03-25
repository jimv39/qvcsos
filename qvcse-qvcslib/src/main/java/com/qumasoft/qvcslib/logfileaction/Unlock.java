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

import com.qumasoft.qvcslib.commandargs.UnlockRevisionCommandArgs;

/**
 * An unlock action.
 * @author Jim Voris
 */
public class Unlock extends ActionType {

    private final UnlockRevisionCommandArgs commandArgs;

    /**
     * Creates a new instance of LogfileActionUnlock.
     * @param args the unlock revision command arguments.
     */
    public Unlock(UnlockRevisionCommandArgs args) {
        super("Unlock", ActionType.UNLOCK);
        commandArgs = args;
    }

    /**
     * Get the unlock command arguments.
     * @return the unlock command arguments.
     */
    public UnlockRevisionCommandArgs getCommandArgs() {
        return commandArgs;
    }
}
