/*   Copyright 2004-2023 Jim Voris
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
package com.qumasoft.guitools.qwin;

import com.qumasoft.qvcslib.RemotePropertiesBaseClass;

/**
 * Branch tree node. The node base class for branch nodes.
 * @author Jim Voris
 */
public abstract class BranchTreeNode extends javax.swing.tree.DefaultMutableTreeNode {
    private static final long serialVersionUID = 8488358470534894969L;

    private final RemotePropertiesBaseClass projectProperties;
    private final String projectName;
    private final String branchName;

    /**
     * Creates a new instance of BranchTreeNode.
     * @param projectProps the project properties.
     * @param project the project name.
     * @param branch the branch name.
     */
    public BranchTreeNode(RemotePropertiesBaseClass projectProps, final String project, final String branch) {
        super(projectProps);
        projectProperties = projectProps;
        projectName = project;
        branchName = branch;
    }

    /**
     * Get the project name.
     * @return the project name.
     */
    public String getProjectName() {
        return projectName;
    }

    /**
     * Get the project properties.
     * @return the project properties.
     */
    public RemotePropertiesBaseClass getProjectProperties() {
        return projectProperties;
    }

    /**
     * Get the branch name.
     * @return the branch name.
     */
    public String getBranchName() {
        return branchName;
    }

    /**
     * Is this a read only branch.
     * @return true if a read-only branch; false otherwise.
     */
    public abstract boolean isReadOnlyBranch();

    /**
     * Is this a read only moveable tag branch.
     *
     * @return true if a read-only moveable tag branch; false otherwise.
     */
    public abstract boolean isReadOnlyMoveableTagBranch();

    /**
     * Is this a read-write branch.
     * @return true if a read-write branch; false otherwise.
     */
    public abstract boolean isReadWriteBranch();

    /**
     * Is this a release branch.
     *
     * @return true if a release branch; false otherwise.
     */
    public abstract boolean isReleaseBranch();
}
