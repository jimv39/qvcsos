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
package com.qumasoft.guitools.qwin;

import ch.qos.logback.classic.Level;
import java.util.Enumeration;
import javax.swing.JRadioButtonMenuItem;

/**
 * Activity pane log level button group.
 * @author Jim Voris
 */
public class ActivityPaneLogLevelButtonGroup extends javax.swing.ButtonGroup {
    private static final long serialVersionUID = -3415649266477307547L;

    /**
     * Creates a new instance of ActivityPaneLogLevelButtonGroup.
     */
    public ActivityPaneLogLevelButtonGroup() {
        super();
    }

    Level getSelectedLevel() {
        Level retVal = Level.INFO;
        Enumeration it = getElements();
        while (it.hasMoreElements()) {
            JRadioButtonMenuItem button = (JRadioButtonMenuItem) it.nextElement();
            if (button.getModel().isSelected()) {
                String buttonString = button.getText();
                retVal = Level.toLevel(buttonString);
                break;
            }
        }
        return retVal;
    }

    void selectActiveButton(Level level) {
        String levelString = level.toString();
        Enumeration it = getElements();
        while (it.hasMoreElements()) {
            JRadioButtonMenuItem button = (JRadioButtonMenuItem) it.nextElement();
            if (button.getText().equalsIgnoreCase(levelString)) {
                setSelected(button.getModel(), true);
                break;
            }
        }
    }
}
