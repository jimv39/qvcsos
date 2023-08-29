/*
 * Copyright 2023 Jim Voris.
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
 * @author Jim Voris.
 */
public class FilterFileCollection {
    private Integer id;
    private Integer userId;
    private Boolean builtInFlag;
    private Integer associatedProjectId;
    private String collectionName;

    /**
     * @return the id
     */
    public Integer getId() {
        return id;
    }

    /**
     * @param collectionId the id to set
     */
    public void setId(Integer collectionId) {
        this.id = collectionId;
    }

    /**
     * @return the userId
     */
    public Integer getUserId() {
        return userId;
    }

    /**
     * @param usrId the userId to set
     */
    public void setUserId(Integer usrId) {
        this.userId = usrId;
    }

    /**
     * @return the builtInFlag
     */
    public Boolean getBuiltInFlag() {
        return builtInFlag;
    }

    /**
     * @param flag the builtInFlag to set
     */
    public void setBuiltInFlag(Boolean flag) {
        this.builtInFlag = flag;
    }

    /**
     * @return the associatedProjectId
     */
    public Integer getAssociatedProjectId() {
        return associatedProjectId;
    }

    /**
     * @param projectId the associatedProjectId to set
     */
    public void setAssociatedProjectId(Integer projectId) {
        this.associatedProjectId = projectId;
    }

    /**
     * @return the collectionName
     */
    public String getCollectionName() {
        return collectionName;
    }

    /**
     * @param cName the collectionName to set
     */
    public void setCollectionName(String cName) {
        this.collectionName = cName;
    }
}
