/*
 * Copyright 2021-2022 Jim Voris.
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
public class FileName {
    private Integer fileNameId;
    private Integer branchId;
    private Integer directoryId;
    private Integer fileId;
    private Integer createdForReason;
    private Integer commitId;
    private String fileName;
    private Boolean promotedFlag;
    private Boolean deletedFlag;
    private Integer promotionCommitId;

    /**
     * @return the id
     */
    public Integer getId() {
        return fileNameId;
    }

    /**
     * @param id the id to set
     */
    public void setId(Integer id) {
        this.fileNameId = id;
    }

    /**
     * @return the fileId
     */
    public Integer getFileId() {
        return fileId;
    }

    /**
     * @param id the fileId to set
     */
    public void setFileId(Integer id) {
        this.fileId = id;
    }

    /**
     * @return the directoryId
     */
    public Integer getDirectoryId() {
        return directoryId;
    }

    /**
     * @param dirId the directoryId to set
     */
    public void setDirectoryId(Integer dirId) {
        this.directoryId = dirId;
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
     * @return the fileName
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * @param filename the fileName to set
     */
    public void setFileName(String filename) {
        this.fileName = filename;
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
     * @return the promotedFlag
     */
    public Boolean getPromotedFlag() {
        return promotedFlag;
    }

    /**
     * @param flag the promotedFlag to set
     */
    public void setPromotedFlag(Boolean flag) {
        this.promotedFlag = flag;
    }

    /**
     * @param reason the createdForReason to set
     */
    public void setCreatedForReason(Integer reason) {
        this.createdForReason = reason;
    }

    /**
     * @return the promotionCommitId
     */
    public Integer getPromotionCommitId() {
        return promotionCommitId;
    }

    /**
     * @param id the promotionCommitId to set
     */
    public void setPromotionCommitId(Integer id) {
        this.promotionCommitId = id;
    }

}
