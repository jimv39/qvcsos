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

import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.QVCSServerNamesFilter;
import java.io.File;
import javax.swing.DefaultComboBoxModel;

/**
 * Servers combo model.
 * @author Jim Voris
 */
public class ServersComboModel extends DefaultComboBoxModel<String> {
    private static final long serialVersionUID = 8918396013512401990L;
    // Use this ctor when a single file is selected.

    ServersComboModel() {
        super();

        // Where all the property files can be found...
        File propertiesDirectory = new java.io.File(System.getProperty("user.dir")
                + System.getProperty("file.separator")
                + QVCSConstants.QVCS_PROPERTIES_DIRECTORY);
        QVCSServerNamesFilter serverNameFilter = new QVCSServerNamesFilter();
        File[] serverFiles = propertiesDirectory.listFiles(serverNameFilter);
        for (File serverFile : serverFiles) {
            String serverName = serverNameFilter.getServerName(serverFile.getName());
            addElement(serverName);
        }
    }
}
