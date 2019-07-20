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
 * Set module description request data.
 * @author Jim Voris
 */
public class ClientRequestSetModuleDescriptionData extends ClientRequestClientData {
    private static final long serialVersionUID = 5779779775467564866L;

    private static final ValidRequestElementType[] VALID_ELEMENTS = {
        ValidRequestElementType.PROJECT_NAME,
        ValidRequestElementType.BRANCH_NAME,
        ValidRequestElementType.APPENDED_PATH,
        ValidRequestElementType.SHORT_WORKFILE_NAME
    };
    private String moduleDescription;

    /**
     * Creates a new instance of ClientRequestSetAttributesData.
     */
    public ClientRequestSetModuleDescriptionData() {
    }

    /**
     * Get the module description.
     * @return the module description.
     */
    public String getModuleDescription() {
        return moduleDescription;
    }

    /**
     * Set the module description.
     * @param description the module description.
     */
    public void setModuleDescription(String description) {
        moduleDescription = description;
    }

    @Override
    public RequestOperationType getOperationType() {
        return RequestOperationType.SET_MODULE_DESCRIPTION;
    }

    @Override
    public ValidRequestElementType[] getValidElements() {
        return VALID_ELEMENTS;
    }
}
