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
package com.qumasoft.guitools.qwin.operation;

import com.qumasoft.guitools.qwin.QWinFrame;
import com.qumasoft.guitools.qwin.dialog.MaintainBranchPropertiesDialog;
import com.qumasoft.qvcslib.RemotePropertiesBaseClass;
import com.qumasoft.qvcslib.ServerProperties;
import java.util.List;

/**
 * Maintain branch operation.
 * @author Jim Voris
 */
public class OperationMaintainBranch {

    private final String projectName;
    private final String branchName;
    private final String parentBranchName;
    private final RemotePropertiesBaseClass remoteProperties;

    /**
     * Create a maintain branch operation.
     * @param serverProps the server properties.
     * @param project the project name.
     * @param branch the branch name.
     * @param rmoteProperties the branch properties.
     */
    public OperationMaintainBranch(ServerProperties serverProps, String project, String branch, RemotePropertiesBaseClass rmoteProperties) {
        projectName = project;
        branchName = branch;
        parentBranchName = rmoteProperties.getBranchParent(project, branch);
        remoteProperties = rmoteProperties;
    }

    String getProjectName() {
        return projectName;
    }

    String getBranchName() {
        return branchName;
    }

    RemotePropertiesBaseClass getRemoteProperties() {
        return remoteProperties;
    }

    /**
     * Maintain a branch.
     */
    public void executeOperation() {
        // Ask for the latest tags
        List<String> tagList = QWinFrame.getQWinFrame().getTagList();

        MaintainBranchPropertiesDialog maintainBranchPropertiesDialog = new MaintainBranchPropertiesDialog(QWinFrame.getQWinFrame(), this.parentBranchName, tagList, true, this.branchName,
                getRemoteProperties());
        maintainBranchPropertiesDialog.setVisible(true);
    }
}
