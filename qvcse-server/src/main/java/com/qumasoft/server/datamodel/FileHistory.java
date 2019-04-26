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
 * File history db model class.
 * @author Jim Voris
 */
public class FileHistory {
    /*
     * The SQL snippet used to create the FileHistory table:
     * ID INT GENERATED ALWAYS AS IDENTITY CONSTRAINT ID_PK PRIMARY KEY, FILE_ID INT, BRANCH_ID INT NOT NULL,
     * DIRECTORY_ID INT NOT NULL, FILE_NAME VARCHAR(256) NOT NULL, INSERT_DATE TIMESTAMP NOT NULL, UPDATE_DATE TIMESTAMP NOT NULL, DELETED_FLAG CHAR(1) NOT NULL);
     */

    private Integer id;
    private Integer fileId;
    private Integer branchId;
    private Integer directoryId;
    private String fileName;
    private Date insertDate;
    private Date updateDate;
    private boolean deletedFlag;

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
     * Get the directory id.
     * @return the directory id.
     */
    public Integer getDirectoryId() {
        return directoryId;
    }

    /**
     * Set the directory id.
     * @param dirId the directory id.
     */
    public void setDirectoryId(Integer dirId) {
        this.directoryId = dirId;
    }

    /**
     * Get the file name.
     * @return the file name.
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Set the file name.
     * @param fName the file name.
     */
    public void setFileName(String fName) {
        this.fileName = fName;
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

    /**
     * Get the update date.
     * @return the update date.
     */
    public Date getUpdateDate() {
        Date returnValue = null;
        if (this.updateDate != null) {
            returnValue = new Date(updateDate.getTime());
        }
        return returnValue;
    }

    /**
     * Set the update date.
     * @param uDate the update date.
     */
    public void setUpdateDate(Date uDate) {
        this.updateDate = new Date(uDate.getTime());
    }

    /**
     * Get the deleted flag.
     * @return the deleted flag.
     */
    public boolean isDeletedFlag() {
        return deletedFlag;
    }

    /**
     * Set the deleted flag.
     * @param flag the deleted flag.
     */
    public void setDeletedFlag(boolean flag) {
        this.deletedFlag = flag;
    }
}
