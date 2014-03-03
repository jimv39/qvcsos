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

import com.qumasoft.qvcslib.LogfileInfo;
import com.qumasoft.qvcslib.MergedInfoInterface;
import com.qumasoft.qvcslib.RevisionInformation;
import javax.swing.DefaultComboBoxModel;

/**
 * Revision combo model (with no default).
 * @author Jim Voris
 */
public class RevisionsComboModelWithNoDefault extends DefaultComboBoxModel<String> {
    private static final long serialVersionUID = 5966343363140964043L;

    /**
     * Creates a new instance of RevisionsComboModelWithNoDefault.
     */
    RevisionsComboModelWithNoDefault(MergedInfoInterface mergedInfo) {
        // Populate the list of revisions...
        if (mergedInfo != null) {
            LogfileInfo logfileInfo = mergedInfo.getLogfileInfo();
            RevisionInformation revisionInformation = logfileInfo.getRevisionInformation();
            for (int i = 0; i < logfileInfo.getLogFileHeaderInfo().getRevisionCount(); i++) {
                addElement(revisionInformation.getRevisionHeader(i).getRevisionString());
            }
        }
    }
}
