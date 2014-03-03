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
package com.qumasoft.qvcslib;

/**
 * Client request promote files data.
 *
 * @author Jim Voris
 */
public class ClientRequestPromoteFileData extends ClientRequestClientData {
    private static final long serialVersionUID = -6491003468921457511L;

    private static final ValidRequestElementType[] VALID_ELEMENTS = {
        ValidRequestElementType.PROJECT_NAME,
        ValidRequestElementType.USER_NAME,
        ValidRequestElementType.FILE_ID,
        ValidRequestElementType.VIEW_NAME
    };

    private String parentBranchName;
    private String mergedInfoBranchName;
    private FilePromotionInfo filePromotionInfo;

    /**
     * Creates a new instance of ClientRequestPromoteFileData.
     */
    public ClientRequestPromoteFileData() {
    }

    /**
     * Get the parent branch name.
     * @return the parent branch name.
     */
    public String getParentBranchName() {
        return this.parentBranchName;
    }

    /**
     * Set the parent branch name.
     * @param branchName the parent branch name.
     */
    public void setParentBranchName(String branchName) {
        this.parentBranchName = branchName;
    }

    /**
     * Get the merged info branch name.
     * @return the merged info branch name.
     */
    public String getMergedInfoBranchName() {
        return this.mergedInfoBranchName;
    }

    /**
     * Set the merged info branch name.
     * @param branchName the merged info branch name.
     */
    public void setMergedInfoBranchName(String branchName) {
        this.mergedInfoBranchName = branchName;
    }

    /**
     * Get the file promotion information.
     * @return the file promotion information.
     */
    public FilePromotionInfo getFilePromotionInfo() {
        return this.filePromotionInfo;
    }

    /**
     * Set the file promotion information.
     * @param promotionInfo the filePromotionInfo to set
     */
    public void setFilePromotionInfo(FilePromotionInfo promotionInfo) {
        this.filePromotionInfo = promotionInfo;
    }

    /**
     * Get the operation type.
     * @return the operation type.
     */
    @Override
    public RequestOperationType getOperationType() {
        return RequestOperationType.PROMOTE_FILE;
    }

    @Override
    public ValidRequestElementType[] getValidElements() {
        return VALID_ELEMENTS;
    }
}
