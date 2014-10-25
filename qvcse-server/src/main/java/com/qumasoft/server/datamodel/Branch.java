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

import java.util.Date;

/**
 * Branch db model class.
 * @author Jim Voris
 */
public class Branch {
    /*
     * The SQL snippet used to create the Branch table:
     * BRANCH_ID INT GENERATED ALWAYS AS IDENTITY CONSTRAINT BRANCH_PK PRIMARY KEY, PROJECT_ID INT NOT NULL, "BRANCH_NAME VARCHAR(256) NOT NULL, BRANCH_TYPE_ID INT NOT NULL,
     * INSERT_DATE TIMESTAMP NOT NULL;
     */

    private Integer branchId;
    private Integer projectId;
    private String branchName;
    private Integer branchTypeId;
    private Date insertDate;

    /**
     * Get the branch id.
     * @return the branch id.
     */
    public Integer getBranchId() {
        return branchId;
    }

    /**
     * Set the branch id.
     * @param bId the branch id.
     */
    public void setBranchId(Integer bId) {
        this.branchId = bId;
    }

    /**
     * Get the project id.
     * @return the project id.
     */
    public Integer getProjectId() {
        return projectId;
    }

    /**
     * Set the project id.
     * @param pId the project id.
     */
    public void setProjectId(Integer pId) {
        this.projectId = pId;
    }

    /**
     * Get the branch name.
     * @return the branch name.
     */
    public String getBranchName() {
        return branchName;
    }

    /**
     * Set the branch name.
     * @param bName the branch name.
     */
    public void setBranchName(String bName) {
        this.branchName = bName;
    }

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
     * Get the insert date.
     * @return the insert date
     */
    public Date getInsertDate() {
        Date returnValue = null;
        if (insertDate != null) {
            returnValue = new Date(insertDate.getTime());
        }
        return returnValue;
    }

    /**
     * Set the insert date.
     * @param iDate the insert date
     */
    public void setInsertDate(Date iDate) {
        this.insertDate = new Date(iDate.getTime());
    }
}
