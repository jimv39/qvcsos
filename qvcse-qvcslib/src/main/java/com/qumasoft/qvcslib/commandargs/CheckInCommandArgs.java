/*   Copyright 2004-2019 Jim Voris
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

import java.util.Date;

/**
 * Checkin command arguments.
 * @author Jim Voris
 */
public class CheckInCommandArgs implements java.io.Serializable {
    private static final long serialVersionUID = 2716987134008081357L;

    private boolean createNewRevisionIfEqualFlag;
    private boolean protectWorkfileFlag;
    private String userName;
    private String shortWorkfileName;
    private String fullWorkfileName;        // the full name of the client workfile
    private String checkInComment;
    private String labelString;             // If we're applying a label, this is its value.
    private String projectName;             // The name of the project.
    private String branchName;
    private String failureReason;           // Normally blank. We fill this in to explain an error condition.
    private Date inputFileTimestamp;        // This is the timestamp on the workfile
    private Date checkInTimestamp;          // this is the time we did the check in.  By default, this is null, which means NOW.
    private transient String newRevisionString;     // this is the revision string that will be associated with the new revision.
    private transient String parentRevisionString;  // the revision string for the new revision's parent revision.

    /**
     * Creates a new instance of LogFileOperationCheckInCommandArgs.
     */
    public CheckInCommandArgs() {
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
        this.userName = user;
    }

    /**
     * Get the new revision string.
     * @return the new revision string.
     */
    public String getNewRevisionString() {
        return newRevisionString;
    }

    /**
     * Set the new revision string.
     * @param newRevString the new revision string.
     */
    public void setNewRevisionString(String newRevString) {
        this.newRevisionString = newRevString;
    }

    /**
     * Get the parent revision string.
     * @return the parent revision string.
     */
    public String getParentRevisionString() {
        return parentRevisionString;
    }

    /**
     * Set the parent revision string.
     * @param parentRevString the parent revision string.
     */
    public void setParentRevisionString(String parentRevString) {
        this.parentRevisionString = parentRevString;
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
        this.fullWorkfileName = fullWorkName;
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
        this.shortWorkfileName = shortName;
    }

    /**
     * Get the checkin comment.
     * @return the checkin comment.
     */
    public String getCheckInComment() {
        return checkInComment;
    }

    /**
     * Set the checkin comment.
     * @param checkInCmmnt the checkin comment.
     */
    public void setCheckInComment(String checkInCmmnt) {
        this.checkInComment = checkInCmmnt;
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
     * Get the project name.
     * @return the project name.
     */
    public String getProjectName() {
        return projectName;
    }

    /**
     * Set the project name.
     * @param project the project name.
     */
    public void setProjectName(final String project) {
        this.projectName = project;
    }

    /**
     * Get the branch name.
     * @return the branch name.
     */
    public String getBranchName() {
        return branchName;
    }

    /**
     * Set the branch name.
     * @param branch the branch name.
     */
    public void setBranchName(final String branch) {
        this.branchName = branch;
    }

    /**
     * Get the failure reason.
     * @return the failure reason.
     */
    public String getFailureReason() {
        return failureReason;
    }

    /**
     * Set the failure reason.
     * @param reason the failure reason.
     */
    public void setFailureReason(final String reason) {
        this.failureReason = reason;
    }

    /**
     * Get the input file timestamp.
     * @return the input file timestamp.
     */
    public Date getInputfileTimeStamp() {
        return inputFileTimestamp;
    }

    /**
     * Set the input file timestamp.
     * @param timestamp the input file timestamp.
     */
    public void setInputfileTimeStamp(Date timestamp) {
        inputFileTimestamp = timestamp;
    }

    /**
     * Get the checkin timestamp.
     * @return the checkin timestamp.
     */
    public Date getCheckInTimestamp() {
        return checkInTimestamp;
    }

    /**
     * Set the checkin timestamp.
     * @param timestamp the checkin timestamp.
     */
    public void setCheckInTimestamp(Date timestamp) {
        checkInTimestamp = timestamp;
    }

    /**
     * Get the create new revision if equal flag.
     * @return the create new revision if equal flag.
     */
    public boolean getCreateNewRevisionIfEqual() {
        return createNewRevisionIfEqualFlag;
    }

    /**
     * Set the create new revision if equal flag.
     * @param flag the create new revision if equal flag.
     */
    public void setCreateNewRevisionIfEqual(boolean flag) {
        createNewRevisionIfEqualFlag = flag;
    }

    /**
     * Get the protect workfile flag.
     * @return the protect workfile flag.
     */
    public boolean getProtectWorkfileFlag() {
        return protectWorkfileFlag;
    }

    /**
     * Set the protect workfile flag.
     * @param flag the protect workfile flag.
     */
    public void setProtectWorkfileFlag(boolean flag) {
        protectWorkfileFlag = flag;
    }
}
