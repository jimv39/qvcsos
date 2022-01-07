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
 * Branch type db model class.
 * @author Jim Voris
 */
public class BranchType {

    private Integer branchTypeId;
    private String branchTypeName;

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
     * @return the branchTypeName
     */
    public String getBranchTypeName() {
        return branchTypeName;
    }

    /**
     * @param name the branchTypeName to set
     */
    public void setBranchTypeName(String name) {
        this.branchTypeName = name;
    }

}
