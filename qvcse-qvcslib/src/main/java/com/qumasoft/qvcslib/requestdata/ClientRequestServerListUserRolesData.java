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
 * List user roles request data.
 * @author Jim Voris
 */
public class ClientRequestServerListUserRolesData extends ClientRequestClientData {
    private static final long serialVersionUID = -9191111791192681334L;

    private static final ValidRequestElementType[] VALID_ELEMENTS = {
        ValidRequestElementType.SERVER_NAME,
        ValidRequestElementType.PROJECT_NAME,
        ValidRequestElementType.BRANCH_NAME,
        ValidRequestElementType.USER_NAME,
        ValidRequestElementType.SYNC_TOKEN
    };

    /**
     * Creates a new instance of ClientRequestServerListUserRolesData.
     */
    public ClientRequestServerListUserRolesData() {
    }

    @Override
    public RequestOperationType getOperationType() {
        return RequestOperationType.LIST_USER_ROLES;
    }

    @Override
    public ValidRequestElementType[] getValidElements() {
        return VALID_ELEMENTS;
    }
}
