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
 * Unlock revision command arguments.
 * @author Jim Voris
 */
public final class LogFileOperationUnlockRevisionCommandArgs implements java.io.Serializable {
    private static final long serialVersionUID = -3474830500660803986L;

    private String userName;
    private String outputFileName;       // the name of the file we write the bytes to
    private String fullWorkfileName;     // the full name of the client workfile
    private String shortWorkfileName;
    private String revisionString;       // this is the revision string of the revision we will unlock.
    private Utility.UndoCheckoutBehavior unDoCheckoutBehavior;

    /**
     * Creates a new instance of LogFileOperationUnlockRevisionCommandArgs.
     */
    public LogFileOperationUnlockRevisionCommandArgs() {
        unDoCheckoutBehavior = Utility.UndoCheckoutBehavior.JUST_UNLOCK_ARCHIVE;
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
     * Get the output filename.
     * @return the output filename.
     */
    public String getOutputFileName() {
        return outputFileName;
    }

    /**
     * Set the output filename.
     * @param outputName the output filename.
     */
    public void setOutputFileName(String outputName) {
        outputFileName = outputName;
    }

    /**
     * Get the undo checkout behavior.
     * @return the undo checkout behavior.
     */
    public Utility.UndoCheckoutBehavior getUndoCheckoutBehavior() {
        return unDoCheckoutBehavior;
    }

    /**
     * Set the undo checkout behavior.
     * @param unDoChckoutBehavior the undo checkout behavior.
     */
    public void setUndoCheckoutBehavior(Utility.UndoCheckoutBehavior unDoChckoutBehavior) {
        unDoCheckoutBehavior = unDoChckoutBehavior;
    }
}
