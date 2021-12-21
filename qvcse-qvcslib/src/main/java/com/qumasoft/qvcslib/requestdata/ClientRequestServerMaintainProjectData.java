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

/**
 * Maintain project request data.
 * @author Jim Voris
 */
public class ClientRequestServerMaintainProjectData extends ClientRequestClientData {
    private static final long serialVersionUID = -6290813975896582315L;

    private static final ValidRequestElementType[] VALID_ELEMENTS = {
        ValidRequestElementType.SERVER_NAME,
        ValidRequestElementType.PROJECT_NAME,
        ValidRequestElementType.BRANCH_NAME,
        ValidRequestElementType.USER_NAME,
        ValidRequestElementType.PASSWORD
    };
    private boolean createReferenceCopyFlag;
    private boolean createOrDeleteCurrentReferenceFilesFlag;
    private boolean defineAlternateReferenceLocationFlag;
    private String alternateReferenceLocation;

    /**
     * Creates a new instance of ClientRequestServerMaintainProjectData.
     */
    public ClientRequestServerMaintainProjectData() {
    }

    /**
     * Get the create reference copy flag.
     * @return the create reference copy flag.
     */
    public boolean getCreateReferenceCopyFlag() {
        return createReferenceCopyFlag;
    }

    /**
     * Set the create reference copy flag.
     * @param flag the create reference copy flag.
     */
    public void setCreateReferenceCopyFlag(boolean flag) {
        createReferenceCopyFlag = flag;
    }

    /**
     * Get the create or delete current reference files flag.
     * @return the create or delete current reference files flag.
     */
    public boolean getCreateOrDeleteCurrentReferenceFilesFlag() {
        return createOrDeleteCurrentReferenceFilesFlag;
    }

    /**
     * Set the create or delete current reference files flag.
     * @param flag the create or delete current reference files flag.
     */
    public void setCreateOrDeleteCurrentReferenceFilesFlag(boolean flag) {
        createOrDeleteCurrentReferenceFilesFlag = flag;
    }

    /**
     * Get the define alternate reference location flag.
     * @return the define alternate reference location flag.
     */
    public boolean getDefineAlternateReferenceLocationFlag() {
        return defineAlternateReferenceLocationFlag;
    }

    /**
     * Set the define alternate reference location flag.
     * @param flag the define alternate reference location flag.
     */
    public void setDefineAlternateReferenceLocationFlag(boolean flag) {
        defineAlternateReferenceLocationFlag = flag;
    }

    /**
     * Get the alternate reference location.
     * @return the alternate reference location.
     */
    public String getAlternateReferenceLocation() {
        if (alternateReferenceLocation == null) {
            return "";
        } else {
            return alternateReferenceLocation;
        }
    }

    /**
     * Set the alternate reference location.
     * @param altReferenceLocation the alternate reference location.
     */
    public void setAlternateReferenceLocation(String altReferenceLocation) {
        alternateReferenceLocation = altReferenceLocation;
    }

    @Override
    public RequestOperationType getOperationType() {
        return RequestOperationType.SERVER_MAINTAIN_PROJECT;
    }

    @Override
    public ValidRequestElementType[] getValidElements() {
        return VALID_ELEMENTS;
    }
}
