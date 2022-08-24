/*   Copyright 2004-2021 Jim Voris
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
 * Project control response.
 * @author Jim Voris
 */
public class ServerResponseProjectControl extends AbstractServerResponse {
    private static final long serialVersionUID = -3494907037146271418L;

    private String serverName;
    private String projectName;
    private String branchName;
    private Integer branchId;
    private Integer parentBranchId;
    private String[] directorySegments;
    private boolean addFlag;
    private boolean removeFlag;

    /**
     * Creates a new instance of ServerResponseProjectControl.
     */
    public ServerResponseProjectControl() {
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
        serverName = server;
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
     * Get the branch name.
     * @return the branch name.
     */
    public String getBranchName() {
        return branchName;
    }

    /**
     * Set the branch name.
     * @param branch the branch name.
     */
    public void setBranchName(final String branch) {
        branchName = branch;
    }

    /**
     * Get the add flag.
     * @return the add flag.
     */
    public boolean getAddFlag() {
        return addFlag;
    }

    /**
     * Set the add flag.
     * @param flag the add flag.
     */
    public void setAddFlag(boolean flag) {
        addFlag = flag;
    }

    /**
     * Get the remove flag.
     * @return the remove flag.
     */
    public boolean getRemoveFlag() {
        return removeFlag;
    }

    /**
     * Set the remove flag.
     * @param flag the remove flag.
     */
    public void setRemoveFlag(boolean flag) {
        removeFlag = flag;
    }

    /**
     * Get the directory segments.
     * @return the directory segments.
     */
    public String[] getDirectorySegments() {
        return directorySegments;
    }

    /**
     * Set the directory segments.
     * @param segments the directory segments.
     */
    public void setDirectorySegments(String[] segments) {
        directorySegments = segments;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseOperationType getOperationType() {
        return ResponseOperationType.SR_PROJECT_CONTROL;
    }

    /**
     * @return the branchId
     */
    public Integer getBranchId() {
        return branchId;
    }

    /**
     * @param id the branchId to set
     */
    public void setBranchId(Integer id) {
        this.branchId = id;
    }

    /**
     * @return the parentBranchId
     */
    public Integer getParentBranchId() {
        return parentBranchId;
    }

    /**
     * @param id the parentBranchId to set
     */
    public void setParentBranchId(Integer id) {
        this.parentBranchId = id;
    }
}
