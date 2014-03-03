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
 * Logfile action type for some kind of change on a branch. Useful to send notifications to listeners.
 * @author Jim Voris
 */
public class LogfileActionChangeOnBranch extends LogfileActionType {

    /** The branch change was a rename action. */
    public static final int RENAME_ON_BRANCH = 10;
    /** The branch change was a delete action. */
    public static final int DELETE_ON_BRANCH = 11;
    /** The branch change was a move action. */
    public static final int MOVE_ON_BRANCH = 12;
    private final int branchActionType;
    private String oldShortWorkfileName = null;

    /**
     * Creates a new instance of LogfileActionChangeOnBranch.
     * @param subject the logfile that changed.
     * @param branchAction the type of action on the branch.
     */
    public LogfileActionChangeOnBranch(ArchiveInfoInterface subject, int branchAction) {
        super("Change on branch", LogfileActionType.CHANGE_ON_BRANCH);
        this.branchActionType = branchAction;
    }

    /**
     * Get the branch action type.
     * @return the branch action type.
     */
    public int getBranchActionType() {
        return this.branchActionType;
    }

    /**
     * Get the old short workfile name.
     * @return the old short workfile name.
     */
    public String getOldShortWorkfileName() {
        return oldShortWorkfileName;
    }

    /**
     * Set the old short workfile name.
     * @param oldShortName the old short workfile name.
     */
    public void setOldShortWorkfileName(String oldShortName) {
        this.oldShortWorkfileName = oldShortName;
    }
}
