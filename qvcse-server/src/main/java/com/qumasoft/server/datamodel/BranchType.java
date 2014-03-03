//   Copyright 2004-2014 Jim Voris
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.
//
package com.qumasoft.server.datamodel;

/**
 * Branch type db model class.
 * @author Jim Voris
 */
public class BranchType {
    /*
     * The SQL snippet used to create the BranchType table:
     * BRANCH_TYPE_ID INT NOT NULL CONSTRAINT BRANCH_TYPE_PK PRIMARY KEY, BRANCH_TYPE_NAME VARCHAR(256) NOT NULL);
     */

    private Integer branchTypeId;
    private String branchTypeName;

    /**
     * Get the branch type id.
     * @return the branch type id.
     */
    public Integer getBranchTypeId() {
        return branchTypeId;
    }

    /**
     * Set the branch type id.
     * @param bTypeId the branch type id.
     */
    public void setBranchTypeId(Integer bTypeId) {
        this.branchTypeId = bTypeId;
    }

    /**
     * Get the branch type name.
     * @return the branch type name.
     */
    public String getBranchTypeName() {
        return branchTypeName;
    }

    /**
     * Set the branch type name.
     * @param bTypeName the branch type name.
     */
    public void setBranchTypeName(String bTypeName) {
        this.branchTypeName = bTypeName;
    }
}
