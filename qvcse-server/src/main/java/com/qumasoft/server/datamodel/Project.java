/*   Copyright 2004-2014 Jim Voris
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.qumasoft.server.datamodel;

import java.util.Date;

/**
 * Project data model class.
 *
 * @author Jim Voris
 */
public class Project {
    /*
     * The SQL snippet used to create the Project table:
     * PROJECT_ID INT GENERATED ALWAYS AS IDENTITY CONSTRAINT PROJECT_PK PRIMARY KEY, PROJECT_NAME VARCHAR(256) NOT NULL,
     * INSERT_DATE TIMESTAMP NOT NULL);
     */

    private Integer projectId;
    private String projectName;
    private Date insertDate;

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
     * Get the project name.
     * @return the project name.
     */
    public String getProjectName() {
        return projectName;
    }

    /**
     * Set the project name.
     * @param pName the project name.
     */
    public void setProjectName(String pName) {
        this.projectName = pName;
    }

    /**
     * Get the insert date.
     * @return the insert date.
     */
    public Date getInsertDate() {
        Date returnValue = null;
        if (this.insertDate != null) {
            returnValue = new Date(insertDate.getTime());
        }
        return returnValue;
    }

    /**
     * Set the insert date.
     * @param iDate the insert date.
     */
    public void setInsertDate(Date iDate) {
        this.insertDate = new Date(iDate.getTime());
    }
}
