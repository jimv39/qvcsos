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
package com.qumasoft.server;

import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.RemoteViewProperties;

/**
 * Project View class. Class that captures the information we need to define a view.
 *
 * @author Jim Voris
 */
public class ProjectView {

    private RemoteViewProperties remoteViewProperties = null;
    private String viewName = null;
    private String projectName = null;

    /**
     * Creates a new instance of ProjectView.
     */
    public ProjectView() {
    }

    /**
     * Get the remote view properties.
     *
     * @return the remote view properties.
     */
    public RemoteViewProperties getRemoteViewProperties() {
        return remoteViewProperties;
    }

    /**
     * Set the remote view properties.
     *
     * @param remoteProperties new value for the remote view properties.
     */
    public void setRemoteViewProperties(RemoteViewProperties remoteProperties) {
        this.remoteViewProperties = remoteProperties;
    }

    /**
     * Get the view name.
     *
     * @return the view name.
     */
    public String getViewName() {
        return viewName;
    }

    /**
     * Set the view name.
     *
     * @param view new value for the view name.
     */
    public void setViewName(String view) {
        this.viewName = view;
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
    public String getTranslucentBranchLabel() {
        String label = "";
        if (getRemoteViewProperties() != null) {
            if (getRemoteViewProperties().getIsTranslucentBranchFlag()) {
                label = QVCSConstants.QVCS_FEATURE_BRANCH_LABEL
                        + getViewName();
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
        if (getRemoteViewProperties() != null) {
            if (getRemoteViewProperties().getIsOpaqueBranchFlag()) {
                label = QVCSConstants.QVCS_OPAQUE_BRANCH_LABEL
                        + getViewName();
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
        buffer.append("View/Branch Name: ").append(getViewName()).append("\n");
        if (getRemoteViewProperties().getIsOpaqueBranchFlag()) {
            buffer.append("Opaque Branch Label: ").append(getOpaqueBranchLabel()).append("\n");
        }
        if (getRemoteViewProperties().getIsTranslucentBranchFlag()) {
            buffer.append("Translucent Branch Label: ").append(getTranslucentBranchLabel()).append("\n");
        }
        buffer.append(getRemoteViewProperties().toString());
        return buffer.toString();
    }
}
