/*   Copyright 2004-2014 Jim Voris
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
import com.qumasoft.qvcslib.ArchiveDirManagerFactory;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.ServerProperties;
import com.qumasoft.qvcslib.TransportProxyFactory;
import com.qumasoft.qvcslib.TransportProxyInterface;
import com.qumasoft.qvcslib.requestdata.ClientRequestServerDeleteViewData;
import javax.swing.JOptionPane;

/**
 * Delete a view operation.
 * @author Jim Voris
 */
public class OperationDeleteView {

    private final ServerProperties serverProperties;
    private final String projectName;
    private final String viewName;

    /**
     * Create a delete view operation.
     * @param serverProps the server properties.
     * @param project the project name.
     * @param view the view name.
     */
    public OperationDeleteView(ServerProperties serverProps, String project, String view) {
        serverProperties = serverProps;
        projectName = project;
        viewName = view;
    }

    /**
     * Delete a view. A confirmation pop-up will verify your intent.
     */
    public void executeOperation() {
        if (0 == viewName.compareTo(QVCSConstants.QVCS_TRUNK_VIEW)) {
            JOptionPane.showMessageDialog(QWinFrame.getQWinFrame(), "You cannot delete the trunk view", "Delete View Error", JOptionPane.INFORMATION_MESSAGE);
        } else {
            int answer = JOptionPane.showConfirmDialog(QWinFrame.getQWinFrame(), "Delete the '" + viewName + "' view?", "Delete View", JOptionPane.YES_NO_OPTION,
                    JOptionPane.INFORMATION_MESSAGE);
            if (answer == JOptionPane.YES_OPTION) {
                // Throw away any directory managers for the view...
                ArchiveDirManagerFactory.getInstance().discardViewDirectoryManagers(serverProperties.getServerName(), projectName, viewName);

                // Send the request to the server...
                TransportProxyInterface transportProxy = TransportProxyFactory.getInstance().getTransportProxy(serverProperties);
                ClientRequestServerDeleteViewData clientRequestServerDeleteViewData = new ClientRequestServerDeleteViewData();
                clientRequestServerDeleteViewData.setUserName(transportProxy.getUsername());
                clientRequestServerDeleteViewData.setServerName(serverProperties.getServerName());
                clientRequestServerDeleteViewData.setProjectName(projectName);
                clientRequestServerDeleteViewData.setViewName(viewName);
                transportProxy.write(clientRequestServerDeleteViewData);
            }
        }
    }
}
