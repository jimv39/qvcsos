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

/**
 * Identify the different types of actions that can be performed.
 * @author Jim Voris
 */
public class ActionType {

    /** Add file action. */
    public static final int ADD_FILE = 5;
    /** Check in a revision action. */
    public static final int CHECKIN_FILE = 20;
    /** Remove action. */
    public static final int REMOVE_FILE = 140;
    /** Rename action. */
    public static final int RENAME_FILE = 150;
    /** Move action. */
    public static final int MOVE_FILE = 160;
    /** Something changed on a branch action. */
    public static final int CHANGE_ON_BRANCH = 170;

    private final String actionType;
    private final int action;

    /**
     * Creates a new instance of ActionType.
     *
     * @param actionTypeString the type of action.
     * @param actionInt an int representing the type of action.
     */
    public ActionType(String actionTypeString, int actionInt) {
        actionType = actionTypeString;
        action = actionInt;
    }

    /**
     * Get the string for the action type.
     * @return the string for the action type.
     */
    public String getActionType() {
        return actionType;
    }

    /**
     * Get the int for the action type.
     * @return the int for the action type.
     */
    public int getAction() {
        return action;
    }
}
