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
package com.qumasoft.server;

import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.RemoteBranchProperties;

/**
 * Project Branch class. Class that captures the information we need to define a branch.
 *
 * @author Jim Voris
 */
public class ProjectBranch {

    private RemoteBranchProperties remoteBranchProperties = null;
    private String branchName = null;
    private String projectName = null;

    /**
     * Creates a new instance of ProjectBranch.
     */
    public ProjectBranch() {
    }

    /**
     * Get the remote branch properties.
     *
     * @return the remote branch properties.
     */
    public RemoteBranchProperties getRemoteBranchProperties() {
        return remoteBranchProperties;
    }

    /**
     * Set the remote branch properties.
     *
     * @param remoteProperties new value for the remote branch properties.
     */
    public void setRemoteBranchProperties(RemoteBranchProperties remoteProperties) {
        this.remoteBranchProperties = remoteProperties;
    }

    /**
     * Get the branch name.
     *
     * @return the branch name.
     */
    public String getBranchName() {
        return branchName;
    }

    /**
     * Set the branch name.
     *
     * @param branch new value for the branch name.
     */
    public void setBranchName(String branch) {
        this.branchName = branch;
    }

    /**
     * Get the project name.
     *
     * @return the project name.
     */
    public String getProjectName() {
        return projectName;
    }

    /**
     * Set the project name.
     *
     * @param project new value for the project name.
     */
    public void setProjectName(String project) {
        this.projectName = project;
    }

    /**
     * This is the QVCS created label that gets applied to revisions that are checked in on a translucent branch.
     *
     * @return the QVCS internal-use label that is used for a translucent branch.
     */
    public String getFeatureBranchLabel() {
        String label = "";
        if (getRemoteBranchProperties() != null) {
            if (getRemoteBranchProperties().getIsTranslucentBranchFlag()) {
                label = QVCSConstants.QVCS_FEATURE_BRANCH_LABEL
                        + getBranchName();
            }
        }
        return label;
    }

    /**
     * This is the QVCS created label that gets applied to revisions that are checked in on a opaque branch.
     *
     * @return the QVCS internal-use label that is used for a opaque branch.
     */
    public String getOpaqueBranchLabel() {
        String label = "";
        if (getRemoteBranchProperties() != null) {
            if (getRemoteBranchProperties().getIsOpaqueBranchFlag()) {
                label = QVCSConstants.QVCS_OPAQUE_BRANCH_LABEL
                        + getBranchName();
            }
        }
        return label;
    }

    /**
     * A useful String representation of this object instance.
     *
     * @return A useful String representation of this object instance.
     */
    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("Project Name: ").append(getProjectName()).append("\n");
        buffer.append("Branch Name: ").append(getBranchName()).append("\n");
        if (getRemoteBranchProperties().getIsOpaqueBranchFlag()) {
            buffer.append("Opaque Branch Label: ").append(getOpaqueBranchLabel()).append("\n");
        }
        if (getRemoteBranchProperties().getIsTranslucentBranchFlag()) {
            buffer.append("Feature Branch Label: ").append(getFeatureBranchLabel()).append("\n");
        }
        buffer.append(getRemoteBranchProperties().toString());
        return buffer.toString();
    }
}
