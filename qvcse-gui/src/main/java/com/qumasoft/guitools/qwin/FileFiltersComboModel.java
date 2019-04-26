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

import javax.swing.DefaultComboBoxModel;

/**
 * File filters combo model.
 * @author Jim Voris
 */
public class FileFiltersComboModel extends DefaultComboBoxModel<FilterCollection> {
    private static final long serialVersionUID = -1852138274614689333L;

    FileFiltersComboModel() {
        FilterCollection[] filterCollections = FilterManager.getFilterManager().listFilterCollections();
        if (filterCollections != null) {
            for (FilterCollection filterCollection : filterCollections) {
                addElement(filterCollection);
            }
        }
    }

    FileFiltersComboModel(String projectName) {
        FilterCollection[] filterCollections = FilterManager.getFilterManager().listFilterCollections();
        if (filterCollections != null) {
            for (FilterCollection filterCollection : filterCollections) {
                if (filterCollection.getAssociatedProjectName().equals(QWinFrame.GLOBAL_PROJECT_NAME)) {
                    addElement(filterCollection);
                } else if (filterCollection.getAssociatedProjectName().equals(projectName)) {
                    addElement(filterCollection);
                }
            }
        }
    }
}
