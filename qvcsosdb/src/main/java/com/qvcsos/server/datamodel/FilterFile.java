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
public class FilterFile {
    private Integer id;
    private Integer filterCollectionId;
    private Integer filterTypeId;
    private Boolean isAndFlag;
    private String filterData;

    /**
     * @return the id
     */
    public Integer getId() {
        return id;
    }

    /**
     * @param filterFileId the id to set
     */
    public void setId(Integer filterFileId) {
        this.id = filterFileId;
    }

    /**
     * @return the filterCollectionId
     */
    public Integer getFilterCollectionId() {
        return filterCollectionId;
    }

    /**
     * @param fcId the filterCollectionId to set
     */
    public void setFilterCollectionId(Integer fcId) {
        this.filterCollectionId = fcId;
    }

    /**
     * @return the filterTypeId
     */
    public Integer getFilterTypeId() {
        return filterTypeId;
    }

    /**
     * @param ftId the filterTypeId to set
     */
    public void setFilterTypeId(Integer ftId) {
        this.filterTypeId = ftId;
    }

    /**
     * @return the isAndFlag
     */
    public Boolean getIsAndFlag() {
        return isAndFlag;
    }

    /**
     * @param flag the isAndFlag to set
     */
    public void setIsAndFlag(Boolean flag) {
        this.isAndFlag = flag;
    }

    /**
     * @return the filterData
     */
    public String getFilterData() {
        return filterData;
    }

    /**
     * @param fData the filterData to set
     */
    public void setFilterData(String fData) {
        this.filterData = fData;
    }
}
