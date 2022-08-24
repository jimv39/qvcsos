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
import com.qumasoft.qvcslib.ArchiveDirManagerFactory;
import com.qumasoft.qvcslib.ClientTransactionManager;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.ServerProperties;
import com.qumasoft.qvcslib.SynchronizationManager;
import com.qumasoft.qvcslib.TransportProxyFactory;
import com.qumasoft.qvcslib.TransportProxyInterface;
import com.qumasoft.qvcslib.requestdata.ClientRequestServerDeleteBranchData;
import javax.swing.JOptionPane;

/**
 * Delete a branch operation.
 * @author Jim Voris
 */
public class OperationDeleteBranch {

    private final ServerProperties serverProperties;
    private final String projectName;
    private final String branchName;

    /**
     * Create a delete branch operation.
     * @param serverProps the server properties.
     * @param project the project name.
     * @param branch the branch name.
     */
    public OperationDeleteBranch(ServerProperties serverProps, String project, String branch) {
        serverProperties = serverProps;
        projectName = project;
        branchName = branch;
    }

    /**
     * Delete a branch. A confirmation pop-up will verify your intent.
     */
    public void executeOperation() {
        if (0 == branchName.compareTo(QVCSConstants.QVCS_TRUNK_BRANCH)) {
            JOptionPane.showMessageDialog(QWinFrame.getQWinFrame(), "You cannot delete the trunk branch", "Delete Branch Error", JOptionPane.INFORMATION_MESSAGE);
        } else {
            int answer = JOptionPane.showConfirmDialog(QWinFrame.getQWinFrame(), "Delete the '" + branchName + "' branch?", "Delete Branch", JOptionPane.YES_NO_OPTION,
                    JOptionPane.INFORMATION_MESSAGE);
            if (answer == JOptionPane.YES_OPTION) {
                // Throw away any directory managers for the branch...
                ArchiveDirManagerFactory.getInstance().discardBranchDirectoryManagers(serverProperties.getServerName(), projectName, branchName);

                // Send the request to the server...
                TransportProxyInterface transportProxy = TransportProxyFactory.getInstance().getTransportProxy(serverProperties);
                ClientRequestServerDeleteBranchData clientRequestServerDeleteBranchData = new ClientRequestServerDeleteBranchData();
                clientRequestServerDeleteBranchData.setUserName(transportProxy.getUsername());
                clientRequestServerDeleteBranchData.setServerName(serverProperties.getServerName());
                clientRequestServerDeleteBranchData.setProjectName(projectName);
                clientRequestServerDeleteBranchData.setBranchName(branchName);
                int transactionID = ClientTransactionManager.getInstance().sendBeginTransaction(transportProxy);
                SynchronizationManager.getSynchronizationManager().waitOnToken(transportProxy, clientRequestServerDeleteBranchData);
                ClientTransactionManager.getInstance().sendEndTransaction(transportProxy, transactionID);
            }
        }
    }
}
