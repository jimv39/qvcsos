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
 * Unlabel directory command arguments.
 * @author Jim Voris
 */
public final class UnLabelDirectoryCommandArgs implements java.io.Serializable {
    private static final long serialVersionUID = -6846917271834978024L;

    private String userName;
    private String labelString;
    private boolean recurseFlag;

    /**
     * Creates a new instance of LogFileOperationUnLabelDirectoryCommandArgs.
     */
    public UnLabelDirectoryCommandArgs() {
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
