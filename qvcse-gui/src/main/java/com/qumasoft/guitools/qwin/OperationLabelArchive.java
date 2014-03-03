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
package com.qumasoft.guitools.qwin;

import com.qumasoft.qvcslib.ArchiveDirManagerInterface;
import com.qumasoft.qvcslib.ArchiveDirManagerProxy;
import com.qumasoft.qvcslib.ClientTransactionManager;
import com.qumasoft.qvcslib.LogFileOperationLabelRevisionCommandArgs;
import com.qumasoft.qvcslib.MergedInfoInterface;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.QVCSException;
import com.qumasoft.qvcslib.TransportProxyInterface;
import com.qumasoft.qvcslib.UserLocationProperties;
import com.qumasoft.qvcslib.Utility;
import java.io.File;
import java.util.List;
import java.util.logging.Level;
import javax.swing.JTable;

/**
 * Label archive operation.
 * @author Jim Voris
 */
public class OperationLabelArchive extends OperationBaseClass {

    /**
     * Create a label archive operation.
     * @param fileTable the file table.
     * @param serverName the server name.
     * @param projectName the project name.
     * @param viewName the view name.
     * @param userLocationProperties user location properties.
     */
    OperationLabelArchive(JTable fileTable, final String serverName, final String projectName, final String viewName, UserLocationProperties userLocationProperties) {
        super(fileTable, serverName, projectName, viewName, userLocationProperties);
    }

    @Override
    void executeOperation() {
        if (getFileTable() != null) {
            try {
                List mergedInfoArray = getSelectedFiles();
                if (mergedInfoArray.size() > 0) {
                    LabelDialog labelDialog = new LabelDialog(QWinFrame.getQWinFrame(), mergedInfoArray, true, this);
                    labelDialog.setVisible(true);
                }
            } catch (Exception e) {
                QWinUtility.logProblem(Level.WARNING, "operationLabel caught exception: " + e.getClass().toString() + " " + e.getLocalizedMessage());
                QWinUtility.logProblem(Level.WARNING, Utility.expandStackTraceToString(e));
            }
        }
    }

    void processDialogResult(final List mergedInfoArray, final LabelDialog labelDialog) {
        // Display the progress dialog.
        final ProgressDialog progressMonitor = createProgressDialog("Labeling QVCS Archive", mergedInfoArray.size());

        Runnable worker = new Runnable() {

            @Override
            public void run() {
                TransportProxyInterface transportProxy = null;
                int transactionID = 0;

                try {
                    String labelString = labelDialog.getLabelString();
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

                        // Don't bother if the file is obsolete.
                        if (mergedInfo.getIsObsolete()) {
                            continue;
                        }

                        if (mergedInfo.getArchiveInfo() == null) {
                            continue;
                        }

                        // Do not request a label if the file is in the cemetery.
                        String appendedPath = mergedInfo.getArchiveDirManager().getAppendedPath();
                        if (0 == appendedPath.compareTo(QVCSConstants.QVCS_CEMETERY_DIRECTORY)) {
                            QWinUtility.logProblem(Level.INFO, "Label request for cemetery file ignored for " + mergedInfo.getShortWorkfileName());
                            continue;
                        }
                        // Do not request a label if the file is in the branch archives directory.
                        if (0 == appendedPath.compareTo(QVCSConstants.QVCS_BRANCH_ARCHIVES_DIRECTORY)) {
                            QWinUtility.logProblem(Level.INFO, "Label request for branch archives file ignored for " + mergedInfo.getShortWorkfileName());
                            continue;
                        }

                        // Update the progress monitor.
                        OperationBaseClass.updateProgressDialog(i, "Applying label to: " + mergedInfo.getArchiveInfo().getShortWorkfileName(), progressMonitor);

                        String workfileBase = getUserLocationProperties().getWorkfileLocation(getServerName(), getProjectName(), getViewName());
                        String fullWorkfileName = workfileBase + File.separator + mergedInfo.getArchiveDirManager().getAppendedPath() + File.separator
                                + mergedInfo.getShortWorkfileName();

                        // The command args
                        LogFileOperationLabelRevisionCommandArgs commandArgs = new LogFileOperationLabelRevisionCommandArgs();

                        // Get the information from the dialog.
                        String revisionString = labelDialog.getRevisionString();
                        if (revisionString == null) {
                            revisionString = mergedInfo.getDefaultRevisionString();
                        }

                        // If we are duplicating a label, then we cannot specify a revision string.
                        if (labelDialog.getDuplicateLabelFlag()) {
                            revisionString = null;
                        }
                        commandArgs.setRevisionString(revisionString);
                        commandArgs.setUserName(mergedInfo.getUserName());
                        commandArgs.setShortWorkfileName(mergedInfo.getShortWorkfileName());
                        commandArgs.setLabelString(labelString);
                        commandArgs.setFloatingFlag(labelDialog.getFloatingLabelFlag());
                        commandArgs.setReuseLabelFlag(labelDialog.getReUseLabelFlag());
                        commandArgs.setDuplicateFlag(labelDialog.getDuplicateLabelFlag());

                        commandArgs.setDuplicateLabelString(labelDialog.getDuplicateLabelString());

                        if (mergedInfo.labelRevision(commandArgs)) {
                            if (mergedInfo.getIsRemote()) {
                                // Log the request.
                                QWinUtility.logProblem(Level.INFO, "Sent label revision request for '" + fullWorkfileName + "' to server.");
                            } else {
                                QWinUtility.logProblem(Level.WARNING, "Local label operation not supported!!");
                            }
                        }
                    }
                } catch (QVCSException e) {
                    QWinUtility.logProblem(Level.WARNING, "operationLabel caught exception: " + e.getClass().toString() + " " + e.getLocalizedMessage());
                    QWinUtility.logProblem(Level.WARNING, Utility.expandStackTraceToString(e));
                } finally {
                    progressMonitor.close();
                    ClientTransactionManager.getInstance().sendEndTransaction(transportProxy, transactionID);
                }
            }
        };

        // Put all this on a separate worker thread.
        new Thread(worker).start();
    }
}
