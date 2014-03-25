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
 * Identify the different types of actions that can be performed.
 * @author Jim Voris
 */
public class ActionType {

    /** Create an archive action. */
    public static final int CREATE = 5;
    /** Checkout a revision action. */
    public static final int CHECKOUT = 10;
    /** Check in a revision action. */
    public static final int CHECKIN = 20;
    /** Lock a revision action. */
    public static final int LOCK = 30;
    /** Unlock a revision action. */
    public static final int UNLOCK = 40;
    /** Apply a label action. */
    public static final int LABEL = 50;
    /** Remove a label action. */
    public static final int UNLABEL = 60;
    /** Change an archive header action. */
    public static final int CHANGE_HEADER = 70;
    /** Change a revision header action. */
    public static final int CHANGE_REVHEADER = 80;
    /** Mark a file obsolete action. */
    public static final int SET_OBSOLETE = 90;
    /** Set archive attributes action. */
    public static final int SET_ATTRIBUTES = 100;
    /** Set comment prefix action. */
    public static final int SET_COMMENT_PREFIX = 110;
    /** Set module description action. */
    public static final int SET_MODULE_DESCRIPTION = 120;
    /** Set revision description action. */
    public static final int SET_REVISION_DESCRIPTION = 130;
    /** Remove an archive action. */
    public static final int REMOVE = 140;
    /** Rename an archive action. */
    public static final int RENAME = 150;
    /** Move an archive action. */
    public static final int MOVE_FILE = 160;
    /** Something changed on a branch action. */
    public static final int CHANGE_ON_BRANCH = 170;

    private final String actionType;
    private final int action;

    /**
     * Creates a new instance of LogfileActionType.
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
