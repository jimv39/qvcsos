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
package com.qumasoft.guitools.qwin.dialog;

import com.qumasoft.guitools.qwin.BranchTreeNode;
import com.qumasoft.guitools.qwin.ProjectTreeModel;
import com.qumasoft.guitools.qwin.QWinFrame;
import com.qumasoft.qvcslib.ClientBranchInfo;
import com.qumasoft.qvcslib.ClientBranchManager;
import com.qumasoft.qvcslib.RemotePropertiesBaseClass;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import javax.swing.DefaultComboBoxModel;

/**
 * Child branch combo box model.
 *
 * @author Jim Voris
 */
class ChildBranchComboBoxModel extends DefaultComboBoxModel<String> {
    private static final long serialVersionUID = -6374393402729931027L;

    ChildBranchComboBoxModel(String parentBranchName) {
        // See what branches are children of this branch. Those go into the model.
        ProjectTreeModel projectTreeModel = QWinFrame.getQWinFrame().getTreeModel();
        String projectName = QWinFrame.getQWinFrame().getProjectName();
        String serverName = QWinFrame.getQWinFrame().getServerName();
        Map<String, BranchTreeNode> branchNodeMap = projectTreeModel.getPeerBranches(serverName, projectName, parentBranchName);
        Collection<BranchTreeNode> branchNodes = branchNodeMap.values();
        for (BranchTreeNode branchTreeNode : branchNodes) {
            ClientBranchInfo clientBranchInfo = ClientBranchManager.getInstance().getClientBranchInfo(serverName, projectName, branchTreeNode.getBranchName());
            Properties branchProperties = clientBranchInfo.getBranchProperties();
            String branchParent = branchProperties.getProperty(RemotePropertiesBaseClass.getStaticBranchParentTag());
            if (branchParent != null) {
                if (branchParent.equals(parentBranchName)) {
                    if (branchTreeNode.isReadWriteBranch()) {
                        addElement(branchTreeNode.getBranchName());
                    }
                }
            }
        }
    }
}
