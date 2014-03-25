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
package com.qumasoft.qvcslib.commandargs;

import com.qumasoft.qvcslib.Utility;
import java.util.Date;

/**
 * Get directory command arguments.
 * @author Jim Voris
 */
public final class GetDirectoryCommandArgs implements java.io.Serializable {
    private static final long serialVersionUID = -5243149170580303043L;

    private String userName;
    private String labelString;
    private String workfileBaseDirectory;
    private boolean recurseFlag;
    private boolean byLabelFlag;
    private boolean byDateFlag;
    private Date byDateValue = null;
    private Utility.OverwriteBehavior overwriteBehavior;
    private Utility.TimestampBehavior timestampBehavior;

    /**
     * Creates a new instance of LogFileOperationGetDirectoryCommandArgs.
     */
    public GetDirectoryCommandArgs() {
        timestampBehavior = Utility.TimestampBehavior.SET_TIMESTAMP_TO_NOW;
        overwriteBehavior = Utility.OverwriteBehavior.ASK_BEFORE_OVERWRITE_OF_WRITABLE_FILE;
    }

    /**
     * Get the user name.
     * @return the user name.
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Set the user name.
     * @param user the user name.
     */
    public void setUserName(String user) {
        userName = user;
    }

    /**
     * Get the recurse flag.
     * @return the recurse flag.
     */
    public boolean getRecurseFlag() {
        return recurseFlag;
    }

    /**
     * Set the recurse flag.
     * @param flag the recurse flag.
     */
    public void setRecurseFlag(boolean flag) {
        recurseFlag = flag;
    }

    /**
     * Get the by label flag.
     * @return the by label flag.
     */
    public boolean getByLabelFlag() {
        return byLabelFlag;
    }

    /**
     * Set the by label flag.
     * @param flag the by label flag.
     */
    public void setByLabelFlag(boolean flag) {
        byLabelFlag = flag;
    }

    /**
     * Get the by date flag.
     * @return the by date flag.
     */
    public boolean getByDateFlag() {
        return byDateFlag;
    }

    /**
     * Set the by date flag.
     * @param flag the by date flag.
     */
    public void setByDateFlag(boolean flag) {
        byDateFlag = flag;
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
        labelString = label;
    }

    /**
     * Get the by date value.
     * @return the by date value.
     */
    public Date getByDateValue() {
        return byDateValue;
    }

    /**
     * Set the by date value.
     * @param date the by date value.
     */
    public void setByDateValue(Date date) {
        byDateValue = date;
    }

    /**
     * Get the workfile base directory.
     * @return the workfile base directory.
     */
    public String getWorkfileBaseDirectory() {
        return workfileBaseDirectory;
    }

    /**
     * Set the workfile base directory.
     * @param workfileBaseDir the workfile base directory.
     */
    public void setWorkfileBaseDirectory(String workfileBaseDir) {
        workfileBaseDirectory = workfileBaseDir;
    }

    /**
     * Get the overwrite behavior.
     * @return the overwrite behavior.
     */
    public Utility.OverwriteBehavior getOverwriteBehavior() {
        return overwriteBehavior;
    }

    /**
     * Set the overwrite behavior.
     * @param overwritBehavior the overwrite behavior.
     */
    public void setOverwriteBehavior(Utility.OverwriteBehavior overwritBehavior) {
        overwriteBehavior = overwritBehavior;
    }

    /**
     * Get the timestamp behavior.
     * @return the timestamp behavior.
     */
    public Utility.TimestampBehavior getTimeStampBehavior() {
        return timestampBehavior;
    }

    /**
     * Set the timestamp behavior.
     * @param tStampBehavior the timestamp behavior.
     */
    public void setTimeStampBehavior(Utility.TimestampBehavior tStampBehavior) {
        timestampBehavior = tStampBehavior;
    }
}
