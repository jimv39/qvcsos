/*
 * Copyright 2021-2023 Jim Voris.
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
import com.qumasoft.guitools.qwin.dialog.CreateTagDialog;
import com.qumasoft.qvcslib.ClientTransactionManager;
import com.qumasoft.qvcslib.RemotePropertiesBaseClass;
import com.qumasoft.qvcslib.SynchronizationManager;
import com.qumasoft.qvcslib.TransportProxyFactory;
import com.qumasoft.qvcslib.TransportProxyInterface;
import com.qumasoft.qvcslib.requestdata.ClientRequestApplyTagData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Jim Voris
 */
public class OperationCreateTag extends OperationBaseClass {
    /**
     * Create our logger class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(OperationCreateTag.class);

    public OperationCreateTag(String serverName, String projectName, String parentBranchName, RemotePropertiesBaseClass remoteProperties) {
        super(null, serverName, projectName, parentBranchName, remoteProperties);
    }

    @Override
    public void executeOperation() {
        LOGGER.info("OperationCreateTag executeOperation.");
        CreateTagDialog createTagDialog = new CreateTagDialog(QWinFrame.getQWinFrame(), this, true);
        createTagDialog.setFont();
        createTagDialog.center();
        createTagDialog.setVisible(true);
    }

    public void processDialogResult(String newTagText, String description, Boolean moveableTagFlag) {
        // Send the request to the server...
        TransportProxyInterface transportProxy = TransportProxyFactory.getInstance().getTransportProxy(QWinFrame.getQWinFrame().getActiveServerProperties());
        ClientRequestApplyTagData clientRequestApplyTagData = new ClientRequestApplyTagData();
        clientRequestApplyTagData.setUserName(transportProxy.getUsername());
        clientRequestApplyTagData.setServerName(getServerName());
        clientRequestApplyTagData.setProjectName(getProjectName());
        clientRequestApplyTagData.setBranchName(getBranchName());
        clientRequestApplyTagData.setTag(newTagText);
        clientRequestApplyTagData.setDescription(description);
        clientRequestApplyTagData.setMoveableTagFlag(moveableTagFlag);
        int transactionID = ClientTransactionManager.getInstance().sendBeginTransaction(transportProxy);
        SynchronizationManager.getSynchronizationManager().waitOnToken(transportProxy, clientRequestApplyTagData);
        ClientTransactionManager.getInstance().sendEndTransaction(transportProxy, transactionID);
    }
}
