/*   Copyright 2004-2021 Jim Voris
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
import com.qumasoft.qvcslib.ClientTransactionManager;
import com.qumasoft.qvcslib.ServerProperties;
import com.qumasoft.qvcslib.SynchronizationManager;
import com.qumasoft.qvcslib.TransportProxyFactory;
import com.qumasoft.qvcslib.TransportProxyInterface;
import com.qumasoft.qvcslib.requestdata.ClientRequestServerCreateBranchData;
import java.util.List;

/**
 * Define a branch operation.
 * @author Jim Voris
 */
public class OperationDefineBranch {

    private final ServerProperties serverProperties;
    private final String projectName;
    private final String parentBranchName;

    /**
     * Creates a new instance of OperationDefineBranch.
     *
     * @param serverProps server properties.
     * @param project the name of the project.
     * @param branch the branch we are currently on.
     */
    public OperationDefineBranch(ServerProperties serverProps, String project, String branch) {
        serverProperties = serverProps;
        projectName = project;
        parentBranchName = branch;
    }

    /**
     * Define a branch.
     */
    public void executeOperation() {
        // Ask for the latest tags
        List<String> tagList = QWinFrame.getQWinFrame().getTagList();

        MaintainBranchPropertiesDialog maintainBranchPropertiesDialog = new MaintainBranchPropertiesDialog(QWinFrame.getQWinFrame(), this.parentBranchName, tagList, true);
        maintainBranchPropertiesDialog.setVisible(true);

        if (maintainBranchPropertiesDialog.getIsOK()) {
            TransportProxyInterface transportProxy = TransportProxyFactory.getInstance().getTransportProxy(serverProperties);
            ClientRequestServerCreateBranchData clientRequestServerCreateBranchData = new ClientRequestServerCreateBranchData();
            clientRequestServerCreateBranchData.setUserName(transportProxy.getUsername());
            clientRequestServerCreateBranchData.setServerName(serverProperties.getServerName());
            clientRequestServerCreateBranchData.setProjectName(projectName);
            clientRequestServerCreateBranchData.setBranchName(maintainBranchPropertiesDialog.getBranchName());

            clientRequestServerCreateBranchData.setIsReadOnlyBranchFlag(maintainBranchPropertiesDialog.getIsReadOnlyBranchFlag());
            clientRequestServerCreateBranchData.setIsTagBasedBranchFlag(maintainBranchPropertiesDialog.getIsTagBasedBranchFlag());
            clientRequestServerCreateBranchData.setIsFeatureBranchFlag(maintainBranchPropertiesDialog.getIsFeatureBranchFlag());
            clientRequestServerCreateBranchData.setIsReleaseBranchFlag(maintainBranchPropertiesDialog.getIsReleaseBranchFlag());

            if (maintainBranchPropertiesDialog.getIsTagBasedBranchFlag()) {
                clientRequestServerCreateBranchData.setTagBasedTag(maintainBranchPropertiesDialog.getTag());
            }
            clientRequestServerCreateBranchData.setParentBranchName(this.parentBranchName);

            int transactionID = ClientTransactionManager.getInstance().sendBeginTransaction(transportProxy);
            SynchronizationManager.getSynchronizationManager().waitOnToken(transportProxy, clientRequestServerCreateBranchData);
            ClientTransactionManager.getInstance().sendEndTransaction(transportProxy, transactionID);
        }
    }
}
