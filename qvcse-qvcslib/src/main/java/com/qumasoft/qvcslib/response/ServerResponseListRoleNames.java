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
 * List role names response.
 * @author Jim Voris
 */
public class ServerResponseListRoleNames implements ServerManagementInterface {
    private static final long serialVersionUID = 3371369285837662577L;

    private String serverName;
    private String[] roleList;

    /**
     * Creates a new instance of ServerResponseListRoleNames.
     */
    public ServerResponseListRoleNames() {
    }

    /**
     * Get the server name.
     * @return the server name.
     */
    public String getServerName() {
        return serverName;
    }

    /**
     * Set the server name.
     * @param server the server name.
     */
    public void setServerName(String server) {
        serverName = server;
    }

    /**
     * Get the role list.
     * @return the role list.
     */
    public String[] getRoleList() {
        return roleList;
    }

    /**
     * Set the role list.
     * @param roles the role list.
     */
    public void setRoleList(String[] roles) {
        roleList = roles;
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
        return ResponseOperationType.SR_SERVER_LIST_ROLE_NAMES;
    }
}
