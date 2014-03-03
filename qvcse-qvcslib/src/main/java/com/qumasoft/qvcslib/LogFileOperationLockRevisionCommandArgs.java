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
 * Lock revision command arguments.
 * @author Jim Voris
 */
public final class LogFileOperationLockRevisionCommandArgs implements java.io.Serializable {
    private static final long serialVersionUID = 3617325426540330146L;

    private String userName;
    private String outputFileName;
    private String fullWorkfileName;
    private String shortWorkfileName;
    private String checkOutComment;
    private String labelString;
    private String revisionString;

    /**
     * Creates a new instance of LogFileOperationLockRevisionCommandArgs.
     */
    public LogFileOperationLockRevisionCommandArgs() {
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
     * Get the checkout comments.
     * @return the checkout comments.
     */
    public String getCheckOutComment() {
        return checkOutComment;
    }

    /**
     * Set the checkout comments.
     * @param checkOutCmmnt the checkout comments.
     */
    public void setCheckOutComment(String checkOutCmmnt) {
        checkOutComment = checkOutCmmnt;
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
}
