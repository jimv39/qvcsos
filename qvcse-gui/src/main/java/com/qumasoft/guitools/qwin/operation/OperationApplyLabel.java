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
import static com.qumasoft.guitools.qwin.QWinUtility.warnProblem;
import com.qumasoft.guitools.qwin.dialog.ApplyLabelDialog;
import com.qumasoft.qvcslib.ClientTransactionManager;
import com.qumasoft.qvcslib.CommonLabel;
import com.qumasoft.qvcslib.MergedInfoInterface;
import com.qumasoft.qvcslib.RemotePropertiesBaseClass;
import com.qumasoft.qvcslib.SynchronizationManager;
import com.qumasoft.qvcslib.TransportProxyFactory;
import com.qumasoft.qvcslib.TransportProxyInterface;
import com.qumasoft.qvcslib.Utility;
import com.qumasoft.qvcslib.requestdata.ClientRequestApplyLabelData;
import java.util.List;
import javax.swing.JTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Jim Voris.
 */
public class OperationApplyLabel extends OperationBaseClass {
    private List<MergedInfoInterface> mergedInfoArray;
    private Integer fileId;

    /**
     * Create our logger class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(OperationApplyLabel.class);

    public OperationApplyLabel(JTable fileTable, String serverName, String projectName, String branchName, RemotePropertiesBaseClass remoteProperties) {
        super(fileTable, serverName, projectName, branchName, remoteProperties);
    }

    @Override
    public void executeOperation() {
        LOGGER.info("OperationApplyLabel executeOperation.");
        if (getFileTable() != null) {
            if (getFileTable().getSelectedRowCount() == 1) {
                try {
                    mergedInfoArray = getSelectedFiles();
                    if (mergedInfoArray.size() == 1) {
                        this.fileId = mergedInfoArray.get(0).getFileID();
                        ApplyLabelDialog applyLabelDialog = new ApplyLabelDialog(QWinFrame.getQWinFrame(), this);
                        applyLabelDialog.setFont();
                        applyLabelDialog.center();
                        applyLabelDialog.setVisible(true);
                    }
                } catch (Exception e) {
                    warnProblem("OperationApplyLabel caught exception: " + e.getClass().toString() + " " + e.getLocalizedMessage());
                    warnProblem(Utility.expandStackTraceToString(e));
                }
            }
        }
    }

    public void processDialogResult(CommonLabel commonLabel) {
        // Send the request to the server...
        TransportProxyInterface transportProxy = TransportProxyFactory.getInstance().getTransportProxy(QWinFrame.getQWinFrame().getActiveServerProperties());
        ClientRequestApplyLabelData clientRequestApplyLabelData = new ClientRequestApplyLabelData();
        clientRequestApplyLabelData.setProjectName(getProjectName());
        clientRequestApplyLabelData.setLabelText(commonLabel.getLabelText());
        clientRequestApplyLabelData.setLabelId(commonLabel.getLabelId());
        clientRequestApplyLabelData.setFileID(this.fileId);
        int transactionID = ClientTransactionManager.getInstance().sendBeginTransaction(transportProxy);
        SynchronizationManager.getSynchronizationManager().waitOnToken(transportProxy, clientRequestApplyLabelData);
        ClientTransactionManager.getInstance().sendEndTransaction(transportProxy, transactionID);
    }
}
