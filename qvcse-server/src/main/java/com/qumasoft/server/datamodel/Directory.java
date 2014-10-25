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
 * Directory db model class.
 * @author Jim Voris
 */
public class Directory {
    /*
     * The SQL snippet used to create the Directory table: // TODO need to verify that this matches what we actually use.
     * DIRECTORY_ID INT NOT NULL, BRANCH_ID INT NOT NULL, APPENDED_PATH VARCHAR(2048) NOT NULL, INSERT_DATE TIMESTAMP NOT NULL, UPDATE_DATE TIMESTAMP NOT NULL,
     * DELETED_FLAG CHAR(1) NOT NULL
     */

    private Integer directoryId;
    private Integer rootDirectoryId;
    private Integer parentDirectoryId;
    private Integer branchId;
    private String appendedPath;
    private Date insertDate;
    private Date updateDate;
    private boolean deletedFlag;

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
     * Get the root directory id.
     * @return the root directory id.
     */
    public Integer getRootDirectoryId() {
        return rootDirectoryId;
    }

    /**
     * Set the root directory id.
     * @param rootDirId the root directory id.
     */
    public void setRootDirectoryId(Integer rootDirId) {
        this.rootDirectoryId = rootDirId;
    }

    /**
     * Get the parent directory id.
     * @return the parent directory id.
     */
    public Integer getParentDirectoryId() {
        return parentDirectoryId;
    }

    /**
     * Set the parent directory id.
     * @param parentDirId the parent directory id.
     */
    public void setParentDirectoryId(Integer parentDirId) {
        this.parentDirectoryId = parentDirId;
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
     * Get the appended path.
     * @return the appended path.
     */
    public String getAppendedPath() {
        return appendedPath;
    }

    /**
     * Set the appended path.
     * @param path the appended path.
     */
    public void setAppendedPath(String path) {
        this.appendedPath = path;
    }

    /**
     * Get the insert date.
     * @return the insert date.
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

    /**
     * Use this for reporting the contents of this for if/when we throw an exception on
     * inserts or updates.
     * @return a convenient String representation of this object instance.
     */
    @Override
    public String toString() {
        StringBuilder directoryValues = new StringBuilder();
        directoryValues.append("Appended Path: [").append(appendedPath).append("]\n");
        directoryValues.append("Branch Id: [").append(branchId).append("]\n");
        directoryValues.append("DeletedFlag: [").append(deletedFlag).append("]\n");
        directoryValues.append("Directory Id: [").append(directoryId).append("]\n");
        directoryValues.append("Insert Date: [").append(insertDate).append("]\n");
        directoryValues.append("Parent Directory Id: [").append(parentDirectoryId).append("]\n");
        directoryValues.append("Root Directory Id: [").append(rootDirectoryId).append("]\n");
        directoryValues.append("Update Date: [").append(updateDate).append("]\n");
        return directoryValues.toString();
    }
}
