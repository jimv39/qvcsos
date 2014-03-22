//   Copyright 2004-2014 Jim Voris
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.
//
package com.qumasoft.guitools.qwin.operation;

import com.qumasoft.guitools.qwin.dialog.MaintainViewPropertiesDialog;
import com.qumasoft.guitools.qwin.QWinFrame;
import com.qumasoft.qvcslib.ClientRequestServerCreateViewData;
import com.qumasoft.qvcslib.ServerProperties;
import com.qumasoft.qvcslib.TransportProxyFactory;
import com.qumasoft.qvcslib.TransportProxyInterface;

/**
 * Define a view operation.
 * @author Jim Voris
 */
public class OperationDefineView {

    private final ServerProperties serverProperties;
    private final String projectName;

    /**
     * Creates a new instance of OperationDefineView.
     *
     * @param serverProps server properties.
     * @param project the name of the project.
     */
    public OperationDefineView(ServerProperties serverProps, String project) {
        serverProperties = serverProps;
        projectName = project;
    }

    /**
     * Define a view.
     */
    public void executeOperation() {
        MaintainViewPropertiesDialog maintainViewPropertiesDialog = new MaintainViewPropertiesDialog(QWinFrame.getQWinFrame(), true);
        maintainViewPropertiesDialog.setVisible(true);

        if (maintainViewPropertiesDialog.getIsOK()) {
            TransportProxyInterface transportProxy = TransportProxyFactory.getInstance().getTransportProxy(serverProperties);
            ClientRequestServerCreateViewData clientRequestServerCreateViewData = new ClientRequestServerCreateViewData();
            clientRequestServerCreateViewData.setUserName(transportProxy.getUsername());
            clientRequestServerCreateViewData.setServerName(serverProperties.getServerName());
            clientRequestServerCreateViewData.setProjectName(projectName);
            clientRequestServerCreateViewData.setViewName(maintainViewPropertiesDialog.getViewName());

            clientRequestServerCreateViewData.setIsReadOnlyViewFlag(maintainViewPropertiesDialog.getIsReadOnlyViewFlag());
            clientRequestServerCreateViewData.setIsDateBasedViewFlag(maintainViewPropertiesDialog.getIsDateBasedViewFlag());
            clientRequestServerCreateViewData.setIsTranslucentBranchFlag(maintainViewPropertiesDialog.getIsTranslucentBranchFlag());
            clientRequestServerCreateViewData.setIsOpaqueBranchFlag(maintainViewPropertiesDialog.getIsOpaqueBranchFlag());
            clientRequestServerCreateViewData.setDateBasedViewBranch(maintainViewPropertiesDialog.getDateBasedViewBranch());

            if (maintainViewPropertiesDialog.getIsDateBasedViewFlag()) {
                clientRequestServerCreateViewData.setDateBasedDate(maintainViewPropertiesDialog.getDate());
            } else if (maintainViewPropertiesDialog.getIsTranslucentBranchFlag() || maintainViewPropertiesDialog.getIsOpaqueBranchFlag()) {
                clientRequestServerCreateViewData.setParentBranchName(maintainViewPropertiesDialog.getParentBranchName());
            }

            transportProxy.write(clientRequestServerCreateViewData);
        }
    }
}
