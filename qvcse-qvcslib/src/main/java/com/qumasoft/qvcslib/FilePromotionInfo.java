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
package com.qumasoft.qvcslib;

import java.io.Serializable;

/**
 * File promotion info.
 * @author Jim Voris
 */
public class FilePromotionInfo implements Serializable {
    private static final long serialVersionUID = -3483941529189433168L;

    // This is what gets serialized.
    private String appendedPath;
    private String shortWorkfileName;
    private String childBranchTipRevisionString;
    private Integer fileId;
    private Integer fileBranchId;
    private MergeType typeOfMerge;
    private Boolean deletedFlag;
    private String describeTypeOfMerge;

    /**
     * Get the appended path.
     * @return the appended path.
     */
    public String getAppendedPath() {
        return appendedPath;
    }

    /**
     * Set the appended path.
     * @param path the appended path.
     */
    public void setAppendedPath(String path) {
        this.appendedPath = path;
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
    public MergeType getTypeOfMerge() {
        return typeOfMerge;
    }

    /**
     * Set the type of merge.
     * @param typeOfMrg the type of merge.
     */
    public void setTypeOfMerge(MergeType typeOfMrg) {
        this.typeOfMerge = typeOfMrg;
    }

    /**
     * Get the description of the type of merge.
     * @return the description of the type of merge.
     */
    public String getDescribeTypeOfMerge() {
        return describeTypeOfMerge;
    }

    /**
     * Set the description of the type of merge.
     * @param describeTypeOfMrg the description of the type of merge.
     */
    public void setDescribeTypeOfMerge(String describeTypeOfMrg) {
        this.describeTypeOfMerge = describeTypeOfMrg;
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
    public Integer getFileBranchId() {
        return fileBranchId;
    }

    /**
     * Set the file branch id.
     * @param fbId the file branch id.
     */
    public void setFileBranchId(Integer fbId) {
        this.fileBranchId = fbId;
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
}
