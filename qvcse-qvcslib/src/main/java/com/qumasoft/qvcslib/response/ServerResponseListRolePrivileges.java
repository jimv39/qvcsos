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
package com.qumasoft.qvcslib.response;

import com.qumasoft.qvcslib.ArchiveDirManagerProxy;

/**
 * List of role privileges response.
 * @author Jim Voris
 */
public class ServerResponseListRolePrivileges extends AbstractServerManagementResponse {
    private static final long serialVersionUID = -4174644130132620058L;

    private String[] rolePrivilegesList;
    private Boolean[] roleFlagsList;

    /**
     * Creates a new instance of ServerResponseListRolePrivileges.
     */
    public ServerResponseListRolePrivileges() {
    }

    /**
     * Get the role privileges list.
     * @return the role privileges list.
     */
    public String[] getRolePrivilegesList() {
        return rolePrivilegesList;
    }

    /**
     * Set the role privileges list.
     * @param rolePrivList the role privileges list.
     */
    public void setRolePrivilegesList(final String[] rolePrivList) {
        rolePrivilegesList = rolePrivList;
    }

    /**
     * Get the role flags list.
     * @return the role flags list.
     */
    public Boolean[] getRoleFlagsList() {
        return roleFlagsList;
    }

    /**
     * Set the role flags list.
     * @param roleFlgsList the role flags list.
     */
    public void setRoleFlagsList(final Boolean[] roleFlgsList) {
        roleFlagsList = roleFlgsList;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateDirManagerProxy(ArchiveDirManagerProxy directoryManagerProxy) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseOperationType getOperationType() {
        return ResponseOperationType.SR_SERVER_LIST_ROLE_PRIVILEGES;
    }
}
