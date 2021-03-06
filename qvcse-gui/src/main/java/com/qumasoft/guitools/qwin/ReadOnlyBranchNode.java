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
package com.qumasoft.guitools.qwin;

import com.qumasoft.qvcslib.AbstractProjectProperties;

/**
 * A read-only branch node.
 * @author Jim Voris
 */
public class ReadOnlyBranchNode extends BranchTreeNode {
    private static final long serialVersionUID = 7037249333912184653L;

    /**
     * Creates new ReadOnlyBranchNode.
     * @param projectProperties the project properties.
     * @param branchName the branch name.
     */
    public ReadOnlyBranchNode(AbstractProjectProperties projectProperties, final String branchName) {
        super(projectProperties, branchName);
    }

    @Override
    boolean isReadOnlyBranch() {
        return true;
    }

    @Override
    public boolean isReadWriteBranch() {
        return false;
    }
}
