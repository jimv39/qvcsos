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

import com.qumasoft.guitools.qwin.FilterCollection;
import com.qumasoft.guitools.qwin.FilterManager;
import javax.swing.DefaultComboBoxModel;

/**
 * File filters mutable combo model.
 * @author Jim Voris
 */
public class FileFiltersMutableComboModel extends DefaultComboBoxModel<FilterCollection> {
    private static final long serialVersionUID = 2893580727680911927L;

    FileFiltersMutableComboModel() {
        FilterCollection[] filterCollections = FilterManager.getFilterManager().listFilterCollections();
        if (filterCollections != null) {
            for (FilterCollection filterCollection : filterCollections) {
                addElement(filterCollection);
            }
        }
    }
}
