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
import java.util.Properties;

/**
 * List projects response.
 * @author Jim Voris
 */
public class ServerResponseListProjects extends AbstractServerManagementResponse {
    private static final long serialVersionUID = -8975280236765787489L;

    private String serverName;
    // TODO -- This is fragile since the projectList and the projectProperties list must be in the same order.
    private String[] projectList;
    private Properties[] projectProperties;

    /**
     * Creates a new instance of ServerResponseListProjects.
     */
    public ServerResponseListProjects() {
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
     * Get the project list.
     * @return the project list.
     */
    public String[] getProjectList() {
        return projectList;
    }

    /**
     * Set the project list.
     * @param projects the project list.
     */
    public void setProjectList(String[] projects) {
        projectList = projects;
    }

    /**
     * Get the list of project properties.
     * @return the list of project properties.
     */
    public Properties[] getPropertiesList() {
        return projectProperties;
    }

    /**
     * Set the list of project properties.
     * @param propertiesList the list of project properties.
     */
    public void setPropertiesList(Properties[] propertiesList) {
        projectProperties = propertiesList;
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
        return ResponseOperationType.SR_LIST_PROJECTS;
    }
}
