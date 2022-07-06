/*   Copyright 2004-2022 Jim Voris
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

import com.qumasoft.qvcslib.commandargs.CreateArchiveCommandArgs;

/**
 * AddFile archive action.
 *
 * @author Jim Voris
 */
public class AddFile extends ActionType {

    private final CreateArchiveCommandArgs commandArgs;

    /**
     * Creates a new instance of AddFile.
     *
     * @param args the create archive command args.
     */
    public AddFile(CreateArchiveCommandArgs args) {
        super("Create", ActionType.ADD_FILE);
        commandArgs = args;
    }

    /**
     * Creates a new instance of AddFile.
     */
    public AddFile() {
        super("Create", ActionType.ADD_FILE);
        commandArgs = null;
    }

    /**
     * Get the create archive command args.
     * @return the create archive command args.
     */
    CreateArchiveCommandArgs getCommandArgs() {
        return commandArgs;
    }
}
