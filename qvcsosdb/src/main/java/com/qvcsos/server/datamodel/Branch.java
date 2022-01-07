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
 * Branch db model class.
 * @author Jim Voris
 */
public class Branch {

    private Integer branchId;
    private Integer parentBranchId;
    private Integer projectId;
    private Integer rootDirectoryId;
    private Integer commitId;
    private String branchName;
    private Integer branchTypeId;
    private Integer tagId;
    private Boolean deletedFlag;

    /**
     * @return the id
     */
    public Integer getId() {
        return branchId;
    }

    /**
     * @param id the id to set
     */
    public void setId(Integer id) {
        this.branchId = id;
    }

    /**
     * @return the parentBranchId
     */
    public Integer getParentBranchId() {
        return parentBranchId;
    }

    /**
     * @param id the parentBranchId to set
     */
    public void setParentBranchId(Integer id) {
        this.parentBranchId = id;
    }

    /**
     * @return the projectId
     */
    public Integer getProjectId() {
        return projectId;
    }

    /**
     * @param id the projectId to set
     */
    public void setProjectId(Integer id) {
        this.projectId = id;
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
     * @return the branchName
     */
    public String getBranchName() {
        return branchName;
    }

    /**
     * @param name the branchName to set
     */
    public void setBranchName(String name) {
        this.branchName = name;
    }

    /**
     * @return the branchTypeId
     */
    public Integer getBranchTypeId() {
        return branchTypeId;
    }

    /**
     * @param id the branchTypeId to set
     */
    public void setBranchTypeId(Integer id) {
        this.branchTypeId = id;
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
     * @return the rootDirectoryId
     */
    public Integer getRootDirectoryId() {
        return rootDirectoryId;
    }

    /**
     * @param id the rootDirectoryId to set
     */
    public void setRootDirectoryId(Integer id) {
        this.rootDirectoryId = id;
    }

    /**
     * @return the tagId
     */
    public Integer getTagId() {
        return tagId;
    }

    /**
     * @param id the tagId to set
     */
    public void setTagId(Integer id) {
        this.tagId = id;
    }

}
