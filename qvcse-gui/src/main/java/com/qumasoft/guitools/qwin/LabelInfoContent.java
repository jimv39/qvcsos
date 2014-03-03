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

import com.qumasoft.qvcslib.AccessList;
import com.qumasoft.qvcslib.LabelInfo;
import com.qumasoft.qvcslib.LogfileInfo;
import com.qumasoft.qvcslib.MergedInfoInterface;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/**
 * Label info content.
 * @author Jim Voris
 */
public class LabelInfoContent {

    private final Map<String, String> labelInfo;

    /**
     * Creates a new instance of RevisionInfoContent.
     * @param mergedInfo the file to work on.
     */
    public LabelInfoContent(MergedInfoInterface mergedInfo) {
        this.labelInfo = Collections.synchronizedMap(new TreeMap<String, String>());
        if (mergedInfo.getArchiveInfo() != null) {
            addLabelInformation(mergedInfo);
        }
    }

    private void addLabelInformation(MergedInfoInterface mergedInfo) {
        LogfileInfo logfileInfo = mergedInfo.getLogfileInfo();
        LabelInfo[] labelInfoArray = logfileInfo.getLogFileHeaderInfo().getLabelInfo();
        int labelCount = 0;
        if (labelInfoArray != null) {
            labelCount = labelInfoArray.length;
        }
        AccessList accessList = new AccessList(logfileInfo.getLogFileHeaderInfo().getModifierList());
        for (int i = 0; i < labelCount; i++) {
            LabelInfo labelInfoElement = labelInfoArray[i];
            if (!labelInfoElement.getIsObsolete()) {
                String labelUserName = accessList.indexToUser(labelInfoElement.getCreatorIndex());
                String mapKey = labelInfoElement.getSortableRevisionString() + Integer.toString(i);
                labelInfo.put(mapKey, "Revision " + labelInfoElement.getLabelRevisionString() + " labeled by " + labelUserName + " as '"
                        + labelInfoElement.getLabelString() + "'\n");
            }
        }
    }

    Iterator iterator() {
        return labelInfo.values().iterator();
    }
}
