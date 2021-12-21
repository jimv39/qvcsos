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
    private static final String IS_TAG_BASED_BRANCH_FLAG_TAG = "QVCS_ISTAG_BASED_BRANCH_FLAG";
    private static final String IS_FEATURE_BRANCH_FLAG_TAG = "QVCS_ISFEATURE_BRANCH_FLAG";
    private static final String IS_RELEASE_BRANCH_FLAG_TAG = "QVCS_ISRELEASE_BRANCH_FLAG";
    private static final String TAG_BASED_BRANCH_TAG = "QVCS_TAG_BASED_BRANCH_TAG";
    private static final String BRANCH_PARENT_TAG = "QVCS_BRANCH_PARENT";
    private static final String BRANCH_ANCHOR_DATE_TAG = "QVCS_BRANCH_ANCHOR_DATE_TAG";
    private static final String MOVEABLE_TAG_FLAG_TAG = "QVCS_MOVEABLE_TAG_TAG";

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
    public static String getIsTagBasedBranchFlagTag() {
        return IS_TAG_BASED_BRANCH_FLAG_TAG;
    }

    /**
     * Get the is feature branch flag tag.
     * @return the is feature branch flag tag.
     */
    public static String getIsFeatureBranchFlagTag() {
        return IS_FEATURE_BRANCH_FLAG_TAG;
    }

    /**
     * Get the is release branch flag tag.
     *
     * @return the is release branch flag tag.
     */
    public static String getIsReleaseBranchFlagTag() {
        return IS_RELEASE_BRANCH_FLAG_TAG;
    }

    /**
     * Get the date based branch date tag.
     * @return the date based branch date tag.
     */
    public static String getTagBasedBranchTag() {
        return TAG_BASED_BRANCH_TAG;
    }

    /**
     * Get the branch parent tag.
     * @return the branch parent tag.
     */
    public static String getBranchParentTag() {
        return BRANCH_PARENT_TAG;
    }

    public static String getBranchAnchorDateTag() {
        return BRANCH_ANCHOR_DATE_TAG;
    }

    public static String getMoveableTagTag() {
        return MOVEABLE_TAG_FLAG_TAG;
    }

////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////
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
    public boolean getIsTagBasedBranchFlag() {
        return getBooleanValue(getIsTagBasedBranchFlagTag());
    }

    /**
     * Set the is date based branch flag.
     * @param flag the is date based branch flag.
     */
    public void setIsTagBasedBranchFlag(boolean flag) {
        setBooleanValue(getIsTagBasedBranchFlagTag(), flag);
    }

    /**
     * Get the is feature branch flag.
     * @return the is feature branch flag.
     */
    public boolean getIsFeatureBranchFlag() {
        return getBooleanValue(getIsFeatureBranchFlagTag());
    }

    /**
     * Set the is feature branch flag.
     * @param flag the is feature branch flag.
     */
    public void setIsFeatureBranchFlag(boolean flag) {
        setBooleanValue(getIsFeatureBranchFlagTag(), flag);
    }

    /**
     * Get the is release branch flag.
     *
     * @return the is opaque branch flag.
     */
    public boolean getIsReleaseBranchFlag() {
        return getBooleanValue(getIsReleaseBranchFlagTag());
    }

    /**
     * Set the is release branch flag.
     *
     * @param flag the is opaque branch flag.
     */
    public void setIsReleaseBranchFlag(boolean flag) {
        setBooleanValue(getIsReleaseBranchFlagTag(), flag);
    }

    /**
     * Get the date based date.
     * @return the date based date.
     */
    public String getTagBasedTag() {
        return getStringValue(getTagBasedBranchTag());
    }

    /**
     * Set the date based date.
     * @param tag the tag.
     */
    public void setTagBasedTag(String tag) {
        setStringValue(getTagBasedBranchTag(), tag);
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
     * Get the branch anchor date.
     *
     * @return the branch anchor date.
     */
    public Date getBranchAnchorDate() {
        String anchor = getStringValue(getBranchAnchorDateTag());
        Long anchorTime = Long.parseLong(anchor);
        return new Date(anchorTime);
    }

    /**
     * Set the branch anchor date.
     *
     * @param branchAnchorDate the branch parent name.
     */
    public void setBranchAnchorDate(Date branchAnchorDate) {
        Long anchorDate = branchAnchorDate.getTime();
        setStringValue(getBranchAnchorDateTag(), anchorDate.toString());
    }

    /**
     * Get the is moveable tag branch flag.
     *
     * @return the is opaque branch flag.
     */
    public boolean getIsMoveableTagBranchFlag() {
        return getBooleanValue(getMoveableTagTag());
    }

    /**
     * Set the is moveable tag branch flag.
     *
     * @param flag the is opaque branch flag.
     */
    public void setIsMoveableTagBranchFlag(boolean flag) {
        setBooleanValue(getMoveableTagTag(), flag);
    }

}
