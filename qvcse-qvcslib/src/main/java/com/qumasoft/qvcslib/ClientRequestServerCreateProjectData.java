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
 * Create project request data.
 * @author Jim Voris
 */
public class ClientRequestServerCreateProjectData extends ClientRequestClientData {
    private static final long serialVersionUID = -4046722466137941839L;

    private static final ValidRequestElementType[] VALID_ELEMENTS = {
        ValidRequestElementType.SERVER_NAME,
        ValidRequestElementType.USER_NAME,
        ValidRequestElementType.PASSWORD
    };
    private String newProjectName;
    private boolean createReferenceCopyFlag;
    private boolean ignoreCaseFlag;
    private boolean defineAlternateReferenceLocationFlag;
    private String alternateReferenceLocation;

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
     * Get the ignore case flag.
     * @return the ignore case flag.
     */
    public boolean getIgnoreCaseFlag() {
        return ignoreCaseFlag;
    }

    /**
     * Set the ignore case flag.
     * @param flag the ignore case flag.
     */
    public void setIgnoreCaseFlag(boolean flag) {
        ignoreCaseFlag = flag;
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
        return alternateReferenceLocation;
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
        return RequestOperationType.SERVER_CREATE_PROJECT;
    }

    @Override
    public ValidRequestElementType[] getValidElements() {
        return VALID_ELEMENTS;
    }
}
