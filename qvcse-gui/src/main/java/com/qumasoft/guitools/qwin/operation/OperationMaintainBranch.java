/*   Copyright 2004-2026 Jim Voris
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
package com.qumasoft.guitools.qwin.operation;

import com.qumasoft.guitools.qwin.BranchTreeNode;
import com.qumasoft.guitools.qwin.QWinFrame;
import com.qumasoft.guitools.qwin.dialog.MaintainBranchPropertiesDialog;
import com.qumasoft.qvcslib.ServerProperties;
import java.util.List;

/**
 * Maintain branch operation.
 * @author Jim Voris
 */
public class OperationMaintainBranch {

    private final String projectName;
    private final String branchName;
    private final BranchTreeNode branchNode;

    /**
     * Create a maintain branch operation.
     * @param serverProps the server properties.
     * @param project the project name.
     * @param branch the branch name.
     * @param brNode the branch node.
     */
    public OperationMaintainBranch(ServerProperties serverProps, String project, String branch, BranchTreeNode brNode) {
        projectName = project;
        branchName = branch;
        branchNode = brNode;
    }

    String getProjectName() {
        return projectName;
    }

    String getBranchName() {
        return branchName;
    }

    /**
     * Maintain a branch.
     */
    public void executeOperation() {
        // Ask for the latest tags
        List<String> tagList = QWinFrame.getQWinFrame().getTagList();

        MaintainBranchPropertiesDialog maintainBranchPropertiesDialog = new MaintainBranchPropertiesDialog(QWinFrame.getQWinFrame(), tagList, true, branchNode);
        maintainBranchPropertiesDialog.setVisible(true);
    }
}
