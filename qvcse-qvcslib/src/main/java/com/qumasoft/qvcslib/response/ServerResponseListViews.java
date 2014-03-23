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
 * List views response.
 * @author Jim Voris
 */
public class ServerResponseListViews implements ServerManagementInterface {
    private static final long serialVersionUID = 8887024818849231876L;

    private String serverName;
    private String projectName;
    // TODO -- this is fragile, and relies on the view list elements being in the same order as the view property elements. Should probably just have some object that includes both
    private String[] viewList;
    private Properties[] viewProperties;

    /**
     * Creates a new instance of ServerResponseListViews.
     */
    public ServerResponseListViews() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateDirManagerProxy(ArchiveDirManagerProxy directoryManagerProxy) {
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
        this.serverName = server;
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
        this.projectName = project;
    }

    /**
     * Get the list of view names.
     * @return the list of view names.
     */
    public String[] getViewList() {
        return viewList;
    }

    /**
     * Set the list of view names.
     * @param views the list of view names.
     */
    public void setViewList(String[] views) {
        this.viewList = views;
    }

    /**
     * Get the list of view properties.
     * @return the list of view properties.
     */
    public Properties[] getViewProperties() {
        return viewProperties;
    }

    /**
     * Set the list of view properties.
     * @param viewProps the list of view properties.
     */
    public void setViewProperties(Properties[] viewProps) {
        this.viewProperties = viewProps;
    }

    @Override
    public ResponseOperationType getOperationType() {
        return ResponseOperationType.SR_LIST_VIEWS;
    }
}
