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
 * Delete project request data.
 * @author Jim Voris
 */
public class ClientRequestServerDeleteProjectData extends ClientRequestClientData {
    private static final long serialVersionUID = 7352594531173297197L;

    private static final ValidRequestElementType[] VALID_ELEMENTS = {
        ValidRequestElementType.SERVER_NAME,
        ValidRequestElementType.USER_NAME,
        ValidRequestElementType.PASSWORD,
        ValidRequestElementType.SYNC_TOKEN
    };
    private String deleteProjectName;

    /**
     * Creates a new instance of ClientRequestServerDeleteProjectData.
     */
    public ClientRequestServerDeleteProjectData() {
    }

    /**
     * Get the name of the project to delete.
     * @return the name of the project to delete.
     */
    public String getDeleteProjectName() {
        return deleteProjectName;
    }

    /**
     * Set the name of the project to delete.
     * @param project the name of the project to delete.
     */
    public void setDeleteProjectName(String project) {
        deleteProjectName = project;
    }

    @Override
    public RequestOperationType getOperationType() {
        return RequestOperationType.SERVER_DELETE_PROJECT;
    }

    @Override
    public ValidRequestElementType[] getValidElements() {
        return VALID_ELEMENTS;
    }
}
