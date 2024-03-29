/*   Copyright 2004-2023 Jim Voris
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

/**
 * Client request list files to promote.
 *
 * @author Jim Voris
 */
public class ClientRequestListFilesToPromoteData extends ClientRequestClientData {
    private static final long serialVersionUID = 1308421046739751502L;
    private String promoteToBranchName;

    private static final ValidRequestElementType[] VALID_ELEMENTS = {
        ValidRequestElementType.USER_NAME,
        ValidRequestElementType.PROJECT_NAME,
        ValidRequestElementType.BRANCH_NAME,
        ValidRequestElementType.SYNC_TOKEN
    };

    /**
     * Creates new ClientRequestListFilesToPromoteData.
     */
    public ClientRequestListFilesToPromoteData() {
    }

    @Override
    public RequestOperationType getOperationType() {
        return RequestOperationType.LIST_FILES_TO_PROMOTE;
    }

    @Override
    public ValidRequestElementType[] getValidElements() {
        return VALID_ELEMENTS;
    }

    /**
     * @return the promoteToBranchName
     */
    public String getPromoteToBranchName() {
        return promoteToBranchName;
    }

    /**
     * @param branchName the promoteToBranchName to set
     */
    public void setPromoteToBranchName(String branchName) {
        this.promoteToBranchName = branchName;
    }
}
