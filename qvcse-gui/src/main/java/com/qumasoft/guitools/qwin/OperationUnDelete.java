/*
 * Copyright 2022-2023 Jim Voris.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qumasoft.guitools.qwin;

import static com.qumasoft.guitools.qwin.QWinUtility.warnProblem;
import com.qumasoft.guitools.qwin.operation.OperationBaseClass;
import com.qumasoft.qvcslib.ClientTransactionManager;
import com.qumasoft.qvcslib.MergedInfoInterface;
import com.qumasoft.qvcslib.RemotePropertiesBaseClass;
import com.qumasoft.qvcslib.SynchronizationManager;
import com.qumasoft.qvcslib.TransportProxyFactory;
import com.qumasoft.qvcslib.TransportProxyInterface;
import com.qumasoft.qvcslib.Utility;
import com.qumasoft.qvcslib.requestdata.ClientRequestUnDeleteFileData;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;

/**
 *
 * @author Jim Voris
 */
public class OperationUnDelete extends OperationBaseClass {

    public OperationUnDelete(JTable fileTable, String serverName, String projectName, String branchName, RemotePropertiesBaseClass userLocationProperties) {
        super(fileTable, serverName, projectName, branchName, userLocationProperties);
    }

    @Override
    public void executeOperation() {
        if (getFileTable() != null) {
            try {
                List<MergedInfoInterface> mergedInfoArray = getSelectedFiles();
                if (!mergedInfoArray.isEmpty()) {
                    // Run the update on the Swing thread.
                    Runnable later = () -> {
                        // Ask the user if they really want to undelete the file.
                        int answer = JOptionPane.showConfirmDialog(QWinFrame.getQWinFrame(), "UnDelete " + mergedInfoArray.get(0).getShortWorkfileName(),
                                "UnDelete selected file", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
                        if (answer == JOptionPane.YES_OPTION) {
                            completeOperation(mergedInfoArray.get(0));
                        }
                    };
                    SwingUtilities.invokeLater(later);
                }
            } catch (Exception e) {
                warnProblem("Error undeleting file. Caught exception: " + e.getClass().toString() + " " + e.getLocalizedMessage());
                warnProblem(Utility.expandStackTraceToString(e));
            }
        }
    }

    private void completeOperation(MergedInfoInterface mergedInfo) {
        // Send the request to the server...
        TransportProxyInterface transportProxy = TransportProxyFactory.getInstance().getTransportProxy(QWinFrame.getQWinFrame().getActiveServerProperties());
        ClientRequestUnDeleteFileData clientRequestUnDeleteFileData = new ClientRequestUnDeleteFileData();
        clientRequestUnDeleteFileData.setServerName(getServerName());
        clientRequestUnDeleteFileData.setUserName(transportProxy.getUsername());
        clientRequestUnDeleteFileData.setServerName(getServerName());
        clientRequestUnDeleteFileData.setProjectName(getProjectName());
        clientRequestUnDeleteFileData.setBranchName(getBranchName());
        clientRequestUnDeleteFileData.setShortWorkfileName(mergedInfo.getShortWorkfileName());
        clientRequestUnDeleteFileData.setFileID(mergedInfo.getFileID());
        int transactionID = ClientTransactionManager.getInstance().sendBeginTransaction(transportProxy);
        SynchronizationManager.getSynchronizationManager().waitOnToken(transportProxy, clientRequestUnDeleteFileData);
        ClientTransactionManager.getInstance().sendEndTransaction(transportProxy, transactionID);
    }

}
