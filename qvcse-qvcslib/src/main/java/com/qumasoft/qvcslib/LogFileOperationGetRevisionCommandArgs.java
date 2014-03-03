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
package com.qumasoft.qvcslib;

import java.util.Date;

/**
 * Get revision command arguments.
 * @author Jim Voris
 */
public final class LogFileOperationGetRevisionCommandArgs implements java.io.Serializable {
    private static final long serialVersionUID = -6287883556440309367L;

    private String userName;
    private String outputFileName;       // the name of the file we write the bytes to
    private String fullWorkfileName;     // the full name of the client workfile
    private String shortWorkfileName;    // the short name of the workfile
    private String labelString;
    private String revisionString;       // this is the revision string of the revision we will lock.
    private String failureReasonString;  // an explanation for failure.  Normally this is blank.
    private boolean byLabelFlag;
    private boolean byDateFlag;
    private Date byDateValue = null;
    private Utility.OverwriteBehavior overwriteBehavior;
    private Utility.TimestampBehavior timestampBehavior;

    /**
     * Creates a new instance of LogFileOperationGetRevisionCommandArgs.
     */
    public LogFileOperationGetRevisionCommandArgs() {
        timestampBehavior = Utility.TimestampBehavior.SET_TIMESTAMP_TO_NOW;
        overwriteBehavior = Utility.OverwriteBehavior.ASK_BEFORE_OVERWRITE_OF_WRITABLE_FILE;
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
     * Get the revision string.
     * @return the revision string.
     */
    public String getRevisionString() {
        return revisionString;
    }

    /**
     * Set the revision string.
     * @param revString the revision string.
     */
    public void setRevisionString(String revString) {
        revisionString = revString;
    }

    /**
     * Get the full workfile name.
     * @return the full workfile name.
     */
    public String getFullWorkfileName() {
        return fullWorkfileName;
    }

    /**
     * Set the full workfile name.
     * @param fullWorkName the full workfile name.
     */
    public void setFullWorkfileName(String fullWorkName) {
        fullWorkfileName = fullWorkName;
    }

    /**
     * Get the short workfile name.
     * @return the short workfile name.
     */
    public String getShortWorkfileName() {
        return shortWorkfileName;
    }

    /**
     * Set the short workfile name.
     * @param shortName the short workfile name.
     */
    public void setShortWorkfileName(String shortName) {
        shortWorkfileName = shortName;
    }

    /**
     * Get the output file name.
     * @return the output file name.
     */
    public String getOutputFileName() {
        return outputFileName;
    }

    /**
     * Set the output file name.
     * @param outputName the output file name.
     */
    public void setOutputFileName(String outputName) {
        outputFileName = outputName;
    }

    /**
     * Get the label string.
     * @return the label string.
     */
    public String getLabel() {
        return labelString;
    }

    /**
     * Set the label string.
     * @param label the label string.
     */
    public void setLabel(String label) {
        labelString = label;
    }

    /**
     * Get the failure reason string.
     * @return the failure reason string.
     */
    public String getFailureReason() {
        return failureReasonString;
    }

    /**
     * Set the failure reason string.
     * @param reasonString the failure reason string.
     */
    public void setFailureReason(String reasonString) {
        failureReasonString = reasonString;
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
    public Utility.TimestampBehavior getTimestampBehavior() {
        return timestampBehavior;
    }

    /**
     * Set the timestamp behavior.
     * @param tStampBehavior the timestamp behavior.
     */
    public void setTimestampBehavior(Utility.TimestampBehavior tStampBehavior) {
        timestampBehavior = tStampBehavior;
    }
}
