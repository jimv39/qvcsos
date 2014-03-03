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

import java.util.Iterator;
import java.util.Map;
import javax.swing.DefaultComboBoxModel;

/**
 * Project names combo model.
 * @author Jim Voris
 */
public class ProjectNamesComboModel extends DefaultComboBoxModel<String> {
    private static final long serialVersionUID = 2821446054107664998L;

    /** Global project name -- All projects. */
    public static final String GLOBAL_PROJECT_NAME = "All Projects";

    ProjectNamesComboModel() {
        // Add global project name as 1st entry
        addElement(GLOBAL_PROJECT_NAME);

        // Get the list of projects from the project tree...
        Map projectList = QWinFrame.getQWinFrame().getTreeModel().getProjectNames();
        Iterator it = projectList.values().iterator();
        while (it.hasNext()) {
            String projectName = (String) it.next();
            addElement(projectName);
        }
    }
}
