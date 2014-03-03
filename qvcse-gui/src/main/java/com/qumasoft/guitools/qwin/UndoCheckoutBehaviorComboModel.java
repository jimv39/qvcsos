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
package com.qumasoft.guitools.qwin;

import com.qumasoft.qvcslib.Utility;
import javax.swing.DefaultComboBoxModel;

/**
 * Un-do checkout behavior combo model.
 * @author Jim Voris
 */
public class UndoCheckoutBehaviorComboModel extends DefaultComboBoxModel<String> {
    private static final long serialVersionUID = -83856886268161540L;

    private static final String JUST_UNLOCK_ARCHIVE = "Just unlock the file";
    private static final String DELETE_THE_WORKFILE = "Delete the workfile";
    private static final String RESTORE_DEFAULT_REVISION = "Restore workfile to the default revision";
    private static final String RESTORE_LOCKED_REVISION = "Restore workfile to the revision you had checked out";

    /**
     * Creates a new instance of UndoCheckoutBehaviorComboModel.
     */
    public UndoCheckoutBehaviorComboModel() {
        addElement(JUST_UNLOCK_ARCHIVE);
        addElement(DELETE_THE_WORKFILE);
        addElement(RESTORE_DEFAULT_REVISION);
        addElement(RESTORE_LOCKED_REVISION);
    }

    Utility.UndoCheckoutBehavior getSelectedUndoCheckoutBehavior() {
        Utility.UndoCheckoutBehavior undoCheckoutBehavior = Utility.UndoCheckoutBehavior.JUST_UNLOCK_ARCHIVE;
        String selectedString = (String) getSelectedItem();
        if (selectedString.compareTo(DELETE_THE_WORKFILE) == 0) {
            undoCheckoutBehavior = Utility.UndoCheckoutBehavior.DELETE_WORKFILE;
        } else if (selectedString.compareTo(JUST_UNLOCK_ARCHIVE) == 0) {
            undoCheckoutBehavior = Utility.UndoCheckoutBehavior.JUST_UNLOCK_ARCHIVE;
        } else if (selectedString.compareTo(RESTORE_DEFAULT_REVISION) == 0) {
            undoCheckoutBehavior = Utility.UndoCheckoutBehavior.RESTORE_DEFAULT_REVISION;
        } else if (selectedString.compareTo(RESTORE_LOCKED_REVISION) == 0) {
            undoCheckoutBehavior = Utility.UndoCheckoutBehavior.RESTORE_LOCKED_REVISION;
        }

        return undoCheckoutBehavior;
    }
}
