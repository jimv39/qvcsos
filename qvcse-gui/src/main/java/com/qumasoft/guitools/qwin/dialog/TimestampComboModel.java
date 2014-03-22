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
package com.qumasoft.guitools.qwin.dialog;

import com.qumasoft.qvcslib.Utility;
import javax.swing.DefaultComboBoxModel;

/**
 * Timestamp combo model.
 * @author Jim Voris
 */
public class TimestampComboModel extends DefaultComboBoxModel<String> {

    private static final String NOW = "Set file timestamp to now";
    private static final String REVISION_CHECKIN_TIME = "Set file timestamp to revision checkin time";
    private static final String REVISION_EDIT_TIME = "Set file timestamp to revision edit time";
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new instance of TimestampComboModel.
     */
    public TimestampComboModel() {
        addElement(NOW);
        addElement(REVISION_CHECKIN_TIME);
        addElement(REVISION_EDIT_TIME);
    }

    Utility.TimestampBehavior getSelectedTimeStampBehavior() {
        Utility.TimestampBehavior timeStampBehavior = Utility.TimestampBehavior.SET_TIMESTAMP_TO_NOW;
        String selectedString = (String) getSelectedItem();
        if (selectedString.compareTo(NOW) == 0) {
            timeStampBehavior = Utility.TimestampBehavior.SET_TIMESTAMP_TO_NOW;
        } else if (selectedString.compareTo(REVISION_CHECKIN_TIME) == 0) {
            timeStampBehavior = Utility.TimestampBehavior.SET_TIMESTAMP_TO_CHECKIN_TIME;
        } else if (selectedString.compareTo(REVISION_EDIT_TIME) == 0) {
            timeStampBehavior = Utility.TimestampBehavior.SET_TIMESTAMP_TO_EDIT_TIME;
        }

        return timeStampBehavior;
    }
}
