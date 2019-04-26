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
package com.qumasoft.qvcslib.requestdata;

/**
 * Rename request data.
 * @author Jim Voris
 */
public class ClientRequestRenameData extends ClientRequestClientData {
    private static final long serialVersionUID = -7303455239720785854L;

    private static final ValidRequestElementType[] VALID_ELEMENTS = {
        ValidRequestElementType.PROJECT_NAME,
        ValidRequestElementType.VIEW_NAME,
        ValidRequestElementType.APPENDED_PATH,
        ValidRequestElementType.USER_NAME
    };

    private String originalWorkfileName;
    private String newWorkfileName;

    /**
     * Creates new ClientRequestCheckInData.
     */
    public ClientRequestRenameData() {
    }

    /**
     * Get the original short workfile name.
     * @return the original short workfile name.
     */
    public String getOriginalShortWorkfileName() {
        return originalWorkfileName;
    }

    /**
     * Set the original short workfile name.
     * @param originalWorkName the original short workfile name.
     */
    public void setOriginalShortWorkfileName(String originalWorkName) {
        originalWorkfileName = originalWorkName;
    }

    /**
     * Get the new short workfile name.
     * @return the new short workfile name.
     */
    public String getNewShortWorkfileName() {
        return newWorkfileName;
    }

    /**
     * Set the new short workfile name.
     * @param newWorkName the new short workfile name.
     */
    public void setNewShortWorkfileName(String newWorkName) {
        newWorkfileName = newWorkName;
    }

    @Override
    public RequestOperationType getOperationType() {
        return RequestOperationType.RENAME_FILE;
    }

    @Override
    public ValidRequestElementType[] getValidElements() {
        return VALID_ELEMENTS;
    }
}
