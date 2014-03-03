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
 * Revision data model class. Each instance represents a file revision.
 *
 * @author Jim Voris
 */
public class Revision {
    /*
     * The SQL snippet used to create the Revision table:
     * ID INT GENERATED ALWAYS AS IDENTITY CONSTRAINT REVISION_PK PRIMARY KEY, BRANCH_ID INT NOT NULL, FILE_ID INT NOT NULL,
     * REVISION_STRING VARCHAR(256) NOT NULL, INSERT_DATE TIMESTAMP NOT NULL);
     */

    private Integer id;
    private Integer branchId;
    private Integer fileId;
    private String revisionString;
    private Date insertDate;

    /**
     * Get the primary key.
     * @return the primary key.
     */
    public Integer getId() {
        return id;
    }

    /**
     * Set the primary key.
     * @param pk the primary key.
     */
    public void setId(Integer pk) {
        this.id = pk;
    }

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
     * Get the file id.
     * @return the file id.
     */
    public Integer getFileId() {
        return fileId;
    }

    /**
     * Set the file id.
     * @param fId the file id.
     */
    public void setFileId(Integer fId) {
        this.fileId = fId;
    }

    /**
     * Get the revision string.
     * @return the revision string.
     */
    public String getRevisionString() {
        return revisionString;
    }

    /**
     * Set the revision string.
     * @param revString the revision string.
     */
    public void setRevisionString(String revString) {
        this.revisionString = revString;
    }

    /**
     * Get the insert date.
     * @return the insert date.
     */
    public Date getInsertDate() {
        return insertDate;
    }

    /**
     * Set the insert date.
     * @param iDate the insert date.
     */
    public void setInsertDate(Date iDate) {
        this.insertDate = iDate;
    }
}
