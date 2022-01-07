/*
 * Copyright 2021 Jim Voris.
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
package com.qvcsos.server.datamodel;

/**
 *
 * @author Jim Voris
 */
public class DirectoryLocation {
    private Integer directoryLocationId;
    private Integer branchId;
    private Integer directoryId;
    private Integer parentDirectoryLocationId;
    private Integer createdForReason;
    private Integer commitId;
    private String directorySegmentName;
    private Boolean deletedFlag;

    /**
     * @return the id
     */
    public Integer getId() {
        return directoryLocationId;
    }

    /**
     * @param id the id to set
     */
    public void setId(Integer id) {
        this.directoryLocationId = id;
    }

    /**
     * @return the directoryId
     */
    public Integer getDirectoryId() {
        return directoryId;
    }

    /**
     * @param id the directoryId to set
     */
    public void setDirectoryId(Integer id) {
        this.directoryId = id;
    }

    /**
     * @return the parentDirectoryLocationId
     */
    public Integer getParentDirectoryLocationId() {
        return parentDirectoryLocationId;
    }

    /**
     * @param id the parentDirectoryLocationId to set
     */
    public void setParentDirectoryLocationId(Integer id) {
        this.parentDirectoryLocationId = id;
    }

    /**
     * @return the commitId
     */
    public Integer getCommitId() {
        return commitId;
    }

    /**
     * @param id the commitId to set
     */
    public void setCommitId(Integer id) {
        this.commitId = id;
    }

    /**
     * @return the directorySegmentName
     */
    public String getDirectorySegmentName() {
        return directorySegmentName;
    }

    /**
     * @param dirName the directorySegmentName to set
     */
    public void setDirectorySegmentName(String dirName) {
        this.directorySegmentName = dirName;
    }

    /**
     * @return the deletedFlag
     */
    public Boolean getDeletedFlag() {
        return deletedFlag;
    }

    /**
     * @param flag the deletedFlag to set
     */
    public void setDeletedFlag(Boolean flag) {
        this.deletedFlag = flag;
    }

    /**
     * @return the branchId
     */
    public Integer getBranchId() {
        return branchId;
    }

    /**
     * @param id the branchId to set
     */
    public void setBranchId(Integer id) {
        this.branchId = id;
    }

    /**
     * @return the createdForReason
     */
    public Integer getCreatedForReason() {
        return createdForReason;
    }

    /**
     * @param reason the createdForReason to set
     */
    public void setCreatedForReason(Integer reason) {
        this.createdForReason = reason;
    }

}
