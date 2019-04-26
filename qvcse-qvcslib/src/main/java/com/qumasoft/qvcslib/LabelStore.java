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
package com.qumasoft.qvcslib;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/**
 * Store label information.
 * @author Jim Voris
 */
public class LabelStore implements java.io.Serializable {
    private static final long serialVersionUID = -812011306079262647L;

    private final Map<String, Map<String, BriefLabelInfo>> labelCollection;

    /**
     * Default constructor.
     */
    public LabelStore() {
        this.labelCollection = Collections.synchronizedMap(new TreeMap<String, Map<String, BriefLabelInfo>>());
    }

    void addLabel(String projectName, LabelInfo labelInfo) {
        addLabel(projectName, labelInfo.getLabelString(), labelInfo.isFloatingLabel());
    }

    void addLabel(String projectName, String labelString, boolean isFloatingLabelFlag) {
        BriefLabelInfo briefLabelInfo = new BriefLabelInfo(labelString, isFloatingLabelFlag);

        Map<String, BriefLabelInfo> projectLabels = labelCollection.get(projectName);
        if (projectLabels == null) {
            // There are no labels for this project yet...
            projectLabels = Collections.synchronizedMap(new TreeMap<String, BriefLabelInfo>());
            labelCollection.put(projectName, projectLabels);
            projectLabels.put(labelString, briefLabelInfo);
        } else {
            BriefLabelInfo existingBriefLabelInfo = projectLabels.get(labelString);
            if (existingBriefLabelInfo != null) {
                // We only store the new label if it is a static label... i.e.
                // a static label trumps a floating label here.  This is so
                // we can distinguish 'purely' floating labels from those that
                // may have been used as a static label also. A date based
                // view can only be based on a floating label.
                if (!briefLabelInfo.isFloatingLabelFlag()) {
                    projectLabels.put(labelString, briefLabelInfo);
                }
            } else {
                projectLabels.put(labelString, briefLabelInfo);
            }
        }
    }

    void removeLabel(String projectName, String labelString) {
        Map<String, BriefLabelInfo> projectLabels = labelCollection.get(projectName);
        if (projectLabels != null) {
            projectLabels.remove(labelString);
        }
    }

    /**
     * Get an iterator for the labels associated with the given project.
     * @param projectName the project name.
     * @return an iterator over the labels for the given project. null if there are no labels for the project.
     */
    Iterator<BriefLabelInfo> getLabels(String projectName) {
        Iterator<BriefLabelInfo> iterator = null;
        Map<String, BriefLabelInfo> projectLabels = labelCollection.get(projectName);
        if (projectLabels != null) {
            iterator = projectLabels.values().iterator();
        }
        return iterator;
    }
}
