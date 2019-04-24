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
import static com.qumasoft.guitools.qwin.QWinUtility.traceProblem;
import static com.qumasoft.guitools.qwin.QWinUtility.warnProblem;
import com.qumasoft.guitools.qwin.dialog.ProgressDialog;
import com.qumasoft.guitools.qwin.dialog.SetAttributesDialog;
import com.qumasoft.qvcslib.ArchiveAttributes;
import com.qumasoft.qvcslib.ArchiveDirManagerInterface;
import com.qumasoft.qvcslib.ArchiveDirManagerProxy;
import com.qumasoft.qvcslib.ClientTransactionManager;
import com.qumasoft.qvcslib.MergedInfoInterface;
import com.qumasoft.qvcslib.QVCSException;
import com.qumasoft.qvcslib.TransportProxyInterface;
import com.qumasoft.qvcslib.UserLocationProperties;
import com.qumasoft.qvcslib.Utility;
import java.util.List;
import javax.swing.JTable;

/**
 * Set archive attributes operation.
 * @author Jim Voris
 */
public class OperationSetArchiveAttributes extends OperationBaseClass {

    /**
     * Create a set archive attributes operation.
     * @param fileTable the file table.
     * @param serverName the server name.
     * @param projectName the project name.
     * @param viewName the view name.
     * @param userLocationProperties user location properties.
     */
    public OperationSetArchiveAttributes(JTable fileTable, final String serverName, final String projectName, final String viewName,
                                         UserLocationProperties userLocationProperties) {
        super(fileTable, serverName, projectName, viewName, userLocationProperties);
    }

    @Override
    public void executeOperation() {
        if (getFileTable() != null) {
            try {
                List mergedInfoArray = getSelectedFiles();
                if (mergedInfoArray.size() > 0) {
                    SetAttributesDialog setAttributesDialog = new SetAttributesDialog(QWinFrame.getQWinFrame(), mergedInfoArray, this);
                    setAttributesDialog.setVisible(true);
                }
            } catch (Exception e) {
                warnProblem("operationSetArchiveAttributes caught exception: " + e.getClass().toString() + " " + e.getLocalizedMessage());
                warnProblem(Utility.expandStackTraceToString(e));
            }
        }
    }

    /**
     * Process dialog choices.
     * @param mergedInfoArray the list of files to operate on.
     * @param attributes the attribute values to apply.
     */
    public void completeOperation(final List mergedInfoArray, final ArchiveAttributes attributes) {
        // Display the progress dialog.
        final ProgressDialog progressMonitor = createProgressDialog("Changing QVCS Archive Attributes", mergedInfoArray.size());

        Runnable worker = () -> {
            TransportProxyInterface transportProxy = null;
            int transactionID = 0;

            try {
                int size = mergedInfoArray.size();
                for (int i = 0; i < size; i++) {
                    if (progressMonitor.getIsCancelled()) {
                        break;
                    }

                    MergedInfoInterface mergedInfo = (MergedInfoInterface) mergedInfoArray.get(i);

                    if (i == 0) {
                        ArchiveDirManagerInterface archiveDirManager = mergedInfo.getArchiveDirManager();
                        ArchiveDirManagerProxy archiveDirManagerProxy = (ArchiveDirManagerProxy) archiveDirManager;
                        transportProxy = archiveDirManagerProxy.getTransportProxy();
                        transactionID = ClientTransactionManager.getInstance().sendBeginTransaction(transportProxy);
                    }

                    if (mergedInfo.getArchiveInfo() == null) {
                        continue;
                    }

                    // Update the progress monitor.
                    OperationBaseClass.updateProgressDialog(i, "Changing attributes for: " + mergedInfo.getArchiveInfo().getShortWorkfileName(), progressMonitor);

                    if (mergedInfo.getIsRemote()) {
                        if (mergedInfo.setAttributes(mergedInfo.getUserName(), attributes)) {
                            // Log the success.
                            traceProblem("Requested change of archive attributes for " + mergedInfo.getArchiveInfo().getShortWorkfileName()
                                    + " from server.");
                        }
                    } else {
                        warnProblem("Local set attributes operation not supported!!");
                    }
                }
            } catch (QVCSException e) {
                warnProblem("operationSetArchiveAttributes caught exception: " + e.getClass().toString() + " " + e.getLocalizedMessage());
                warnProblem(Utility.expandStackTraceToString(e));
            } finally {
                progressMonitor.close();
                ClientTransactionManager.getInstance().sendEndTransaction(transportProxy, transactionID);
            }
        };

        // Put all this on a separate worker thread.
        new Thread(worker).start();
    }
}
