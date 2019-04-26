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

import java.io.Serializable;

/**
 * Brief label info. A skinnier flavor of label information.
 * @author Jim Voris
 */
public final class BriefLabelInfo implements Serializable {
    private static final long serialVersionUID = 5889703350837042748L;
    private String labelString;
    private boolean floatingLabelFlag;

    /**
     * Creates a new instance of BriefLabelInfo.
     */
    public BriefLabelInfo() {
    }

    /**
     * Construct a brief label info object.
     * @param labelInfo the LabelInfo from which we create this object.
     */
    public BriefLabelInfo(LabelInfo labelInfo) {
        setLabelString(labelInfo.getLabelString());
        setFloatingLabelFlag(labelInfo.isFloatingLabel());
    }

    /**
     * Construct a brief label info object using the given parameters.
     * @param label the label string.
     * @param flag flag indicating if this is a floating label (true), or not (false).
     */
    public BriefLabelInfo(String label, boolean flag) {
        setLabelString(label);
        setFloatingLabelFlag(flag);
    }

    /**
     * Get the label string.
     * @return the label string.
     */
    public String getLabelString() {
        return labelString;
    }

    /**
     * Set the label string.
     * @param label the label string.
     */
    public void setLabelString(String label) {
        this.labelString = label;
    }

    /**
     * Is this a floating label.
     * @return true if this is a floating label; false otherwise.
     */
    public boolean isFloatingLabelFlag() {
        return floatingLabelFlag;
    }

    /**
     * Set the floating label flag.
     * @param flag the floating label flag.
     */
    public void setFloatingLabelFlag(boolean flag) {
        this.floatingLabelFlag = flag;
    }
}
