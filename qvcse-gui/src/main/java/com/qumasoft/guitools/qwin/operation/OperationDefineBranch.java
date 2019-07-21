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
package com.qumasoft.guitools.qwin.operation;

import com.qumasoft.guitools.qwin.QWinFrame;
import com.qumasoft.guitools.qwin.dialog.MaintainBranchPropertiesDialog;
import com.qumasoft.qvcslib.ServerProperties;
import com.qumasoft.qvcslib.TransportProxyFactory;
import com.qumasoft.qvcslib.TransportProxyInterface;
import com.qumasoft.qvcslib.requestdata.ClientRequestServerCreateBranchData;

/**
 * Define a branch operation.
 * @author Jim Voris
 */
public class OperationDefineBranch {

    private final ServerProperties serverProperties;
    private final String projectName;

    /**
     * Creates a new instance of OperationDefineBranch.
     *
     * @param serverProps server properties.
     * @param project the name of the project.
     */
    public OperationDefineBranch(ServerProperties serverProps, String project) {
        serverProperties = serverProps;
        projectName = project;
    }

    /**
     * Define a branch.
     */
    public void executeOperation() {
        MaintainBranchPropertiesDialog maintainBranchPropertiesDialog = new MaintainBranchPropertiesDialog(QWinFrame.getQWinFrame(), true);
        maintainBranchPropertiesDialog.setVisible(true);

        if (maintainBranchPropertiesDialog.getIsOK()) {
            TransportProxyInterface transportProxy = TransportProxyFactory.getInstance().getTransportProxy(serverProperties);
            ClientRequestServerCreateBranchData clientRequestServerCreateBranchData = new ClientRequestServerCreateBranchData();
            clientRequestServerCreateBranchData.setUserName(transportProxy.getUsername());
            clientRequestServerCreateBranchData.setServerName(serverProperties.getServerName());
            clientRequestServerCreateBranchData.setProjectName(projectName);
            clientRequestServerCreateBranchData.setBranchName(maintainBranchPropertiesDialog.getBranchName());

            clientRequestServerCreateBranchData.setIsReadOnlyBranchFlag(maintainBranchPropertiesDialog.getIsReadOnlyBranchFlag());
            clientRequestServerCreateBranchData.setIsDateBasedBranchFlag(maintainBranchPropertiesDialog.getIsDateBasedBranchFlag());
            clientRequestServerCreateBranchData.setIsTranslucentBranchFlag(maintainBranchPropertiesDialog.getIsTranslucentBranchFlag());
            clientRequestServerCreateBranchData.setIsOpaqueBranchFlag(maintainBranchPropertiesDialog.getIsOpaqueBranchFlag());

            if (maintainBranchPropertiesDialog.getIsDateBasedBranchFlag()) {
                clientRequestServerCreateBranchData.setDateBasedDate(maintainBranchPropertiesDialog.getDate());
            }
            clientRequestServerCreateBranchData.setParentBranchName(maintainBranchPropertiesDialog.getParentBranchName());

            transportProxy.write(clientRequestServerCreateBranchData);
        }
    }
}
