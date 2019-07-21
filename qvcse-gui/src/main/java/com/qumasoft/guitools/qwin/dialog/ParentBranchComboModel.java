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
import com.qumasoft.guitools.qwin.QWinFrame;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.RemoteBranchProperties;
import java.util.Enumeration;
import javax.swing.DefaultComboBoxModel;

/**
 * Parent branch combo mode.
 * @author Jim Voris
 */
public class ParentBranchComboModel extends DefaultComboBoxModel<String> {
    private static final long serialVersionUID = -2055902214031932022L;

    ParentBranchComboModel() {
        Enumeration viewEnumeration = QWinFrame.getQWinFrame().getTreeControl().getCurrentBranches();
        while (viewEnumeration.hasMoreElements()) {
            BranchTreeNode viewTreeNode = (BranchTreeNode) viewEnumeration.nextElement();
            RemoteBranchProperties remoteViewProperties = (RemoteBranchProperties) viewTreeNode.getProjectProperties();
            if (remoteViewProperties.getIsOpaqueBranchFlag()
                    || remoteViewProperties.getIsTranslucentBranchFlag()
                    || (0 == QVCSConstants.QVCS_TRUNK_BRANCH.compareTo(remoteViewProperties.getBranchName()))) {
                addElement(remoteViewProperties.getBranchName());
            }
        }
    }
}
