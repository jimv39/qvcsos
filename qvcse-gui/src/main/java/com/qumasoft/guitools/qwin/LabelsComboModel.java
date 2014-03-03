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

import com.qumasoft.qvcslib.BriefLabelInfo;
import com.qumasoft.qvcslib.LabelInfo;
import com.qumasoft.qvcslib.LabelManager;
import com.qumasoft.qvcslib.LogfileInfo;
import com.qumasoft.qvcslib.MergedInfoInterface;
import com.qumasoft.qvcslib.QVCSConstants;
import java.util.Iterator;
import javax.swing.DefaultComboBoxModel;

/**
 * Labels combo model.
 * @author Jim Voris
 */
public class LabelsComboModel extends DefaultComboBoxModel<String> {
    private static final long serialVersionUID = -5366613680145842609L;

    // Use this ctor when a single file is selected.
    LabelsComboModel(MergedInfoInterface mergedInfo) {
        // If we have any labels...
        LogfileInfo logfileInfo = mergedInfo.getLogfileInfo();
        if (logfileInfo.getLogFileHeaderInfo().getLogFileHeader().versionCount() > 0) {
            LabelInfo[] labelInfo = logfileInfo.getLogFileHeaderInfo().getLabelInfo();
            for (LabelInfo labelInfo1 : labelInfo) {
                addElement(labelInfo1.getLabelString());
            }
        }
    }

    // Use this ctor when more than a single file is selected.
    LabelsComboModel() {
        Iterator<BriefLabelInfo> iterator = LabelManager.getInstance().getLabels(QWinFrame.getQWinFrame().getProjectName());
        if (iterator != null) {
            while (iterator.hasNext()) {
                String labelString = iterator.next().getLabelString();
                addElement(labelString);
            }
        }
    }

    // Use this ctor for defining views.
    LabelsComboModel(boolean includeTrunkFlag) {
        Iterator<BriefLabelInfo> iterator = LabelManager.getInstance().getLabels(QWinFrame.getQWinFrame().getProjectName());
        if (includeTrunkFlag) {
            addElement(QVCSConstants.QVCS_TRUNK_VIEW);
        }
        if (iterator != null) {
            while (iterator.hasNext()) {
                BriefLabelInfo briefLabelInfo = iterator.next();
                if (briefLabelInfo.isFloatingLabelFlag()) {
                    String labelString = briefLabelInfo.getLabelString();
                    addElement(labelString);
                }
            }
        }
    }
}
