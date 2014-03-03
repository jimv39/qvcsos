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

/**
 * Label directory command arguments.
 * @author Jim Voris
 */
public final class LogFileOperationLabelDirectoryCommandArgs implements java.io.Serializable {
    private static final long serialVersionUID = 22546285668625467L;

    private String userName;
    private boolean duplicateFlag;        // true if the -duplabel flag is set
    private boolean floatingFlag;         // true if this is a floating label.
    private boolean reuseLabelFlag;       // true if we should re-use an existing label.
    private boolean recurseFlag;
    private String newLabelString;
    private String existingLabelString;

    /**
     * Creates a new instance of LogFileOperationLabelDirectoryCommandArgs.
     */
    public LogFileOperationLabelDirectoryCommandArgs() {
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
     * Get the new label string.
     * @return the new label string.
     */
    public String getNewLabelString() {
        return newLabelString;
    }

    /**
     * Set the new label string.
     * @param label the new label string.
     */
    public void setNewLabelString(String label) {
        newLabelString = label;
    }

    /**
     * Get the existing label string.
     * @return the existing label string.
     */
    public String getExistingLabelString() {
        return existingLabelString;
    }

    /**
     * Set the existing label string.
     * @param label the existing label string.
     */
    public void setExistingLabelString(String label) {
        existingLabelString = label;
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
}
