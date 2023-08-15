/*   Copyright 2021-2023 Jim Voris
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
 * Release branch node. Used for Release branches.
 *
 * @author Jim Voris
 */
public class ReleaseBranchNode extends BranchTreeNode {
    private static final long serialVersionUID = -2370155679364038191L;

    /**
     * Creates new ReadWriteBranchNode.
     * @param projectProperties the project properties.
     * @param projectName the project name.
     * @param branchName the branch name.
     */
    public ReleaseBranchNode(RemotePropertiesBaseClass projectProperties, final String projectName, final String branchName) {
        super(projectProperties, projectName, branchName);
    }

    @Override
    public boolean isReadOnlyBranch() {
        return false;
    }

    @Override
    public boolean isReadWriteBranch() {
        return true;
    }

    @Override
    public boolean isReleaseBranch() {
        return true;
    }

    @Override
    public boolean isReadOnlyMoveableTagBranch() {
        return false;
    }
}
