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
package com.qumasoft.guitools.qwin.dialog;

import com.qumasoft.qvcslib.Utility;
import javax.swing.DefaultComboBoxModel;

/**
 * Overwrite workfile combo model.
 * @author Jim Voris
 */
public class OverwriteWorkfileComboModel extends DefaultComboBoxModel<String> {
    private static final long serialVersionUID = -4846765175287695009L;

    /** Ask before overwriting the workfile that may have changed. */
    public static final String ASK_BEFORE_OVERWRITE = "Ask before overwriting a workfile that you may have changed";
    /** Do not replace a workfile that you may have changed. */
    public static final String DO_NOT_REPLACE_WORKFILE = "Do not replace a workfile that you may have changed";
    /** Replace any workfile that you may have changed, losing all of your changes. */
    public static final String REPLACE_WORKFILE = "Replace any workfile that you may have changed, losing all your changes.";

    OverwriteWorkfileComboModel() {
        addElement(ASK_BEFORE_OVERWRITE);
        addElement(DO_NOT_REPLACE_WORKFILE);
        addElement(REPLACE_WORKFILE);
    }

    Utility.OverwriteBehavior getSelectedOverwriteBehavior() {
        Utility.OverwriteBehavior overwriteBehavior = Utility.OverwriteBehavior.ASK_BEFORE_OVERWRITE_OF_WRITABLE_FILE;
        String selectedString = (String) getSelectedItem();
        if (selectedString.compareTo(ASK_BEFORE_OVERWRITE) == 0) {
            overwriteBehavior = Utility.OverwriteBehavior.ASK_BEFORE_OVERWRITE_OF_WRITABLE_FILE;
        } else if (selectedString.compareTo(DO_NOT_REPLACE_WORKFILE) == 0) {
            overwriteBehavior = Utility.OverwriteBehavior.DO_NOT_REPLACE_WRITABLE_FILE;
        } else if (selectedString.compareTo(REPLACE_WORKFILE) == 0) {
            overwriteBehavior = Utility.OverwriteBehavior.REPLACE_WRITABLE_FILE;
        }

        return overwriteBehavior;
    }
}
