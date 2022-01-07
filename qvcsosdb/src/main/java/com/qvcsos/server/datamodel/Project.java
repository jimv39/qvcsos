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
public class Project {
    private Integer projectId;
    private Integer commitId;
    private String projectName;
    private Boolean deletedFlag;

    /**
     * @return the id
     */
    public Integer getId() {
        return projectId;
    }

    /**
     * @param id the id to set
     */
    public void setId(Integer id) {
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
     * @return the projectName
     */
    public String getProjectName() {
        return projectName;
    }

    /**
     * @param name the projectName to set
     */
    public void setProjectName(String name) {
        this.projectName = name;
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


}
