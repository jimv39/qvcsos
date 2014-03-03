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
 * Assign user roles request data.
 * @author Jim Voris
 */
public class ClientRequestServerAssignUserRolesData extends ClientRequestClientData {
    private static final long serialVersionUID = 6323177724487809626L;

    private static final ValidRequestElementType[] VALID_ELEMENTS = {
        ValidRequestElementType.SERVER_NAME,
        ValidRequestElementType.PROJECT_NAME,
        ValidRequestElementType.VIEW_NAME,
        ValidRequestElementType.USER_NAME
    };
    private String[] assignedRoles;

    /**
     * Creates a new instance of ClientRequestServerAssignUserRolesData.
     */
    public ClientRequestServerAssignUserRolesData() {
    }

    /**
     * Get the assigned roles.
     * @return the assigned roles.
     */
    public String[] getAssignedRoles() {
        return assignedRoles;
    }

    /**
     * Set the assigned roles.
     * @param roles the assigned roles.
     */
    public void setAssignedRoles(String[] roles) {
        assignedRoles = roles;
    }

    @Override
    public RequestOperationType getOperationType() {
        return RequestOperationType.ASSIGN_USER_ROLES;
    }

    @Override
    public ValidRequestElementType[] getValidElements() {
        return VALID_ELEMENTS;
    }
}
