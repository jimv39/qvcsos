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

import com.qumasoft.guitools.qwin.QWinFrame;
import com.qumasoft.qvcslib.ClientRequestDeleteDirectoryData;
import com.qumasoft.qvcslib.ClientTransactionManager;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.ServerProperties;
import com.qumasoft.qvcslib.TransportProxyFactory;
import com.qumasoft.qvcslib.TransportProxyInterface;
import javax.swing.JOptionPane;

/**
 * Delete directory operation.
 * @author Jim Voris
 */
public class OperationDeleteDirectory {

    private final ServerProperties serverProperties;
    private final String projectName;
    private final String viewName;
    private final String appendedPath;

    /**
     * Create a delete directory operation.
     * @param serverProps the server properties.
     * @param project the project name.
     * @param view the view name.
     * @param path the appended path.
     */
    public OperationDeleteDirectory(ServerProperties serverProps, String project, String view, String path) {
        serverProperties = serverProps;
        projectName = project;
        viewName = view;
        appendedPath = path;
    }

    /**
     * Delete the given directory. Some checks are performed... e.g. you are not allowed to delete the cemetery directory, or the branch archives directory. A confirmation
     * pop-up will verify your intent.
     */
    public void executeOperation() {
        if (0 == appendedPath.compareTo(QVCSConstants.QVCS_CEMETERY_DIRECTORY)) {
            JOptionPane.showMessageDialog(QWinFrame.getQWinFrame(), "You are not allowed to delete the cemetery directory.", "Cemetery Delete Not Allowed",
                    JOptionPane.INFORMATION_MESSAGE);
        } else if (0 == appendedPath.compareTo(QVCSConstants.QVCS_BRANCH_ARCHIVES_DIRECTORY)) {
            JOptionPane.showMessageDialog(QWinFrame.getQWinFrame(), "You are not allowed to delete the branch archives directory.", "Branch Archives Delete Not Allowed",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            int answer = JOptionPane.showConfirmDialog(QWinFrame.getQWinFrame(), "Delete the '" + appendedPath + "' directory?", "Delete Directory", JOptionPane.YES_NO_OPTION,
                    JOptionPane.INFORMATION_MESSAGE);
            if (answer == JOptionPane.YES_OPTION) {
                TransportProxyInterface transportProxy = TransportProxyFactory.getInstance().getTransportProxy(serverProperties);
                ClientRequestDeleteDirectoryData clientRequestDeleteDirectoryData = new ClientRequestDeleteDirectoryData();
                clientRequestDeleteDirectoryData.setProjectName(projectName);
                clientRequestDeleteDirectoryData.setViewName(viewName);
                clientRequestDeleteDirectoryData.setAppendedPath(appendedPath);

                synchronized (transportProxy) {
                    int transactionID = ClientTransactionManager.getInstance().sendBeginTransaction(transportProxy);
                    transportProxy.write(clientRequestDeleteDirectoryData);
                    ClientTransactionManager.getInstance().sendEndTransaction(transportProxy, transactionID);
                }
            }
        }
    }
}
