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
package com.qumasoft.qvcslib.requestdata;

/**
 * Remove user role request data.
 * @author Jim Voris
 */
public class ClientRequestServerRemoveUserRoleData extends ClientRequestClientData {
    private static final long serialVersionUID = 503039620758971538L;

    private static final ValidRequestElementType[] VALID_ELEMENTS = {
        ValidRequestElementType.PROJECT_NAME,
        ValidRequestElementType.VIEW_NAME,
        ValidRequestElementType.USER_NAME,
        ValidRequestElementType.ROLE
    };

    /**
     * Creates a new instance of ClientRequestServerRemoveUserRoleData.
     */
    public ClientRequestServerRemoveUserRoleData() {
    }

    @Override
    public RequestOperationType getOperationType() {
        return RequestOperationType.REMOVE_USER_ROLE;
    }

    @Override
    public ValidRequestElementType[] getValidElements() {
        return VALID_ELEMENTS;
    }
}
