/*   Copyright 2004-2019 Jim Voris
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
package com.qumasoft.qvcslib.requestdata;

import java.util.Date;

/**
 * Create branch request data.
 * @author Jim Voris
 */
public class ClientRequestServerCreateBranchData extends ClientRequestClientData {
    private static final long serialVersionUID = 8477594120551146914L;

    private static final ValidRequestElementType[] VALID_ELEMENTS = {
        ValidRequestElementType.SERVER_NAME,
        ValidRequestElementType.PROJECT_NAME,
        ValidRequestElementType.BRANCH_NAME,
        ValidRequestElementType.USER_NAME
    };

    private boolean isReadOnlyBranchFlag = false;
    private boolean isDateBasedBranchFlag = false;
    private boolean isTranslucentBranchFlag = false;
    private boolean isOpaqueBranchFlag = false;
    private Date dateBasedDate = null;
    private String parentBranchName = null;

    /**
     * Creates a new instance of ClientRequestServerCreateBranchData.
     */
    public ClientRequestServerCreateBranchData() {
    }

    /**
     * Get the is read only type of branch flag.
     * @return the is read only type of branch flag.
     */
    public boolean getIsReadOnlyBranchFlag() {
        return isReadOnlyBranchFlag;
    }

    /**
     * Set the is read only type of branch flag.
     * @param flag the is read only type of branch flag.
     */
    public void setIsReadOnlyBranchFlag(boolean flag) {
        this.isReadOnlyBranchFlag = flag;
    }

    /**
     * Get the is this a date based branch flag.
     * @return the is this a date based branch flag.
     */
    public boolean getIsDateBasedBranchFlag() {
        return isDateBasedBranchFlag;
    }

    /**
     * Set the is this a date based branch flag.
     * @param flag the is this a date based branch flag.
     */
    public void setIsDateBasedBranchFlag(boolean flag) {
        this.isDateBasedBranchFlag = flag;
    }

    /**
     * Get is this a translucent branch flag.
     * @return is this a translucent branch flag.
     */
    public boolean getIsTranslucentBranchFlag() {
        return isTranslucentBranchFlag;
    }

    /**
     * Set is this a translucent branch flag.
     * @param flag is this a translucent branch flag.
     */
    public void setIsTranslucentBranchFlag(boolean flag) {
        this.isTranslucentBranchFlag = flag;
    }

    /**
     * Get is this an opaque branch flag.
     * @return is this an opaque branch flag.
     */
    public boolean getIsOpaqueBranchFlag() {
        return isOpaqueBranchFlag;
    }

    /**
     * Set is this an opaque branch flag.
     * @param flag is this an opaque branch flag.
     */
    public void setIsOpaqueBranchFlag(boolean flag) {
        this.isOpaqueBranchFlag = flag;
    }

    /**
     * Get the date-based anchor date.
     * @return the date-based anchor date.
     */
    public Date getDateBasedDate() {
        return dateBasedDate;
    }

    /**
     * Set the date-based anchor date.
     * @param date the date-based anchor date.
     */
    public void setDateBasedDate(Date date) {
        this.dateBasedDate = date;
    }

    /**
     * Get the parent branch name.
     * @return the parent branch name.
     */
    public String getParentBranchName() {
        return parentBranchName;
    }

    /**
     * Set the parent branch name.
     * @param parentBranch the parent branch name.
     */
    public void setParentBranchName(final String parentBranch) {
        this.parentBranchName = parentBranch;
    }

    @Override
    public RequestOperationType getOperationType() {
        return RequestOperationType.SERVER_CREATE_BRANCH;
    }

    @Override
    public ValidRequestElementType[] getValidElements() {
        return VALID_ELEMENTS;
    }
}
