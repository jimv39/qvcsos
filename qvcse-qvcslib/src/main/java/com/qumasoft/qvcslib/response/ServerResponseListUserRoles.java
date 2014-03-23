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
 * List user roles response.
 * @author Jim Voris
 */
public class ServerResponseListUserRoles implements ServerManagementInterface {
    private static final long serialVersionUID = 5492156955409000455L;

    private String userName;
    private String projectName;
    private String[] userRolesList;
    private String[] availableRolesList;

    /**
     * Creates a new instance of ServerResponseListUserRoles.
     */
    public ServerResponseListUserRoles() {
    }

    /**
     * Get the user name.
     * @return the user name.
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Set the user name.
     * @param user the user name.
     */
    public void setUserName(String user) {
        userName = user;
    }

    /**
     * Get the project name.
     * @return the project name.
     */
    public String getProjectName() {
        return projectName;
    }

    /**
     * Set the project name.
     * @param project the project name.
     */
    public void setProjectName(String project) {
        projectName = project;
    }

    /**
     * Get the list of user roles.
     * @return the list of user roles.
     */
    public String[] getUserRoles() {
        return userRolesList;
    }

    /**
     * Set the list of user roles.
     * @param roles the list of user roles.
     */
    public void setUserRolesList(String[] roles) {
        userRolesList = roles;
    }

    /**
     * Get the list of available roles.
     * @return the list of available roles.
     */
    public String[] getAvailableRoles() {
        return availableRolesList;
    }

    /**
     * Set the list of available roles.
     * @param availRoles the list of available roles.
     */
    public void setAvailableRoles(String[] availRoles) {
        availableRolesList = availRoles;
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
        return ResponseOperationType.SR_LIST_USER_ROLES;
    }
}
