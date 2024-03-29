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
import com.qumasoft.guitools.qwin.dialog.PromoteFromChildBranchDialog;
import com.qumasoft.guitools.qwin.dialog.PromoteToParentDialog;
import com.qumasoft.qvcslib.RemotePropertiesBaseClass;
import java.io.File;

/**
 * Operation promote from child branch. Present a dialog to the user to allow them to choose which child branch they want to merge to this parent branch. From the result from that
 * dialog, we show another dialog to show the user the set of files that have changed on the selected branch. The user can then choose from among those files which ones to promote
 * to this parent branch.
 *
 * @author Jim Voris
 */
public class OperationPromoteFilesFromChildBranch extends OperationBaseClass {

    /**
     * Create a promote files from child branch operation.
     * @param serverName the server name.
     * @param projectName the project name.
     * @param parentBranchName the parent branch name.
     * @param remoteProperties user location properties.
     * @param currentWorkfileDirectory the current workfile directory.
     */
    public OperationPromoteFilesFromChildBranch(String serverName, String projectName, String parentBranchName, RemotePropertiesBaseClass remoteProperties,
                                                File currentWorkfileDirectory) {
        super(null, serverName, projectName, parentBranchName, remoteProperties);
    }

    @Override
    public void executeOperation() {
        PromoteFromChildBranchDialog promoteFromChildBranchDialog = new PromoteFromChildBranchDialog(QWinFrame.getQWinFrame(), true, getBranchName());
        promoteFromChildBranchDialog.setVisible(true);
        if (promoteFromChildBranchDialog.isOk()) {
            String branchToPromoteFromName = promoteFromChildBranchDialog.getChildBranchName();
            PromoteToParentDialog promoteToParentDialog = new PromoteToParentDialog(QWinFrame.getQWinFrame(), branchToPromoteFromName);
            promoteToParentDialog.setVisible(true);
        }
    }
}
