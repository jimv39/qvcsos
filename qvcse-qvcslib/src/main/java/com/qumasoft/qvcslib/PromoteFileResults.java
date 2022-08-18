/*   Copyright 2004-2022 Jim Voris
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

import com.qumasoft.qvcslib.response.AbstractServerResponsePromoteFile;

/**
 * Helper class that encapsulates the results of a promote file response.
 * @author Jim Voris
 */
public class PromoteFileResults {

    private final String projectName;
    private final String promotedToBranchName;
    private final String promotedToAppendedPath;
    private final String promotedToShortWorkfileName;
    private final String promotedFromBranchName;
    private final String promotedFromAppendedPath;
    private final String promotedFromShortWorkfileName;
    private PromotionType promotionType;
    private final SkinnyLogfileInfo promotedFromSkinnyLogfileInfo;
    private final SkinnyLogfileInfo promotedToSkinnyLogfileInfo;
    private final byte[] mergedResultBuffer;
    private final byte[] commonAncestorBuffer;
    private final byte[] branchParentTipRevisionBuffer;
    private final byte[] branchTipRevisionBuffer;
    private final LogfileInfo logfileInfo;
    private final Integer commonAncestorRevisionId;
    private final Integer featureBranchTipRevisionId;
    private final Integer parentBranchTipRevisionId;

    public PromoteFileResults(AbstractServerResponsePromoteFile serverResponsePromoteFile) {
        this.projectName = serverResponsePromoteFile.getProjectName();
        this.promotedToBranchName = serverResponsePromoteFile.getPromotedToBranchName();
        this.promotedToAppendedPath = serverResponsePromoteFile.getPromotedToAppendedPath();
        this.promotedToShortWorkfileName = serverResponsePromoteFile.getPromotedToShortWorkfileName();
        this.promotedFromBranchName = serverResponsePromoteFile.getPromotedFromBranchName();
        this.promotedFromAppendedPath = serverResponsePromoteFile.getPromotedFromAppendedPath();
        this.promotedFromShortWorkfileName = serverResponsePromoteFile.getPromotedFromShortWorkfileName();
        this.promotionType = serverResponsePromoteFile.getPromotionType();
        this.promotedFromSkinnyLogfileInfo = serverResponsePromoteFile.getPromotedFromSkinnyLogfileInfo();
        this.promotedToSkinnyLogfileInfo = serverResponsePromoteFile.getPromotedToSkinnyLogfileInfo();
        this.mergedResultBuffer = serverResponsePromoteFile.getMergedResultBuffer();
        this.commonAncestorBuffer = serverResponsePromoteFile.getCommonAncestorBuffer();
        this.branchParentTipRevisionBuffer = serverResponsePromoteFile.getBranchParentTipRevisionBuffer();
        this.branchTipRevisionBuffer = serverResponsePromoteFile.getBranchTipRevisionBuffer();
        this.logfileInfo = serverResponsePromoteFile.getLogfileInfo();
        this.commonAncestorRevisionId = serverResponsePromoteFile.getCommonAncestorRevisionId();
        this.featureBranchTipRevisionId = serverResponsePromoteFile.getFeatureBranchTipRevisionId();
        this.parentBranchTipRevisionId = serverResponsePromoteFile.getParentBranchTipRevisionId();
    }

    /**
     * Get the project name.
     * @return the project name.
     */
    public String getProjectName() {
        return projectName;
    }

    /**
     * Get the branch name.
     * @return the branch name.
     */
    public String getPromotedToBranchName() {
        return promotedToBranchName;
    }

    /**
     * Get the appended path.
     * @return the appended path.
     */
    public String getPromotedToAppendedPath() {
        return promotedToAppendedPath;
    }

    /**
     * Get the short workfile name.
     * @return the short workfile name.
     */
    public String getPromotedToShortWorkfileName() {
        return promotedToShortWorkfileName;
    }

    /**
     * @return the promotedFromBranchName
     */
    public String getPromotedFromBranchName() {
        return promotedFromBranchName;
    }

    /**
     * @return the promotedFromAppendedPath
     */
    public String getPromotedFromAppendedPath() {
        return promotedFromAppendedPath;
    }

    /**
     * @return the promotedFromShortWorkfileName
     */
    public String getPromotedFromShortWorkfileName() {
        return promotedFromShortWorkfileName;
    }

    /**
     * Get the promoted from skinny logfile info. This is the feature-branch skinnyLogfileInfo.
     * @return the promoted-from skinny logfile info.
     */
    public SkinnyLogfileInfo getPromotedFromSkinnyLogfileInfo() {
        return promotedFromSkinnyLogfileInfo;
    }

    /**
     * Get the promoted to skinny logfile info. This is the parent-branch skinnyLogfileInfo.
     * @return the promoted-to skinny logfile info.
     */
    public SkinnyLogfileInfo getPromotedToSkinnyLogfileInfo() {
        return promotedToSkinnyLogfileInfo;
    }

    /**
     * Get the merged result buffer.
     * @return the merged result buffer.
     */
    public byte[] getMergedResultBuffer() {
        return mergedResultBuffer;
    }

    /**
     * Get the common ancestor buffer.
     * @return the common ancestor buffer.
     */
    public byte[] getCommonAncestorBuffer() {
        return commonAncestorBuffer;
    }

    /**
     * Get the branch parent tip revision buffer.
     * @return the branch parent tip revision buffer.
     */
    public byte[] getBranchParentTipRevisionBuffer() {
        return branchParentTipRevisionBuffer;
    }

    /**
     * Get the branch tip revision buffer.
     * @return the branch tip revision buffer.
     */
    public byte[] getBranchTipRevisionBuffer() {
        return branchTipRevisionBuffer;
    }

    /**
     * Get the logfile info.
     * @return the logfile info.
     */
    public LogfileInfo getLogfileInfo() {
        return logfileInfo;
    }

    /**
     * Get the merge type.
     * @return the merge type.
     */
    public PromotionType getPromotionType() {
        return promotionType;
    }

    /**
     * Set the merge type.
     * @param type the merge type.
     */
    public void setPromotionType(PromotionType type) {
        this.promotionType = type;
    }

    /**
     * @return the commonAncestorRevisionId
     */
    public Integer getCommonAncestorRevisionId() {
        return commonAncestorRevisionId;
    }

    /**
     * @return the featureBranchTipRevisionId
     */
    public Integer getFeatureBranchTipRevisionId() {
        return featureBranchTipRevisionId;
    }

    /**
     * @return the parentBranchTipRevisionId
     */
    public Integer getParentBranchTipRevisionId() {
        return parentBranchTipRevisionId;
    }
}
