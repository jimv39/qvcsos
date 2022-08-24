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
 * Update privileges request data.
 * @author Jim Voris
 */
public class ClientRequestServerUpdatePrivilegesData extends ClientRequestClientData {
    private static final long serialVersionUID = -4918043954133370414L;

    private static final ValidRequestElementType[] VALID_ELEMENTS = {
        ValidRequestElementType.SERVER_NAME,
        ValidRequestElementType.USER_NAME,
        ValidRequestElementType.ROLE,
        ValidRequestElementType.SYNC_TOKEN
    };
    private String[] privileges;
    private Boolean[] privilegesFlags;

    /**
     * Creates a new instance of ClientRequestServerUpdatePrivilegesData.
     */
    public ClientRequestServerUpdatePrivilegesData() {
    }

    /**
     * Get the privileges.
     * @return the privileges.
     */
    public String[] getPrivileges() {
        return privileges;
    }

    /**
     * Set the privileges.
     * @param p the privileges.
     */
    public void setPrivileges(final String[] p) {
        privileges = p;
    }

    /**
     * Get the privileges flags.
     * @return the privileges flags.
     */
    public Boolean[] getPrivilegesFlags() {
        return privilegesFlags;
    }

    /**
     * Set the privileges flags.
     * @param flags the privileges flags.
     */
    public void setPrivilegesFlags(final Boolean[] flags) {
        privilegesFlags = flags;
    }

    @Override
    public RequestOperationType getOperationType() {
        return RequestOperationType.SERVER_UPDATE_ROLE_PRIVILEGES;
    }

    @Override
    public ValidRequestElementType[] getValidElements() {
        return VALID_ELEMENTS;
    }
}
