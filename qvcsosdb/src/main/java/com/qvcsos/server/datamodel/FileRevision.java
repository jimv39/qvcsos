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

import java.sql.Timestamp;

/**
 *
 * @author Jim Voris
 */
public class FileRevision {
    private Integer fileRevisionId;
    private Integer branchId;
    private Integer fileId;
    private Integer ancestorRevisionId;
    private Integer reverseDeltaRevisionId;
    private Integer commitId;
    private Timestamp workfileEditDate;
    private byte[] revisionDigest;
    private byte[] revisionData;
    private Boolean promotedFlag;
    private Integer revisionDataSize;

    /**
     * @return the id
     */
    public Integer getId() {
        return fileRevisionId;
    }

    /**
     * @param id the id to set
     */
    public void setId(Integer id) {
        this.fileRevisionId = id;
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
     * @return the revisionDigest
     */
    public byte[] getRevisionDigest() {
        return revisionDigest;
    }

    /**
     * @param digest the revisionDigest to set
     */
    public void setRevisionDigest(byte[] digest) {
        this.revisionDigest = digest;
    }

    /**
     * @return the ancestorRevisionId
     */
    public Integer getAncestorRevisionId() {
        return ancestorRevisionId;
    }

    /**
     * @param id the ancestorRevisionId to set
     */
    public void setAncestorRevisionId(Integer id) {
        this.ancestorRevisionId = id;
    }

    /**
     * @return the reverseDeltaRevisionId
     */
    public Integer getReverseDeltaRevisionId() {
        return reverseDeltaRevisionId;
    }

    /**
     * @param id the reverseDeltaRevisionId to set
     */
    public void setReverseDeltaRevisionId(Integer id) {
        this.reverseDeltaRevisionId = id;
    }

    /**
     * @return the revisionData
     */
    public byte[] getRevisionData() {
        return revisionData;
    }

    /**
     * @param data the revisionData to set
     */
    public void setRevisionData(byte[] data) {
        this.revisionData = data;
    }

    /**
     * @return the revisionDataSize
     */
    public Integer getRevisionDataSize() {
        return revisionDataSize;
    }

    /**
     * @param size the revisionDataSize to set
     */
    public void setRevisionDataSize(Integer size) {
        this.revisionDataSize = size;
    }

    /**
     * @return the workfileEditDate
     */
    public Timestamp getWorkfileEditDate() {
        return workfileEditDate;
    }

    /**
     * @param editDate the workfileEditDate to set
     */
    public void setWorkfileEditDate(Timestamp editDate) {
        this.workfileEditDate = editDate;
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

}
