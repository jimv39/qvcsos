/*
 * Copyright 2022 Jim Voris.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qumasoft.qvcslib.response;

import com.qumasoft.qvcslib.LogfileInfo;
import com.qumasoft.qvcslib.PromotionType;
import com.qumasoft.qvcslib.SkinnyLogfileInfo;

/**
 *
 * @author Jim Voris
 */
public abstract class AbstractServerResponsePromoteFile extends AbstractServerResponse {
    private String projectName = null;
    private String promotedToBranchName = null;
    private String promotedToAppendedPath = null;
    private String promotedToShortWorkfileName = null;
    private String promotedFromBranchName = null;
    private String promotedFromAppendedPath = null;
    private String promotedFromShortWorkfileName = null;
    private String mergedInfoSyncBranchName = null;
    private String mergedInfoSyncAppendedPath = null;
    private String mergedInfoSyncShortWorkfileName = null;
    private Integer commonAncestorRevisionId = null;
    private Integer featureBranchTipRevisionId = null;
    private Integer parentBranchTipRevisionId = null;
    private PromotionType promotionType = null;
    // Send back the respective skinny logfile infos...
    private SkinnyLogfileInfo promotedFromSkinnyLogfileInfo = null;
    private SkinnyLogfileInfo promotedToSkinnyLogfileInfo = null;
    /**
     * The merged result buffer (optional)
     */
    private byte[] mergedResultBuffer = null;
    /**
     * The common ancestor buffer (optional)
     */
    private byte[] commonAncestorBuffer = null;
    /**
     * The branch parent tip revision (optional)
     */
    private byte[] branchParentTipRevisionBuffer = null;
    /**
     * The branch tip revision (optional)
     */
    private byte[] branchTipRevisionBuffer = null;
    /**
     * Optionally sent back if needed.
     */
    private LogfileInfo logfileInfo = null;

    /**
     * @return the promotedToShortWorkfileName
     */
    public String getPromotedToShortWorkfileName() {
        return promotedToShortWorkfileName;
    }

    /**
     * Set the short workfile name.
     * @param shortName the promotedToShortWorkfileName to set
     */
    public void setPromotedToShortWorkfileName(String shortName) {
        this.promotedToShortWorkfileName = shortName;
    }

    /**
     * Get the appended path.
     * @return the appended path.
     */
    public String getPromotedToAppendedPath() {
        return promotedToAppendedPath;
    }

    /**
     * Set the appended path.
     * @param path the appended path.
     */
    public void setPromotedToAppendedPath(String path) {
        this.promotedToAppendedPath = path;
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
    public void setProjectName(String project) {
        this.projectName = project;
    }

    /**
     * Get the parent branch name.
     * @return the branch name.
     */
    public String getPromotedToBranchName() {
        return promotedToBranchName;
    }

    /**
     * Set the parent branch name.
     * @param branch the parent branch name.
     */
    public void setPromotedToBranchName(String branch) {
        this.promotedToBranchName = branch;
    }

    /**
     * Get the skinny logfile info.
     * @return the skinny logfile info.
     */
    public SkinnyLogfileInfo getPromotedFromSkinnyLogfileInfo() {
        return promotedFromSkinnyLogfileInfo;
    }

    /**
     * Set the skinny logfile info.
     * @param skinnyInfo the skinny logfile info.
     */
    public void setPromotedFromSkinnyLogfileInfo(SkinnyLogfileInfo skinnyInfo) {
        this.promotedFromSkinnyLogfileInfo = skinnyInfo;
    }

    /**
     * @return the promotedToSkinnyLogfileInfo
     */
    public SkinnyLogfileInfo getPromotedToSkinnyLogfileInfo() {
        return promotedToSkinnyLogfileInfo;
    }

    /**
     * @param skinnyInfo the promotedToSkinnyLogfileInfo to set
     */
    public void setPromotedToSkinnyLogfileInfo(SkinnyLogfileInfo skinnyInfo) {
        this.promotedToSkinnyLogfileInfo = skinnyInfo;
    }

    /**
     * Get the merged result buffer.
     * @return the merged result buffer.
     */
    public byte[] getMergedResultBuffer() {
        return mergedResultBuffer;
    }

    /**
     * Set the merged result buffer.
     * @param mergedBuffer the merged result buffer.
     */
    public void setMergedResultBuffer(byte[] mergedBuffer) {
        this.mergedResultBuffer = mergedBuffer;
    }

    /**
     * Get the common ancestor buffer.
     * @return the common ancestor buffer.
     */
    public byte[] getCommonAncestorBuffer() {
        return commonAncestorBuffer;
    }

    /**
     * Set the common ancestor buffer.
     * @param ancestorBuffer the common ancestor buffer.
     */
    public void setCommonAncestorBuffer(byte[] ancestorBuffer) {
        this.commonAncestorBuffer = ancestorBuffer;
    }

    /**
     * Get the branch parent tip revision buffer.
     * @return the branch parent tip revision buffer.
     */
    public byte[] getBranchParentTipRevisionBuffer() {
        return branchParentTipRevisionBuffer;
    }

    /**
     * Set the branch parent tip revision buffer.
     * @param branchParentTipBuffer the branch parent tip revision buffer.
     */
    public void setBranchParentTipRevisionBuffer(byte[] branchParentTipBuffer) {
        this.branchParentTipRevisionBuffer = branchParentTipBuffer;
    }

    /**
     * Get the branch tip revision buffer.
     * @return the branch tip revision buffer.
     */
    public byte[] getBranchTipRevisionBuffer() {
        return branchTipRevisionBuffer;
    }

    /**
     * Set the branch tip revision buffer.
     * @param branchTipBuffer the branch tip revision buffer.
     */
    public void setBranchTipRevisionBuffer(byte[] branchTipBuffer) {
        this.branchTipRevisionBuffer = branchTipBuffer;
    }

    /**
     * Get the logfile info.
     * @return the logfile info.
     */
    public LogfileInfo getLogfileInfo() {
        return logfileInfo;
    }

    /**
     * Set the logfile info.
     * @param fileInfo the logfile info.
     */
    public void setLogfileInfo(LogfileInfo fileInfo) {
        this.logfileInfo = fileInfo;
    }

    /**
     * Get the promotion type.
     * @return the promotion type.
     */
    public PromotionType getPromotionType() {
        return promotionType;
    }

    /**
     * Set the promotion type.
     * @param type the promotion type.
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
     * @param id the commonAncestorRevisionId to set
     */
    public void setCommonAncestorRevisionId(Integer id) {
        this.commonAncestorRevisionId = id;
    }

    /**
     * @return the featureBranchTipRevisionId
     */
    public Integer getFeatureBranchTipRevisionId() {
        return featureBranchTipRevisionId;
    }

    /**
     * @param id the featureBranchTipRevisionId to set
     */
    public void setFeatureBranchTipRevisionId(Integer id) {
        this.featureBranchTipRevisionId = id;
    }

    /**
     * @return the parentBranchTipRevisionId
     */
    public Integer getParentBranchTipRevisionId() {
        return parentBranchTipRevisionId;
    }

    /**
     * @param id the parentBranchTipRevisionId to set
     */
    public void setParentBranchTipRevisionId(Integer id) {
        this.parentBranchTipRevisionId = id;
    }

    /**
     * @return the mergedInfoSyncBranchName
     */
    public String getMergedInfoSyncBranchName() {
        return mergedInfoSyncBranchName;
    }

    /**
     * @param branchName the mergedInfoSyncBranchName to set
     */
    public void setMergedInfoSyncBranchName(String branchName) {
        this.mergedInfoSyncBranchName = branchName;
    }

    /**
     * @return the mergedInfoSyncAppendedPath
     */
    public String getMergedInfoSyncAppendedPath() {
        return mergedInfoSyncAppendedPath;
    }

    /**
     * @param appendedPath the mergedInfoSyncAppendedPath to set
     */
    public void setMergedInfoSyncAppendedPath(String appendedPath) {
        this.mergedInfoSyncAppendedPath = appendedPath;
    }

    /**
     * @return the mergedInfoSyncShortWorkfileName
     */
    public String getMergedInfoSyncShortWorkfileName() {
        return mergedInfoSyncShortWorkfileName;
    }

    /**
     * @param shortWorkfileName the     * mergedInfoSyncShortWorkfileName to set
     */
    public void setMergedInfoSyncShortWorkfileName(String shortWorkfileName) {
        this.mergedInfoSyncShortWorkfileName = shortWorkfileName;
    }

    /**
     * @return the promotedFromBranchName
     */
    public String getPromotedFromBranchName() {
        return promotedFromBranchName;
    }

    /**
     * @param branchName the promotedFromBranchName to set
     */
    public void setPromotedFromBranchName(String branchName) {
        this.promotedFromBranchName = branchName;
    }

    /**
     * @return the promotedFromAppendedPath
     */
    public String getPromotedFromAppendedPath() {
        return promotedFromAppendedPath;
    }

    /**
     * @param appendedPath the promotedFromAppendedPath to set
     */
    public void setPromotedFromAppendedPath(String appendedPath) {
        this.promotedFromAppendedPath = appendedPath;
    }

    /**
     * @return the promotedFromShortWorkfileName
     */
    public String getPromotedFromShortWorkfileName() {
        return promotedFromShortWorkfileName;
    }

    /**
     * @param shortWorkfileName the promotedFromShortWorkfileName to set
     */
    public void setPromotedFromShortWorkfileName(String shortWorkfileName) {
        this.promotedFromShortWorkfileName = shortWorkfileName;
    }

}
