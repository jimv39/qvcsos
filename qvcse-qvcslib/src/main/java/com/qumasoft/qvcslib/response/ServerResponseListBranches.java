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
package com.qumasoft.qvcslib.response;

import com.qumasoft.qvcslib.ArchiveDirManagerProxy;
import com.qumasoft.qvcslib.ClientBranchInfo;
import java.util.List;

/**
 * List branches response.
 * @author Jim Voris
 */
public class ServerResponseListBranches implements ServerManagementInterface {
    private static final long serialVersionUID = 8887024818849231876L;

    private String serverName;
    private String projectName;
    private List<ClientBranchInfo> clientBranchInfoList;

    /**
     * Creates a new instance of ServerResponseListBranches.
     */
    public ServerResponseListBranches() {
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

    @Override
    public ResponseOperationType getOperationType() {
        return ResponseOperationType.SR_LIST_BRANCHES;
    }

    /**
     * @return the clientBranchInfoList
     */
    public List<ClientBranchInfo> getClientBranchInfoList() {
        return clientBranchInfoList;
    }

    /**
     * @param infoList the clientBranchInfoList to set
     */
    public void setClientBranchInfoList(List<ClientBranchInfo> infoList) {
        this.clientBranchInfoList = infoList;
    }
}
