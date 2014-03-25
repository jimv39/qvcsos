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

import com.qumasoft.qvcslib.commandargs.LockRevisionCommandArgs;

/**
 * Lock revision action.
 * @author Jim Voris
 */
public class Lock extends ActionType {

    private final LockRevisionCommandArgs commandArgs;

    /**
     * Creates a new instance of LogfileActionLock.
     * @param args the lock file command args.
     */
    public Lock(LockRevisionCommandArgs args) {
        super("Lock", ActionType.LOCK);
        commandArgs = args;
    }

    /**
     * Get the lock revision command args.
     * @return the lock revision command args.
     */
    public LockRevisionCommandArgs getCommandArgs() {
        return commandArgs;
    }
}
