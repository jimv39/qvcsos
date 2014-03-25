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

/**
 * Label revision command arguments.
 * @author Jim Voris
 */
public final class LabelRevisionCommandArgs implements java.io.Serializable {
    private static final long serialVersionUID = 3348664451072880670L;

    private String userName;
    private boolean revisionFlag;         // true if the -r option is used
    private boolean duplicateFlag;        // true if the -duplabel flag is set
    private boolean floatingFlag;         // true if this is a floating label.
    private boolean reuseLabelFlag;       // true if we should re-use an existing label.
    private String labelString;
    private String duplicateLabelString;
    private String revisionString;        // this is the revision string that the label applies to.
    private String shortWorkfileName;
    private String errorMessage;

    /**
     * Creates a new instance of LogFileOperationLabelRevisionCommandArgs.
     */
    public LabelRevisionCommandArgs() {
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
     * Get the revision flag.
     * @return the revision flag.
     */
    public boolean getRevisionFlag() {
        return revisionFlag;
    }

    /**
     * Set the revision flag.
     * @param flag the revision flag.
     */
    public void setRevisionFlag(boolean flag) {
        revisionFlag = flag;
    }

    /**
     * Get the duplicate flag.
     * @return the duplicate flag.
     */
    public boolean getDuplicateFlag() {
        return duplicateFlag;
    }

    /**
     * Set the duplicate flag.
     * @param flag the duplicate flag.
     */
    public void setDuplicateFlag(boolean flag) {
        duplicateFlag = flag;
    }

    /**
     * Get the floating flag.
     * @return the floating flag.
     */
    public boolean getFloatingFlag() {
        return floatingFlag;
    }

    /**
     * Set the floating flag.
     * @param flag the floating flag.
     */
    public void setFloatingFlag(boolean flag) {
        floatingFlag = flag;
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
     * Get the reuse label flag.
     * @return the reuse label flag.
     */
    public boolean getReuseLabelFlag() {
        return reuseLabelFlag;
    }

    /**
     * Set the reuse label flag.
     * @param flag the reuse label flag.
     */
    public void setReuseLabelFlag(boolean flag) {
        reuseLabelFlag = flag;
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
     * Get the duplicate label string.
     * @return the duplicate label string.
     */
    public String getDuplicateLabelString() {
        return duplicateLabelString;
    }

    /**
     * Set the duplicate label string.
     * @param label the duplicate label string.
     */
    public void setDuplicateLabelString(String label) {
        duplicateLabelString = label;
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
     * Get the error message.
     * @return the error message.
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Set the error message.
     * @param msg the error message.
     */
    public void setErrorMessage(String msg) {
        errorMessage = msg;
    }
}
