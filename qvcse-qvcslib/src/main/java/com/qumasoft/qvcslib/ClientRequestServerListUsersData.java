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
 * List users request data.
 * @author Jim Voris
 */
public class ClientRequestServerListUsersData extends ClientRequestClientData {
    private static final long serialVersionUID = 5534045623171745215L;

    private static final ValidRequestElementType[] VALID_ELEMENTS = {
        ValidRequestElementType.SERVER_NAME
    };

    /**
     * Creates a new instance of ClientRequestServerAddUserData.
     */
    public ClientRequestServerListUsersData() {
    }

    @Override
    public RequestOperationType getOperationType() {
        return RequestOperationType.LIST_USERS;
    }

    @Override
    public ValidRequestElementType[] getValidElements() {
        return VALID_ELEMENTS;
    }
}
