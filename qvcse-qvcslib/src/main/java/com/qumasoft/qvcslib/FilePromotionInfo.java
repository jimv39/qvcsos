/*   Copyright 2004-2021 Jim Voris
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

import java.io.Serializable;

/**
 * File promotion info.
 * @author Jim Voris
 */
public class FilePromotionInfo implements Serializable {
    private static final long serialVersionUID = -3483941529189433168L;

    // This is what gets serialized.
    private String promotedFromBranchName;
    private String promotedToBranchName;
    private String promotedFromAppendedPath;
    private String promotedToAppendedPath;
    private String promotedFromShortWorkfileName;
    private String promotedToShortWorkfileName;
    private String childBranchTipRevisionString;
    private Integer fileId;
    private Integer featureBranchRevisionId;
    private Integer promotedFromBranchId;
    private Integer promotedToBranchId;
    private PromotionType typeOfPromotion;
    private Boolean deletedFlag;
    private String describeTypeOfPromotion;

    /**
     * Get the appended path.
     * @return the appended path.
     */
    public String getPromotedFromAppendedPath() {
        return promotedFromAppendedPath;
    }

    /**
     * Set the appended path.
     * @param path the appended path.
     */
    public void setPromotedFromAppendedPath(String path) {
        this.promotedFromAppendedPath = path;
    }

    /**
     * Get the short workfile name.
     * @return the short workfile name.
     */
    public String getPromotedFromShortWorkfileName() {
        return promotedFromShortWorkfileName;
    }

    /**
     * Set the short workfile name.
     * @param shortName the short workfile name.
     */
    public void setPromotedFromShortWorkfileName(String shortName) {
        this.promotedFromShortWorkfileName = shortName;
    }

    /**
     * Get the file id.
     * @return the file id.
     */
    public Integer getFileId() {
        return fileId;
    }

    /**
     * Set the file id.
     * @param id the file id.
     */
    public void setFileId(Integer id) {
        this.fileId = id;
    }

    /**
     * Get the type of merge.
     * @return the type of merge.
     */
    public PromotionType getTypeOfPromotion() {
        return typeOfPromotion;
    }

    /**
     * Set the type of merge.
     * @param typeOfMrg the type of merge.
     */
    public void setTypeOfPromotion(PromotionType typeOfMrg) {
        this.typeOfPromotion = typeOfMrg;
    }

    /**
     * Get the description of the type of merge.
     * @return the description of the type of merge.
     */
    public String getDescribeTypeOfPromotion() {
        return describeTypeOfPromotion;
    }

    /**
     * Set the description of the type of merge.
     * @param describeTypeOfPromote the description of the type of merge.
     */
    public void setDescribeTypeOfPromotion(String describeTypeOfPromote) {
        this.describeTypeOfPromotion = describeTypeOfPromote;
    }

    /**
     * Get the deleted flag.
     * @return the deleted flag.
     */
    public Boolean getDeletedFlag() {
        return deletedFlag;
    }

    /**
     * Set the deleted flag.
     * @param flag the deleted flag.
     */
    public void setDeletedFlag(Boolean flag) {
        this.deletedFlag = flag;
    }

    /**
     * Get the file branch id.
     * @return the file branch id.
     */
    public Integer getPromotedFromBranchId() {
        return promotedFromBranchId;
    }

    /**
     * Set the file branch id.
     * @param fbId the file branch id.
     */
    public void setPromotedFromBranchId(Integer fbId) {
        this.promotedFromBranchId = fbId;
    }

    /**
     * Get the child branch tip revision string.
     * @return the child branch tip revision string.
     */
    public String getChildBranchTipRevisionString() {
        return childBranchTipRevisionString;
    }

    /**
     * Set the child branch tip revision string.
     * @param childBranchTipRevString the child branch tip revision string.
     */
    public void setChildBranchTipRevisionString(String childBranchTipRevString) {
        this.childBranchTipRevisionString = childBranchTipRevString;
    }

    /**
     * @return the featureBranchRevisionId
     */
    public Integer getFeatureBranchRevisionId() {
        return featureBranchRevisionId;
    }

    /**
     * @param revisionId the featureBranchRevisionId to set
     */
    public void setFeatureBranchRevisionId(Integer revisionId) {
        this.featureBranchRevisionId = revisionId;
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
     * @return the promotedToShortWorkfileName
     */
    public String getPromotedToShortWorkfileName() {
        return promotedToShortWorkfileName;
    }

    /**
     * @param fileName the promotedToShortWorkfileName to set
     */
    public void setPromotedToShortWorkfileName(String fileName) {
        this.promotedToShortWorkfileName = fileName;
    }

    /**
     * @return the promotedToBranchName
     */
    public String getPromotedToBranchName() {
        return promotedToBranchName;
    }

    /**
     * @param branchName the promotedToBranchName to set
     */
    public void setPromotedToBranchName(String branchName) {
        this.promotedToBranchName = branchName;
    }

    /**
     * @return the promotedToAppendedPath
     */
    public String getPromotedToAppendedPath() {
        return promotedToAppendedPath;
    }

    /**
     * @param appendedPath the promotedToAppendedPath to set
     */
    public void setPromotedToAppendedPath(String appendedPath) {
        this.promotedToAppendedPath = appendedPath;
    }

    /**
     * @return the promotedToBranchId
     */
    public Integer getPromotedToBranchId() {
        return promotedToBranchId;
    }

    /**
     * @param branchId the promotedToBranchId to set
     */
    public void setPromotedToBranchId(Integer branchId) {
        this.promotedToBranchId = branchId;
    }
}
