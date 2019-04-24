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
import static com.qumasoft.guitools.qwin.QWinUtility.logProblem;
import static com.qumasoft.guitools.qwin.QWinUtility.warnProblem;
import com.qumasoft.guitools.qwin.dialog.ProgressDialog;
import com.qumasoft.guitools.qwin.dialog.UnLabelDialog;
import com.qumasoft.qvcslib.ArchiveDirManagerInterface;
import com.qumasoft.qvcslib.ArchiveDirManagerProxy;
import com.qumasoft.qvcslib.ClientTransactionManager;
import com.qumasoft.qvcslib.MergedInfoInterface;
import com.qumasoft.qvcslib.QVCSException;
import com.qumasoft.qvcslib.TransportProxyInterface;
import com.qumasoft.qvcslib.UserLocationProperties;
import com.qumasoft.qvcslib.Utility;
import com.qumasoft.qvcslib.commandargs.UnLabelRevisionCommandArgs;
import java.io.File;
import java.util.List;
import javax.swing.JTable;

/**
 * Un-label a file operation.
 * @author Jim Voris
 */
public class OperationUnLabelArchive extends OperationBaseClass {

    /**
     * Create an unlabel file operation.
     * @param fileTable the file table.
     * @param serverName the server name.
     * @param projectName the project name.
     * @param viewName the view name.
     * @param userLocationProperties user location properties.
     */
    public OperationUnLabelArchive(JTable fileTable, final String serverName, final String projectName, final String viewName, UserLocationProperties userLocationProperties) {
        super(fileTable, serverName, projectName, viewName, userLocationProperties);
    }

    @Override
    public void executeOperation() {
        if (getFileTable() != null) {
            try {
                List mergedInfoArray = getSelectedFiles();
                if (mergedInfoArray.size() > 0) {
                    UnLabelDialog unLabelDialog = new UnLabelDialog(QWinFrame.getQWinFrame(), mergedInfoArray, true, this);
                    unLabelDialog.setVisible(true);
                }
            } catch (Exception e) {
                warnProblem("Caught exception when removing label: " + e.getClass().toString() + ": " + e.getLocalizedMessage());
                warnProblem(Utility.expandStackTraceToString(e));
            }
        }
    }

    /**
     * Process the dialog results.
     * @param mergedInfoArray the list of files to operate on.
     * @param unLabelDialog the dialog from which we may get additional information.
     */
    public void processDialogResult(final List mergedInfoArray, final UnLabelDialog unLabelDialog) {
        // Display the progress dialog.
        final ProgressDialog progressMonitor = createProgressDialog("Removing label from QVCS Archive", mergedInfoArray.size());

        Runnable worker = () -> {
            TransportProxyInterface transportProxy = null;
            int transactionID = 0;

            try {
                String labelString = unLabelDialog.getLabelString();
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
                    OperationBaseClass.updateProgressDialog(i, "Removing label from: " + mergedInfo.getArchiveInfo().getShortWorkfileName(), progressMonitor);

                    String workfileBase = getUserLocationProperties().getWorkfileLocation(getServerName(), getProjectName(), getViewName());
                    String fullWorkfileName = workfileBase + File.separator + mergedInfo.getArchiveDirManager().getAppendedPath() + File.separator
                            + mergedInfo.getShortWorkfileName();

                    // The command args
                    UnLabelRevisionCommandArgs commandArgs = new UnLabelRevisionCommandArgs();

                    // Get the information from the dialog.
                    commandArgs.setUserName(mergedInfo.getUserName());
                    commandArgs.setShortWorkfileName(mergedInfo.getShortWorkfileName());
                    commandArgs.setLabelString(labelString);

                    if (mergedInfo.unLabelRevision(commandArgs)) {
                        if (mergedInfo.getIsRemote()) {
                            // Log the request.
                            logProblem("Sent remove label request for '" + fullWorkfileName + "' to server.");
                        } else {
                            warnProblem("Local remove label operation not supported!!");
                        }
                    }
                }
            } catch (QVCSException e) {
                warnProblem("Caught exception when removing label: " + e.getClass().toString() + ": " + e.getLocalizedMessage());
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
