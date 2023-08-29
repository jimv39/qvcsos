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
package com.qumasoft.qvcslib;

import java.util.List;

/**
 *
 * @author Jim Voris
 */
public class CommonFilterFileCollection implements java.io.Serializable {
    private Integer id;
    private Integer userId;
    private Boolean builtInFlag;
    private Integer associatedProjectId;
    private String associatedProjectName;
    private String collectionName;
    private List<CommonFilterFile> filterFileList;

    /**
     * @return the id
     */
    public Integer getId() {
        return id;
    }

    /**
     * @param fcId the id to set
     */
    public void setId(Integer fcId) {
        this.id = fcId;
    }

    /**
     * @return the userId
     */
    public Integer getUserId() {
        return userId;
    }

    /**
     * @param uId the userId to set
     */
    public void setUserId(Integer uId) {
        this.userId = uId;
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
     * @param apId the associatedProjectId to set
     */
    public void setAssociatedProjectId(Integer apId) {
        this.associatedProjectId = apId;
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

    /**
     * @return the filterFileList
     */
    public List<CommonFilterFile> getFilterFileList() {
        return filterFileList;
    }

    /**
     * @param filterList the filterFileList to set
     */
    public void setFilterFileList(List<CommonFilterFile> filterList) {
        this.filterFileList = filterList;
    }

    /**
     * @return the associatedProjectName
     */
    public String getAssociatedProjectName() {
        return associatedProjectName;
    }

    /**
     * @param projectName the associatedProjectName to set
     */
    public void setAssociatedProjectName(String projectName) {
        this.associatedProjectName = projectName;
    }

}
