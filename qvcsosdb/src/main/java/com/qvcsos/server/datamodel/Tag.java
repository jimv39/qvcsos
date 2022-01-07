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
public class Tag {
    private Integer tagId;
    private Integer commitId;
    private Integer branchId;
    private Boolean moveableFlag = Boolean.FALSE;
    private String tagText;
    private String description;

    /**
     * @return the tagId
     */
    public Integer getId() {
        return tagId;
    }

    /**
     * @param id the tagId to set
     */
    public void setId(Integer id) {
        this.tagId = id;
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
     * @return the tagText
     */
    public String getTagText() {
        return tagText;
    }

    /**
     * @param text the tagText to set
     */
    public void setTagText(String text) {
        this.tagText = text;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param text the description to set
     */
    public void setDescription(String text) {
        this.description = text;
    }

    /**
     * @return the moveableFlag
     */
    public Boolean getMoveableFlag() {
        return moveableFlag;
    }

    /**
     * @param flag the moveableFlag to set
     */
    public void setMoveableFlag(Boolean flag) {
        this.moveableFlag = flag;
    }

}
