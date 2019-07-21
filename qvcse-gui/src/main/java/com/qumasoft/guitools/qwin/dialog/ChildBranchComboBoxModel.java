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
package com.qumasoft.guitools.qwin.dialog;

import com.qumasoft.guitools.qwin.BranchTreeNode;
import com.qumasoft.guitools.qwin.ProjectTreeModel;
import com.qumasoft.guitools.qwin.QWinFrame;
import com.qumasoft.qvcslib.RemoteBranchProperties;
import java.util.Collection;
import java.util.Map;
import javax.swing.DefaultComboBoxModel;

/**
 * Child branch combo box model.
 *
 * @author Jim Voris
 */
class ChildBranchComboBoxModel extends DefaultComboBoxModel<String> {
    private static final long serialVersionUID = -6374393402729931027L;

    ChildBranchComboBoxModel(String parentViewName) {
        // See what views are children of this view. Those go into the model.
        ProjectTreeModel projectTreeModel = QWinFrame.getQWinFrame().getTreeModel();
        Map<String, BranchTreeNode> viewNodeMap = projectTreeModel.getPeerBranches(QWinFrame.getQWinFrame().getServerName(),
                QWinFrame.getQWinFrame().getProjectName(), parentViewName);
        Collection<BranchTreeNode> viewNodes = viewNodeMap.values();
        for (BranchTreeNode viewTreeNode : viewNodes) {
            RemoteBranchProperties remoteViewProperties = (RemoteBranchProperties) viewTreeNode.getProjectProperties();
            if (remoteViewProperties.getBranchParent().equals(parentViewName)) {
                addElement(viewTreeNode.getBranchName());
            }
        }
    }
}
