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

/**
 * Rename action.
 * @author Jim Voris
 */
public class Rename extends ActionType {

    private final String oldShortWorkfileName;

    /**
     * Creates a new instance of LogfileActionRename.
     * @param oldShortName the old short workfile name.
     */
    public Rename(String oldShortName) {
        super("Rename", ActionType.RENAME);
        oldShortWorkfileName = oldShortName;
    }

    /**
     * Get the old short workfile name.
     * @return the old short workfile name.
     */
    public String getOldShortWorkfileName() {
        return oldShortWorkfileName;
    }
}
