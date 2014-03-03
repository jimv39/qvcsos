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

import javax.swing.DefaultComboBoxModel;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;

/**
 * Look and feel combo model.
 * @author Jim Voris
 */
public class LookAndFeelComboModel extends DefaultComboBoxModel<String> {
    private static final long serialVersionUID = -5675848139727059312L;
    private final java.util.Map<String, String> lookAndFeelMap = new java.util.TreeMap<>();

    LookAndFeelComboModel() {
        UIManager.LookAndFeelInfo[] installedLookAndFeels = UIManager.getInstalledLookAndFeels();
        for (UIManager.LookAndFeelInfo installedLookAndFeel : installedLookAndFeels) {
            String lookAndFeelName = installedLookAndFeel.getName();
            addElement(lookAndFeelName);
            // Save off the installed look and feel object... we'll need it.
            lookAndFeelMap.put(lookAndFeelName, installedLookAndFeel.getClassName());
        }

        // And make sure the current default look and feel is in the combo box...
        LookAndFeel currentLookAndFeel = UIManager.getLookAndFeel();
        if (currentLookAndFeel != null) {
            if (null == lookAndFeelMap.get(currentLookAndFeel.getName())) {
                String currentLookAndFeelName = currentLookAndFeel.getName();
                String currentLookAndFeelClassName = currentLookAndFeel.getClass().getName();

                addElement(currentLookAndFeelName);
                lookAndFeelMap.put(currentLookAndFeelName, currentLookAndFeelClassName);
            }
        }
    }

    String getLookAndFeelClassName(String name) {
        return lookAndFeelMap.get(name);
    }
}
