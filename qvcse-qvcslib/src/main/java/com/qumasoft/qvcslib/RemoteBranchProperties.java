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
package com.qumasoft.qvcslib;

import java.util.Date;
import java.util.Properties;

/**
 * Remote branch properties.
 * @author Jim Voris
 */
public class RemoteBranchProperties extends RemoteProjectProperties {

    private String branchName = null;
    private static final String IS_READ_ONLY_BRANCH_FLAG_TAG = "QVCS_ISREADONLY_BRANCH_FLAG";
    private static final String IS_DATE_BASED_BRANCH_FLAG_TAG = "QVCS_ISDATE_BASED_BRANCH_FLAG";
    private static final String IS_TRANSLUCENT_BRANCH_FLAG_TAG = "QVCS_ISTRANSLUCENT_BRANCH_FLAG";
    private static final String IS_OPAQUE_BRANCH_FLAG_TAG = "QVCS_ISOPAQUE_BRANCH_FLAG";
    private static final String DATE_BASED_BRANCH_DATE_TAG = "QVCS_DATE_BASED_BRANCH_DATE";
    private static final String DATE_BASED_BRANCH_TAG = "QVCS_DATE_BASED_BRANCH";
    private static final String BRANCH_PARENT_TAG = "QVCS_BRANCH_PARENT";
    private static final String BRANCH_DATE_TAG = "QVCS_BRANCH_DATE";

    /**
     * Build remote branch properties using project name, and branch name. (Used for unit tests).
     * @param project the project name.
     * @param branch the branch name.
     */
    public RemoteBranchProperties(String project, String branch) {
        super(null, project);
        branchName = branch;
    }

    /**
     * This constructor is used so a client application can construct a remote project properties object using the response message from the server that is serving this project.
     * @param project the project name.
     * @param branch the branch name.
     * @param properties the project properties.
     */
    public RemoteBranchProperties(String project, String branch, Properties properties) {
        super(project, properties);
        branchName = branch;
    }

    /**
     * Get the branch name.
     * @return the branch name.
     */
    public String getBranchName() {
        return branchName;
    }

    /**
     * Get the is read-only branch flag tag.
     * @return the is read-only branch flag tag.
     */
    public static String getIsReadOnlyBranchFlagTag() {
        return IS_READ_ONLY_BRANCH_FLAG_TAG;
    }

    /**
     * Get the is date-based branch flag tag.
     * @return the is date-based branch flag tag.
     */
    public static String getIsDateBasedBranchFlagTag() {
        return IS_DATE_BASED_BRANCH_FLAG_TAG;
    }

    /**
     * Get the is translucent branch flag tag.
     * @return the is translucent branch flag tag.
     */
    public static String getIsTranslucentBranchFlagTag() {
        return IS_TRANSLUCENT_BRANCH_FLAG_TAG;
    }

    /**
     * Get the is opaque branch flag tag.
     * @return the is opaque branch flag tag.
     */
    public static String getIsOpaqueBranchFlagTag() {
        return IS_OPAQUE_BRANCH_FLAG_TAG;
    }

    /**
     * Get the date based branch date tag.
     * @return the date based branch date tag.
     */
    public static String getDateBasedBranchDateTag() {
        return DATE_BASED_BRANCH_DATE_TAG;
    }

    /**
     * Get the date based branch tag.
     * @return the date based branch tag.
     */
    public static String getDateBasedBranchTag() {
        return DATE_BASED_BRANCH_TAG;
    }

    /**
     * Get the branch parent tag.
     * @return the branch parent tag.
     */
    public static String getBranchParentTag() {
        return BRANCH_PARENT_TAG;
    }

    /**
     * Get the branch date tag.
     * @return the branch date tag.
     */
    public static String getBranchDateTag() {
        return BRANCH_DATE_TAG;
    }

    /**
     * Get the is read-only branch flag.
     * @return the is read-only branch flag.
     */
    public boolean getIsReadOnlyBranchFlag() {
        return getBooleanValue(getIsReadOnlyBranchFlagTag());
    }

    /**
     * Set the is read-only branch flag.
     * @param flag the is read-only branch flag.
     */
    public void setIsReadOnlyBranchFlag(boolean flag) {
        setBooleanValue(getIsReadOnlyBranchFlagTag(), flag);
    }

    /**
     * Get the is date based branch flag.
     * @return the is date based branch flag.
     */
    public boolean getIsDateBasedBranchFlag() {
        return getBooleanValue(getIsDateBasedBranchFlagTag());
    }

    /**
     * Set the is date based branch flag.
     * @param flag the is date based branch flag.
     */
    public void setIsDateBasedBranchFlag(boolean flag) {
        setBooleanValue(getIsDateBasedBranchFlagTag(), flag);
    }

    /**
     * Get the is translucent branch flag.
     * @return the is translucent branch flag.
     */
    public boolean getIsTranslucentBranchFlag() {
        return getBooleanValue(getIsTranslucentBranchFlagTag());
    }

    /**
     * Set the is translucent branch flag.
     * @param flag the is translucent branch flag.
     */
    public void setIsTranslucentBranchFlag(boolean flag) {
        setBooleanValue(getIsTranslucentBranchFlagTag(), flag);
    }

    /**
     * Get the is opaque branch flag.
     * @return the is opaque branch flag.
     */
    public boolean getIsOpaqueBranchFlag() {
        return getBooleanValue(getIsOpaqueBranchFlagTag());
    }

    /**
     * Set the is opaque branch flag.
     * @param flag the is opaque branch flag.
     */
    public void setIsOpaqueBranchFlag(boolean flag) {
        setBooleanValue(getIsOpaqueBranchFlagTag(), flag);
    }

    /**
     * Get the date based date.
     * @return the date based date.
     */
    public Date getDateBasedDate() {
        return getDateValue(getDateBasedBranchDateTag());
    }

    /**
     * Set the date based date.
     * @param date the date based date.
     */
    public void setDateBaseDate(Date date) {
        setDateValue(getDateBasedBranchDateTag(), date);
    }

    /**
     * A date based branch <i>must</i> be based on either the trunk, or some branch so that in the case where a file is branched, we can choose from among the revisions that
     * can be active on the given date.
     * @return the branch (or trunk) that identifies which branch to associate with this date-based branch.
     */
    public String getDateBasedBranch() {
        return getStringValue(getDateBasedBranchTag());
    }

    /**
     * Set the date based branch.
     * @param branch the date based branch.
     */
    public void setDateBasedBranch(String branch) {
        setStringValue(getDateBasedBranchTag(), branch);
    }

    /**
     * Get the branch parent name.
     * @return the branch parent name.
     */
    public String getBranchParent() {
        return getStringValue(getBranchParentTag());
    }

    /**
     * Set the branch parent name.
     * @param branchParent the branch parent name.
     */
    public void setBranchParent(String branchParent) {
        setStringValue(getBranchParentTag(), branchParent);
    }

    /**
     * Get the branch date.
     * @return the branch date.
     */
    public Date getBranchDate() {
        return getDateValue(getBranchDateTag());
    }

    /**
     * Set the branch date.
     * @param date the branch date.
     */
    public void setBranchDate(Date date) {
        setDateValue(getBranchDateTag(), date);
    }
}
