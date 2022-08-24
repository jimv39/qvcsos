/*   Copyright 2004-2021 Jim Voris
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
 * Create project request data.
 * @author Jim Voris
 */
public class ClientRequestServerCreateProjectData extends ClientRequestClientData {
    private static final long serialVersionUID = -4046722466137941839L;

    private static final ValidRequestElementType[] VALID_ELEMENTS = {
        ValidRequestElementType.SERVER_NAME,
        ValidRequestElementType.USER_NAME,
        ValidRequestElementType.PASSWORD,
        ValidRequestElementType.SYNC_TOKEN
    };
    private String newProjectName;

    /**
     * Creates a new instance of ClientRequestServerCreateProjectData.
     */
    public ClientRequestServerCreateProjectData() {
    }

    /**
     * Get the new project name.
     * @return the new project name.
     */
    public String getNewProjectName() {
        return newProjectName;
    }

    /**
     * Set the new project name.
     * @param projectName the new project name.
     */
    public void setNewProjectName(String projectName) {
        newProjectName = projectName;
    }

    @Override
    public RequestOperationType getOperationType() {
        return RequestOperationType.SERVER_CREATE_PROJECT;
    }

    @Override
    public ValidRequestElementType[] getValidElements() {
        return VALID_ELEMENTS;
    }
}
