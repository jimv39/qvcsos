/*
 * Copyright 2026 Jim Voris.
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
package com.qumasoft.guitools.qwin.operation;

import com.qumasoft.guitools.qwin.QWinFrame;
import com.qumasoft.guitools.qwin.dialog.DeleteLabelDialog;
import com.qumasoft.qvcslib.ClientTransactionManager;
import com.qumasoft.qvcslib.RemotePropertiesBaseClass;
import com.qumasoft.qvcslib.SynchronizationManager;
import com.qumasoft.qvcslib.TransportProxyFactory;
import com.qumasoft.qvcslib.TransportProxyInterface;
import com.qumasoft.qvcslib.requestdata.ClientRequestDeleteLabelData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Jim Voris
 */
public class OperationDeleteLabel extends OperationBaseClass {
    /**
     * Create our logger class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(OperationDeleteLabel.class);

    public OperationDeleteLabel(String serverName, String projectName, String parentBranchName, RemotePropertiesBaseClass remoteProperties) {
        super(null, serverName, projectName, parentBranchName, remoteProperties);
    }

    @Override
    public void executeOperation() {
        LOGGER.info("OperationDeleteLabel executeOperation.");
        DeleteLabelDialog deleteLabelDialog = new DeleteLabelDialog(QWinFrame.getQWinFrame(), this, true);
        deleteLabelDialog.setFont();
        deleteLabelDialog.center();
        deleteLabelDialog.setVisible(true);
    }

    public void processDialogResult(String labelText) {
        // Send the request to the server...
        TransportProxyInterface transportProxy = TransportProxyFactory.getInstance().getTransportProxy(QWinFrame.getQWinFrame().getActiveServerProperties());
        ClientRequestDeleteLabelData clientRequestDeleteLabelData = new ClientRequestDeleteLabelData();
        clientRequestDeleteLabelData.setProjectName(getProjectName());
        clientRequestDeleteLabelData.setLabelText(labelText);
        int transactionID = ClientTransactionManager.getInstance().sendBeginTransaction(transportProxy);
        SynchronizationManager.getSynchronizationManager().waitOnToken(transportProxy, clientRequestDeleteLabelData);
        ClientTransactionManager.getInstance().sendEndTransaction(transportProxy, transactionID);
    }
}
